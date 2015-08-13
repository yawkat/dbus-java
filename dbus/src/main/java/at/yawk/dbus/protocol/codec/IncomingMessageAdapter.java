package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageConsumer;
import at.yawk.dbus.protocol.MessageHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class IncomingMessageAdapter extends SimpleChannelInboundHandler<Object> {
    private final MessageConsumer consumer;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<MessageHeader> headerAttribute = ctx.attr(Local.CURRENT_HEADER);
        if (msg instanceof MessageHeader) {
            if (consumer.requireAccept((MessageHeader) msg)) {
                headerAttribute.set((MessageHeader) msg);
            } else {
                headerAttribute.set(null);
            }
        } else if (msg instanceof MessageBody) {
            MessageHeader header = headerAttribute.get();
            if (header != null) {
                DbusMessage message = new DbusMessage();
                message.setHeader(header);
                message.setBody((MessageBody) msg);
                consumer.accept(message);
                headerAttribute.set(null);
            }
        }
    }
}
