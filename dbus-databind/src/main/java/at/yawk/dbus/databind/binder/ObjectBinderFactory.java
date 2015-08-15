package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public class ObjectBinderFactory implements BinderFactory {
    @Getter
    private static final ObjectBinderFactory instance = new ObjectBinderFactory();

    private ObjectBinderFactory() {}

    @Nullable
    @Override
    public Binder<?> getBinder(BinderFactoryContext ctx, Type type) {
        if (type == Object.class) {
            return new Binder<Object>() {
                @Override
                public TypeDefinition getType() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object decode(DbusObject object) {
                    return ctx.getDefaultBinder(object.getType()).decode(object);
                }

                @Override
                public DbusObject encode(Object obj) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        return null;
    }
}
