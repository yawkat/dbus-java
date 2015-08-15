package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public interface BinderFactory {
    @Nullable
    Binder<?> getBinder(BinderFactoryContext ctx, Type type);

    /**
     * Get the default binder to use for the given type definition. For example, for an array type definition, this
     * might return a {@code List<Object>} binder.
     */
    @Nullable
    default Binder<?> getDefaultBinder(BinderFactoryContext ctx, TypeDefinition typeDefinition) {
        return null;
    }
}
