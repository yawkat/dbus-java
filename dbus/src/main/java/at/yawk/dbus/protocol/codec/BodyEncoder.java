package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * @author yawkat
 */
class BodyEncoder extends MessageToMessageEncoder<DbusMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DbusMessage msg, List<Object> out) throws Exception {
        MessageHeader header = msg.getHeader();
        // note: we still modify the header below
        out.add(header);

        MessageBody body = msg.getBody();
        if (body != null && !body.getArguments().isEmpty()) {
            List<TypeDefinition> types = new ArrayList<>(body.getArguments().size());
            ByteBuf bodyBuffer = ctx.alloc().buffer();
            AlignableByteBuf aligned = AlignableByteBuf.fromAlignedBuffer(bodyBuffer, 8);
            for (DbusObject arg : body.getArguments()) {
                types.add(arg.getType());
                arg.serialize(aligned);
            }

            if (header.getHeaderFields() == null) {
                header.setHeaderFields(new EnumMap<>(HeaderField.class));
            }
            header.getHeaderFields().put(HeaderField.SIGNATURE, SignatureObject.create(types));
            header.setMessageBodyLength(bodyBuffer.readableBytes());
            out.add(bodyBuffer);
        }
    }
}
