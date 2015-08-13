package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public class VariantObject implements DbusObject {
    private final DbusObject object;

    VariantObject(DbusObject object) {
        this.object = object;
    }

    @Override
    public TypeDefinition getType() {
        return VariantTypeDefinition.getInstance();
    }

    public static VariantObject create(DbusObject object) {
        return new VariantObject(object);
    }

    public static VariantObject deserialize(AlignableByteBuf buf) {
        BasicObject signature = StringBasicObject.deserialize(BasicType.SIGNATURE, buf);
        TypeDefinition type;
        try {
            type = TypeParser.parseTypeDefinition(signature.stringValue());
        } catch (MalformedTypeDefinitionException e) {
            throw new DeserializerException(e);
        }
        return new VariantObject(type.deserialize(buf));
    }

    @Override
    public void serialize(AlignableByteBuf buf) {
        BasicObject string = BasicObject.createString(BasicType.SIGNATURE, object.getType().serialize());
        string.serialize(buf);
        object.serialize(buf);
    }
}
