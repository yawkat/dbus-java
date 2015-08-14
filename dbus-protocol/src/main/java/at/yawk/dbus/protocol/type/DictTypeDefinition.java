package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DictObject;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class DictTypeDefinition implements TypeDefinition {
    private final TypeDefinition keyType;
    private final TypeDefinition valueType;

    @Override
    public String serialize() {
        return "a{" + keyType.serialize() + valueType.serialize() + '}';
    }

    @Override
    public DictObject deserialize(AlignableByteBuf buf) {
        return DictObject.deserialize(this, buf);
    }
}
