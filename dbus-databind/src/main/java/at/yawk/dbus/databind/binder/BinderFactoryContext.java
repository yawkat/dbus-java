package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;

/**
 * @author yawkat
 */
public interface BinderFactoryContext {
    /**
     * Get a binder that may encode and decode the given type.
     */
    Binder<?> getBinder(Type type);

    /**
     * Get a binder that may encode (not necessarily decode to!) the given type.
     */
    Binder<?> getDefaultEncodeBinder(Type type);

    /**
     * Get a binder that may decode (not necessarily encode to!) the given type.
     */
    Binder<?> getDefaultDecodeBinder(TypeDefinition typeDefinition);
}
