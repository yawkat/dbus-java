package at.yawk.dbus.databind.request;

import at.yawk.dbus.protocol.*;
import at.yawk.dbus.protocol.object.StringObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yawkat
 */
public class ChannelRequestExecutor implements RequestExecutor {
    private final DbusChannel channel;
    private final ChannelRequestStateHolder<Response> stateHolder = new ChannelRequestStateHolder<>();
    private final AtomicInteger serialGenerator = new AtomicInteger();

    public ChannelRequestExecutor(DbusChannel channel) {
        this.channel = channel;
        channel.closeStage().thenRun(stateHolder.createCleaner());
    }

    private int createSerial() {
        int serial;
        do {
            serial = serialGenerator.incrementAndGet();
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
}
