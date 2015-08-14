package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.DbusObject;

/**
 * @author yawkat
 */
public interface TypeDefinition {
    String serialize();

    DbusObject deserialize(AlignableByteBuf buf);
}
