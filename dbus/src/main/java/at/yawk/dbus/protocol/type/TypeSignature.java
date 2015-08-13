package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yawkat
 */
public class TypeSignature {
    private final List<TypeDefinition> types;

    public TypeSignature(List<TypeDefinition> types) {
        this.types = types;
    }

    public List<DbusObject> deserialize(AlignableByteBuf buf) {
        List<DbusObject> objects = new ArrayList<>(types.size());
        //noinspection Convert2streamapi
        for (TypeDefinition type : types) {
            objects.add(type.deserialize(buf));
        }
        return objects;
    }
}
