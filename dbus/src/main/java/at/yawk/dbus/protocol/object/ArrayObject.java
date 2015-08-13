package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public abstract class ArrayObject implements DbusObject {
    static final int ARRAY_MAX_BYTES = 1 << 26; // 64 MiB

    private final ArrayTypeDefinition type;

    ArrayObject(ArrayTypeDefinition type) {
        this.type = type;
    }

    public List<DbusObject> getValues() {
        return new AbstractList<DbusObject>() {
            @Override
            public DbusObject get(int index) {
                return ArrayObject.this.get(index);
            }

            @Override
            public int size() {
                return ArrayObject.this.size();
            }
        };
    }

    public static ArrayObject create(ArrayTypeDefinition type, List<DbusObject> values) {
        for (DbusObject value : values) {
            TypeDefinition valueType = value.getType();
            if (!valueType.equals(type.getMemberType())) {
                throw new IllegalArgumentException(
                        "Mismatched value type " + valueType + ", expected " + type.getMemberType());
            }
        }
        return new SimpleArrayObject(type, values);
    }

    public static ArrayObject deserialize(ArrayTypeDefinition type, AlignableByteBuf buf) {
        buf.alignRead(4);
        int bytes = Math.toIntExact(buf.readUnsignedInt());
        if (bytes > ARRAY_MAX_BYTES) {
            throw new DeserializerException(
                    "Array exceeded length limit (got " + bytes + " bytes, max is " + ARRAY_MAX_BYTES + " bytes)");
        }
        buf.alignRead(8);
        int start = buf.readerIndex();
        List<DbusObject> values = new ArrayList<>();
        while ((buf.readerIndex() - start) < bytes) {
            values.add(type.getMemberType().deserialize(buf));
        }
        return new SimpleArrayObject(type, values);
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        ByteBuf tempBuffer = serializeValues(buf.alloc());

        buf.alignWrite(4);
        buf.writeInt(tempBuffer.writerIndex());
        buf.alignWrite(8);
        buf.writeBytes(tempBuffer);
        tempBuffer.release();
    }

    protected abstract ByteBuf serializeValues(ByteBufAllocator allocator);

    protected abstract int size();

    public abstract DbusObject get(int i);

    private static final class SimpleArrayObject extends ArrayObject {
        private final List<DbusObject> values;

        SimpleArrayObject(ArrayTypeDefinition type, List<DbusObject> values) {
            super(type);
            this.values = values;
        }

        @Override
        protected ByteBuf serializeValues(ByteBufAllocator allocator) {
            AlignableByteBuf tempBuffer = new AlignableByteBuf(allocator.buffer(), 0);
            for (DbusObject value : values) {
                // we align to the 8-byte-mark later so we don't need to pass anything but 0
                value.serialize(tempBuffer);
            }
            return tempBuffer;
        }

        @Override
        protected int size() {
            return values.size();
        }

        @Override
        public DbusObject get(int i) {
            return values.get(i);
        }
    }
}
