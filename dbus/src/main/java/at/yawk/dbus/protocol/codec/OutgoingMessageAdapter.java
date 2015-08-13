package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.DbusMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

/**
 * @author yawkat
 */
class OutgoingMessageAdapter extends MessageToMessageEncoder<DbusMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DbusMessage msg, List<Object> out) throws Exception {
        out.add(msg.getHeader());
        out.add(msg.getBody());
    }
}
