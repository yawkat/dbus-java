package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.DictObject;
import at.yawk.dbus.protocol.type.DictTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public class DictBinderFactory implements BinderFactory {
    @Getter
    private static final DictBinderFactory instance = new DictBinderFactory();

    private DictBinderFactory() {}

    @Nullable
    @Override
    public Binder<?> getBinder(BinderFactoryContext ctx, Type type) {
        if (TypeUtil.getRawType(type) == Map.class) {
            return new MapBinder<>(ctx.getBinder(TypeUtil.getTypeVariable(type, Map.class, "K")),
                                   ctx.getBinder(TypeUtil.getTypeVariable(type, Map.class, "V")));
        }

        return null;
    }

    @Nullable
    @Override
    public Binder<?> getDefaultBinder(BinderFactoryContext ctx, TypeDefinition typeDefinition) {
        if (typeDefinition instanceof DictTypeDefinition) {
            return new MapBinder<>(ctx.getDefaultBinder(((DictTypeDefinition) typeDefinition).getKeyType()),
                                   ctx.getDefaultBinder(((DictTypeDefinition) typeDefinition).getValueType()));
        }

        return null;
    }

    private static class MapBinder<K, V> implements Binder<Map<K, V>> {
        Binder<K> keyBinder;
        Binder<V> valueBinder;
        DictTypeDefinition dictType;

        @SuppressWarnings("unchecked")
        public MapBinder(Binder<K> keyBinder, Binder<V> valueBinder) {
            this.keyBinder = keyBinder;
            this.valueBinder = valueBinder;
            dictType = new DictTypeDefinition(this.keyBinder.getType(), this.valueBinder.getType());
        }

        @Override
        public TypeDefinition getType() {
            return dictType;
        }

        @Override
        public Map<K, V> decode(DbusObject object) {
            Map<DbusObject, DbusObject> entries = object.getEntries();
            Map<K, V> obj = new HashMap<>(entries.size());
            entries.forEach((k, v) -> obj.put(keyBinder.decode(k), valueBinder.decode(v)));
            return obj;
        }

        @Override
        public DbusObject encode(Map<K, V> obj) {
            Map<DbusObject, DbusObject> entries = new HashMap<>(obj.size());
            obj.forEach((k, v) -> entries.put(keyBinder.encode(k), valueBinder.encode(v)));
            return DictObject.create(dictType, entries);
        }
    }
}
