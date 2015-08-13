package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author yawkat
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
class StringBasicObject extends BasicObject {
    private final BasicType type;
    private final ByteBuf bytes;
    private final String value;

    public StringBasicObject(BasicType type, ByteBuf bytes, String value) {
        super(type);
        this.type = type;
        this.bytes = bytes;
        this.value = value;
    }

    static StringBasicObject deserialize0(BasicType type, AlignableByteBuf buf) {
        assert type.isStringLike();
            buf.alignRead(4);
        int len = Math.toIntExact(buf.readUnsignedInt());
        ByteBuf bts = buf.readBytes(len);
        if (buf.readByte() != 0) {
            throw new DeserializerException("String not properly NUL-terminated");
        }
        return new StringBasicObject(type, bts, bts.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        buf.alignWrite(4);
        buf.writeInt(bytes.readableBytes());
        buf.writeBytes(bytes.slice());
        buf.writeByte(0);
    }

    @Override
    public String stringValue() {
        return value;
    }
}
