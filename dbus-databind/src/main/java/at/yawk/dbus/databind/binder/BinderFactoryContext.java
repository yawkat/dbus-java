package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;

/**
 * @author yawkat
 */
public interface BinderFactoryContext {
    Binder<?> getBinder(Type type);

    Binder<?> getDefaultBinder(TypeDefinition typeDefinition);
}
