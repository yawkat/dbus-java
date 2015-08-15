package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.*;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class ChannelRequestExecutor implements RequestExecutor {
    private final DbusChannel channel;
    private final ChannelRequestStateHolder<Response> stateHolder = new ChannelRequestStateHolder<>();

    // start at 2, 1 is used by hello
    // todo: move this to the actual channel
    private final AtomicInteger serialGenerator = new AtomicInteger(2);

    public ChannelRequestExecutor(DbusChannel channel) {
        this.channel = channel;
        channel.closeStage().thenRun(stateHolder.createCleaner());
        channel.setMessageConsumer(new MessageConsumerImpl());
    }

    private int createSerial() {
        int serial;
        do {
            serial = serialGenerator.getAndIncrement();
        } while (serial == 0);
        return serial;
    }

    @Override
    public Response execute(Request request) throws Exception {
        return executeLater(request).get();
    }

    private CompletableFuture<Response> executeLater(Request request) {
        int serial = createSerial();

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

        CompletableFuture<Response> future = stateHolder.registerPending(serial);

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
            int serial = getReplySerial(header);
            if (serial == 0) {
                log.trace("Discarding {} because of missing serial", header);
                return false;
            }
            return stateHolder.isPending(serial);
        }

        @Override
        public void accept(DbusMessage message) {
            int serial = getReplySerial(message.getHeader());
            if (serial == 0) {
                return;
            }
            DbusObject errorNameObject = message.getHeader().getHeaderFields().get(HeaderField.ERROR_NAME);
            List<DbusObject> arguments = message.getBody().getArguments();
            assert arguments != null;
            Response response = errorNameObject == null ?
                    Response.success(arguments) :
                    Response.error(errorNameObject.stringValue(), arguments);

            stateHolder.complete(serial, response);
        }
    }
}
