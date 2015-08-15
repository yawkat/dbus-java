package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageBody;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class BodyDecoder extends ByteToMessageDecoder {
    private boolean firstData;
    private List<TypeDefinition> types;
    private int currentTypeIndex;
    private List<DbusObject> bodyObjects;
    /**
     * Offset of buffer[readerIndex] in the current body.
     */
    private int bodyOffset;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);

        if (msg instanceof MessageHeader) {
            firstData = true;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.trace("Decode {}", in);

        if (!in.isReadable()) { return; }

        MessageHeader header = ctx.attr(Local.CURRENT_HEADER).get();
        if (header == null) {
            if (!firstData) {
                throw new DecoderException("Message marked to be skipped but already decoded some data: " + in);
            }

            in.skipBytes(in.readableBytes());
            return;
        }

        if (firstData) {
            DbusObject signature = header.getHeaderFields().get(HeaderField.SIGNATURE);
            if (signature == null) { throw new DecoderException("Non-empty body but missing signature header"); }
            types = signature.typeValue();
            currentTypeIndex = 0;
            bodyObjects = new ArrayList<>();
            firstData = false;
        } else if (types == null) {
            throw new DecoderException("Too much data");
        }

        TypeDefinition type = types.get(currentTypeIndex);

        int itemStart = in.readerIndex();

        AlignableByteBuf aligned = AlignableByteBuf.fromOffsetBuffer(in, bodyOffset - in.readerIndex(), 8);
        DbusObject object = MessageHeaderCodec.tryDecode(type, aligned);
        if (object == null) {
            in.readerIndex(itemStart);
            return;
        }
        log.trace("Decoded object {}", object);

        bodyObjects.add(object);
        currentTypeIndex++;
        bodyOffset += in.readerIndex() - itemStart;

        if (currentTypeIndex >= types.size()) {
            MessageBody body = new MessageBody();
            body.setArguments(bodyObjects);
            out.add(body);

            types = null;
            bodyObjects = null;
        }
    }
}
