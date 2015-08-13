package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author yawkat
 */
public class MessageHeaderCodec extends ByteToMessageCodec<MessageHeader> {

    // flags
    private static final byte NO_REPLY_EXPECTED = 0x1;
    private static final byte NO_AUTO_START = 0x2;
    private static final byte ALLOW_INTERACTIVE_AUTHORIZATION = 0x4;

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageHeader msg, ByteBuf out)
            throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
            throws Exception {
        if (buf.readableBytes() >= 12) {
            byte endianness = buf.readByte();
            ByteOrder order;
            switch (endianness) {
            case 'l':
                order = ByteOrder.LITTLE_ENDIAN;
                break;
            case 'B':
                order = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new DecoderException("Unknown byte order byte " + endianness);
            }

            buf.order(order);

            MessageType type = MessageType.byId(buf.readByte());
            if (type == null) {
                // todo: handle
            }
            byte flags = buf.readByte();
            byte majorProtocolVersion = buf.readByte();
            if (majorProtocolVersion != 1) {
                throw new DecoderException("Unsupported major protocol version " + majorProtocolVersion);
            }
            long bodyLength = buf.readUnsignedInt();
            int serial = buf.readInt();

            MessageHeader header = new MessageHeader();
            header.setByteOrder(order);
            header.setMessageType(type);
            header.setNoReplyExpected((flags & NO_REPLY_EXPECTED) != 0);
            header.setNoAutoStart((flags & NO_AUTO_START) != 0);
            header.setAllowInteractiveAuthorization((flags & ALLOW_INTERACTIVE_AUTHORIZATION) != 0);
            header.setMajorProtocolVersion(majorProtocolVersion);
            header.setMessageBodyLength(bodyLength);
            header.setSerial(serial);
            out.add(header);
        }
    }
}
