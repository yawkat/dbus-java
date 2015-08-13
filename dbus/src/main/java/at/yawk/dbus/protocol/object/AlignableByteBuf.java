package at.yawk.dbus.protocol.object;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.WrappedByteBuf;

/**
 * @author yawkat
 */
public class AlignableByteBuf extends WrappedByteBuf {
    private final int messageOffset;

    public AlignableByteBuf(ByteBuf buf, int messageOffset) {
        super(buf);
        this.messageOffset = messageOffset;
    }

    private static int calculateAlignmentOffset(int messageOffset, int position, int alignment) {
        return alignment - ((messageOffset + position) % alignment);
    }

    public boolean canAlignRead(int alignment) {
        int toPad = calculateAlignmentOffset(messageOffset, readerIndex(), alignment);
        return readableBytes() >= toPad;
    }

    public void alignRead(int alignment) {
        int i = readerIndex();
        int toPad = calculateAlignmentOffset(messageOffset, i, alignment);
        readerIndex(i + toPad);
    }

    public boolean canAlignWrite(int alignment) {
        int toPad = calculateAlignmentOffset(messageOffset, writerIndex(), alignment);
        return writableBytes() >= toPad;
    }

    public void alignWrite(int alignment) {
        int i = writerIndex();
        int toPad = calculateAlignmentOffset(messageOffset, i, alignment);
        writerIndex(i + toPad);
    }
}
