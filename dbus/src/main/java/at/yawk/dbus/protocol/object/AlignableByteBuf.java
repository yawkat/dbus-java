package at.yawk.dbus.protocol.object;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.WrappedByteBuf;

/**
 * @author yawkat
 */
public class AlignableByteBuf extends WrappedByteBuf {
    private final int messageOffset;
    private final int baseAlignment;

    // visible for testing
    public AlignableByteBuf(ByteBuf buf, int messageOffset, int baseAlignment) {
        super(buf);
        this.messageOffset = messageOffset;
        this.baseAlignment = baseAlignment;
    }

    /**
     * Create a new {@link AlignableByteBuf} from the given message buffer. The message must start at {@code
     * buffer[0]}.
     */
    public static AlignableByteBuf fromMessageBuffer(ByteBuf buffer) {
        return new AlignableByteBuf(buffer, 0, 1 << 30);
    }

    /**
     * Create a new {@link AlignableByteBuf} from a buffer that is known to be aligned to a block boundary with the
     * given block size.
     *
     * @param existingAlignment the alignment of the given buffer.
     */
    public static AlignableByteBuf fromAlignedBuffer(ByteBuf buffer, int existingAlignment) {
        return new AlignableByteBuf(buffer, 0, existingAlignment);
    }

    private static int calculateAlignmentOffset(int messageOffset, int position, int alignment) {
        return alignment - ((messageOffset + position) % alignment);
    }

    private boolean canAlign(int alignment) {
        return (baseAlignment % alignment) == 0;
    }

    private void checkAlign(int alignment) {
        if (!canAlign(alignment)) {
            throw new IllegalArgumentException(
                    "Cannot align to boundary " + alignment + ": base boundary is " + baseAlignment);
        }
    }

    public boolean canAlignRead(int alignment) {
        if (!canAlign(alignment)) { return false; }
        int toPad = calculateAlignmentOffset(messageOffset, readerIndex(), alignment);
        return readableBytes() >= toPad;
    }

    public void alignRead(int alignment) {
        checkAlign(alignment);
        int i = readerIndex();
        int toPad = calculateAlignmentOffset(messageOffset, i, alignment);
        readerIndex(i + toPad);
    }

    public boolean canAlignWrite(int alignment) {
        if (!canAlign(alignment)) { return false; }
        int toPad = calculateAlignmentOffset(messageOffset, writerIndex(), alignment);
        return writableBytes() >= toPad;
    }

    public void alignWrite(int alignment) {
        checkAlign(alignment);
        int i = writerIndex();
        int toPad = calculateAlignmentOffset(messageOffset, i, alignment);
        writerIndex(i + toPad);
    }
}
