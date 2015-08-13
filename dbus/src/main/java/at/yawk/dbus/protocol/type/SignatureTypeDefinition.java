package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import at.yawk.dbus.protocol.object.VariantObject;
import lombok.Getter;

/**
 * @author yawkat
 */
public class SignatureTypeDefinition implements TypeDefinition {
    @Getter
    private static final SignatureTypeDefinition instance = new SignatureTypeDefinition();

    private SignatureTypeDefinition() {}

    @Override
    public String serialize() {
        return "g";
    }

    @Override
    public SignatureObject deserialize(AlignableByteBuf buf) {
        return SignatureObject.deserialize(buf);
    }
}
