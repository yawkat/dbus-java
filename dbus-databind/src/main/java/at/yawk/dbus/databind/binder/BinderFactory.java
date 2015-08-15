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
     * Get the default binder to use for the given type definition. For example, for any type that extends {@link
     * Iterable}, this might return an {@link at.yawk.dbus.protocol.object.ArrayObject} binder. This binder only has
     * to support encoding.
     */
    @Nullable
    default Binder<?> getDefaultEncodeBinder(BinderFactoryContext ctx, Type type) {
        return getBinder(ctx, type);
    }

    /**
     * Get the default binder to use for the given type definition. For example, for an array type definition, this
     * might return a {@code List<Object>} binder. This binder only has to support decoding.
     */
    @Nullable
    default Binder<?> getDefaultDecodeBinder(BinderFactoryContext ctx, TypeDefinition typeDefinition) {
        return null;
    }
}
