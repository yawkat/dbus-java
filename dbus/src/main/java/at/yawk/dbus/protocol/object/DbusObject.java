package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.TypeDefinition;

/**
 * @author yawkat
 */
public interface DbusObject {
    TypeDefinition getType();

    /**
     * Serialize this object to the given buffer.
     *
     * @param buf The output buffer.
     */
    void serialize(AlignableByteBuf buf);
}
