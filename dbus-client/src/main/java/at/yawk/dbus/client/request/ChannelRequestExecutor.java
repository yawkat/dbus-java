/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.*;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class ChannelRequestExecutor implements RequestExecutor {
    private final DbusChannel channel;
    private final ChannelRequestStateHolder<Response> requestHolder = new ChannelRequestStateHolder<>();
    private final ListenerHolder listenerHolder = new ListenerHolder();
    private final EventThreadWatcher eventThreadWatcher = new EventThreadWatcher();

    public ChannelRequestExecutor(DbusChannel channel) {
        this.channel = channel;
        channel.closeStage().thenRun(requestHolder.createCleaner());
        channel.setMessageConsumer(new MessageConsumerImpl());
    }

    @Override
    public Response execute(Request request) throws Exception {
        return executeLaterChecked(request).get();
    }

    @Override
    public Response execute(Request request, long timeout, TimeUnit unit) throws Exception {
        return executeLaterChecked(request).get(timeout, unit);
    }

    private CompletableFuture<Response> executeLaterChecked(Request request) {
        eventThreadWatcher.checkLock();
        return executeLater(request);
    }

    @Override
    public Runnable listen(String bus, MatchRule rule, Consumer<List<DbusObject>> listener) {
        String serialized = rule.serialize();
        log.trace("Adding listener {} on match rule {}", listener, serialized);
        StringObject ruleStringObject = BasicObject.createString(serialized);
        DbusMessage registrationMessage = MessageFactory.methodCall(
                "/",
                "org.freedesktop.DBus",
                "org.freedesktop.DBus",
                "AddMatch",
                ruleStringObject
        );
        Consumer<DbusMessage> listenerH = msg -> {
            MessageBody body = msg.getBody();
            eventThreadWatcher.lock();
            try {
                listener.accept(body == null ? Collections.emptyList() : body.getArguments());
            } finally {
                eventThreadWatcher.unlock();
            }
        };
        if (listenerHolder.addListener(rule, listenerH)) {
            channel.write(registrationMessage);
        }
        return () -> {
            if (listenerHolder.removeListener(rule, listenerH)) {
                DbusMessage removeRegistrationMessage = MessageFactory.methodCall(
                        "/",
                        "org.freedesktop.DBus",
                        "org.freedesktop.DBus",
                        "RemoveMatch",
                        ruleStringObject
                );
                channel.write(removeRegistrationMessage);
            }
        };
    }

    private CompletableFuture<Response> executeLater(Request request) {
        int serial = channel.createSerial();

        MessageHeader header = new MessageHeader();
        header.setMessageType(request.getType());
        header.setSerial(serial);

        header.addHeader(HeaderField.INTERFACE, request.getInterfaceName());
        header.addHeader(HeaderField.PATH, request.getObjectPath());
        header.addHeader(HeaderField.MEMBER, request.getMember());
        StringObject destination = request.getDestination();
        if (destination != null) {
            header.addHeader(HeaderField.DESTINATION, destination);
        }

        MessageBody body = new MessageBody();
        body.setArguments(request.getArguments());

        CompletableFuture<Response> future = requestHolder.registerPending(serial);

        DbusMessage message = new DbusMessage();
        message.setHeader(header);
        message.setBody(body);
        channel.write(message);
        return future;
    }

    private class MessageConsumerImpl implements MessageConsumer {
        /**
         * @return The reply serial or {@code 0} if unknown.
         */
        private int getReplySerial(MessageHeader header) {
            DbusObject serialObject = header.getHeaderFields().get(HeaderField.REPLY_SERIAL);
            return serialObject == null ? 0 : serialObject.intValue();
        }

        @Override
        public boolean requireAccept(MessageHeader header) {
            return true;
        }

        @Override
        public void accept(DbusMessage message) {
            listenerHolder.post(message);

            int serial = getReplySerial(message.getHeader());
            if (serial == 0) {
                return;
            }
            DbusObject errorNameObject = message.getHeader().getHeaderFields().get(HeaderField.ERROR_NAME);
            MessageBody body = message.getBody();
            List<DbusObject> arguments = body == null ? Collections.emptyList() : body.getArguments();
            assert arguments != null;
            Response response = errorNameObject == null ?
                    Response.success(arguments) :
                    Response.error(errorNameObject.stringValue(), arguments);

            requestHolder.complete(serial, response);
        }
    }
}
