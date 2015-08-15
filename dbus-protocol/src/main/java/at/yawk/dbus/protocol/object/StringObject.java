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
public class StringObject extends BasicObject {
    private final String value;

    public StringObject(String value) {
        super(BasicType.STRING);
        this.value = value;
    }

    static StringObject deserialize(AlignableByteBuf buf) {
        buf.alignRead(4);
        int len = Math.toIntExact(buf.readUnsignedInt());
        ByteBuf bts = buf.readBytes(len);
        if (buf.readByte() != 0) {
            throw new DeserializerException("String not properly NUL-terminated");
        }
        return new StringObject(bts.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        buf.alignWrite(4);
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeByte(0);
    }

    @Override
    public String stringValue() {
        return value;
    }
}
