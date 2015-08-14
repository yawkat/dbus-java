package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.StructObject;
import java.util.List;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class StructTypeDefinition implements TypeDefinition {
    private final List<TypeDefinition> members;

    @Override
    public String serialize() {
        StringBuilder builder = new StringBuilder("(");
        for (TypeDefinition member : members) {
            builder.append(member.serialize());
        }
        return builder.append(')').toString();
    }

    @Override
    public StructObject deserialize(AlignableByteBuf buf) {
        return StructObject.deserialize(this, buf);
    }
}
