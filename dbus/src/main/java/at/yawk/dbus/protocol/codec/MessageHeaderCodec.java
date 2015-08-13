package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.ArrayObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.VariantTypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * @author yawkat
 */
public class MessageHeaderCodec extends ByteToMessageCodec<MessageHeader> {
    // the headers are an array of byte:variant structs
    private static final ArrayTypeDefinition HEADER_FIELD_LIST_TYPE =
            new ArrayTypeDefinition(new StructTypeDefinition(Arrays.asList(
                    BasicType.BYTE, VariantTypeDefinition.getInstance())));

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
            buf.markReaderIndex();
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
                assert false; // todo: skip message
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
            header.setHeaderFields(new EnumMap<>(HeaderField.class));

            ArrayObject headers;
            try {
                headers = HEADER_FIELD_LIST_TYPE.deserialize(new AlignableByteBuf(buf, 0));
            } catch (IndexOutOfBoundsException e) {
                // not enough data

                // todo: don't catch such a broad exception
                // hack: ignore list out of bounds etc.
                if (e.getClass() != IndexOutOfBoundsException.class) { throw e; }

                buf.resetReaderIndex();
                return;
            }
            for (DbusObject struct : headers.getValues()) {
                HeaderField field = HeaderField.byId(struct.get(0).byteValue());
                if (field != null) {
                    DbusObject value = struct.get(1).getValue();
                    if (!value.getType().equals(field.getType())) {
                        throw new DecoderException(
                                "Invalid header type on " + field + ": got " + value.getType() + " but expected " +
                                field.getType()
                        );
                    }
                    header.getHeaderFields().put(field, value);
                }
            }

            for (HeaderField required : type.getRequiredHeaders()) {
                if (!header.getHeaderFields().containsKey(required)) {
                    throw new DecoderException("Missing required header field " + required);
                }
            }

            out.add(header);
        }
    }
}
