package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.object.ArrayObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public class ArrayBinderFactory implements BinderFactory {
    @Getter
    private static final ArrayBinderFactory instance = new ArrayBinderFactory();

    private ArrayBinderFactory() {}

    @Override
    public Binder<?> getBinder(BinderFactoryContext ctx, Type type) {
        Class<?> rawType = TypeUtil.getRawType(type);
        if (rawType.isArray()) {
            return new ArrayBinder(rawType, ctx.getBinder(TypeUtil.getComponentType(type)));
        }

        if (rawType == List.class ||
            rawType == Collection.class ||
            rawType == Iterable.class) {
            return new CollectorBinder<>(
                    rawType, Collectors.toList(), ctx.getBinder(TypeUtil.getTypeVariable(type, Iterable.class, "T")));
        }

        if (rawType == Set.class) {
            return new CollectorBinder<>(
                    rawType, Collectors.toSet(), ctx.getBinder(TypeUtil.getTypeVariable(type, Iterable.class, "T")));
        }

        return null;
    }

    @Nullable
    @Override
    public Binder<?> getDefaultBinder(BinderFactoryContext ctx, TypeDefinition typeDefinition) {
        if (typeDefinition instanceof ArrayTypeDefinition) {
            return new CollectorBinder<>(
                    Object.class,
                    Collectors.toList(),
                    ctx.getDefaultBinder(((ArrayTypeDefinition) typeDefinition).getMemberType())
            );
        }

        return null;
    }

    private static class ArrayBinder implements Binder<Object> {
        Binder componentBinder;
        Class<?> rawComponentType;
        ArrayTypeDefinition arrayType;

        public ArrayBinder(Class<?> rawType, Binder<?> componentBinder) {
            this.componentBinder = componentBinder;
            rawComponentType = rawType.getComponentType();
            arrayType = new ArrayTypeDefinition(this.componentBinder.getType());
        }

        @Override
        public TypeDefinition getType() {
            return arrayType;
        }

        @Override
        public Object decode(DbusObject object) {
            List<DbusObject> values = object.getValues();
            Object array = Array.newInstance(rawComponentType, values.size());
            for (int i = 0; i < values.size(); i++) {
                Object entry = componentBinder.decode(values.get(i));
                Array.set(array, i, entry);
            }
            return array;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DbusObject encode(Object obj) {
            int length = Array.getLength(obj);
            List<DbusObject> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(componentBinder.encode(Array.get(obj, i)));
            }
            return ArrayObject.create(arrayType, values);
        }
    }

    private static class CollectorBinder<C extends Iterable<T>, T> implements Binder<C> {
        Binder<T> componentBinder;
        Class<?> rawComponentType;
        ArrayTypeDefinition arrayType;
        Collector<T, ?, C> collector;

        @SuppressWarnings("unchecked")
        public CollectorBinder(Class<?> rawType, Collector<T, ?, C> collector, Binder<T> componentBinder) {
            this.componentBinder = componentBinder;
            rawComponentType = rawType.getComponentType();
            arrayType = new ArrayTypeDefinition(this.componentBinder.getType());
            this.collector = collector;
        }

        @Override
        public TypeDefinition getType() {
            return arrayType;
        }

        @Override
        public C decode(DbusObject object) {
            return object.getValues().stream()
                    .map(componentBinder::decode)
                    .collect(collector);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DbusObject encode(C obj) {
            Stream<T> stream;
            if (obj instanceof Collection) {
                stream = ((Collection<T>) obj).stream();
            } else {
                stream = StreamSupport.stream(obj.spliterator(), false);
            }
            return ArrayObject.create(
                    arrayType,
                    stream.map(componentBinder::encode).collect(Collectors.toList())
            );
        }
    }
}
