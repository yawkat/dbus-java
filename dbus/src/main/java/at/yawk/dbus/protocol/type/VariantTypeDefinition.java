package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.VariantObject;
import lombok.Getter;

/**
 * @author yawkat
 */
public class VariantTypeDefinition implements TypeDefinition {
    @Getter
    private static final VariantTypeDefinition instance = new VariantTypeDefinition();

    private VariantTypeDefinition() {}

    @Override
    public String serialize() {
        return "v";
    }

    @Override
    public DbusObject deserialize(AlignableByteBuf buf) {
        return VariantObject.deserialize(buf);
    }
}
