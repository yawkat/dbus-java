package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.ArrayObject;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ArrayTypeDefinition implements TypeDefinition {
    private final TypeDefinition memberType;

    @Override
    public String serialize() {
        return 'a' + memberType.serialize();
    }

    @Override
    public ArrayObject deserialize(AlignableByteBuf buf) {
        return ArrayObject.deserialize(this, buf);
    }
}
