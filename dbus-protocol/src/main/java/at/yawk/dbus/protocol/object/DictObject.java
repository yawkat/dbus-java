package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.DictTypeDefinition;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public class DictObject implements DbusObject {
    private final DictTypeDefinition type;
    private final Map<DbusObject, DbusObject> entries;

    DictObject(DictTypeDefinition type, Map<DbusObject, DbusObject> entries) {
        this.type = type;
        this.entries = entries;
    }

    public static DictObject create(DictTypeDefinition type, Map<DbusObject, DbusObject> entries) {
        entries.forEach((k, v) -> {
            if (!k.getType().equals(type.getKeyType())) {
                throw new IllegalArgumentException(
                        "Mismatched key type: got " + k.getType() + " but was " + type.getKeyType());
            }
            if (!v.getType().equals(type.getValueType())) {
                throw new IllegalArgumentException(
                        "Mismatched key type: got " + v.getType() + " but was " + type.getValueType());
            }
        });
        return new DictObject(type, entries);
    }

    public static DictObject deserialize(DictTypeDefinition type, AlignableByteBuf buf) {
        Map<DbusObject, DbusObject> values = new HashMap<>();
        buf.alignRead(8);
        int bytes = Math.toIntExact(buf.readUnsignedInt());
        if (bytes > 0) {
            buf.alignRead(8);
            int start = buf.readerIndex();
            while (start + bytes > buf.readerIndex()) {
                buf.alignRead(8);
                DbusObject key = type.getKeyType().deserialize(buf);
                DbusObject value = type.getValueType().deserialize(buf);
                values.put(key, value);
            }
        }
        return new DictObject(type, values);
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        AlignableByteBuf tempBuffer = AlignableByteBuf.fromAlignedBuffer(
                ArrayObject.allocateBufferForWrite(buf), 8);
        entries.forEach((k, v) -> {
            tempBuffer.alignWrite(8);
            k.serialize(tempBuffer);
            v.serialize(tempBuffer);
        });

        buf.alignWrite(8);
        buf.writeInt(tempBuffer.writerIndex());
        if (tempBuffer.isReadable()) {
            buf.alignWrite(8);
            buf.writeBytes(tempBuffer.getBuffer());
        }

        tempBuffer.release();
    }
}
