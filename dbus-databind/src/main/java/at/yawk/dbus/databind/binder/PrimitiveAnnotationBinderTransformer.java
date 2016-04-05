package at.yawk.dbus.databind.binder;

import at.yawk.dbus.databind.annotation.Primitive;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.VariantObject;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * @author yawkat
 */
public class PrimitiveAnnotationBinderTransformer implements AnnotationBinderTransformer<Primitive> {
    private static final Map<BasicType, LongFunction<DbusObject>> INTEGER_FACTORIES = new HashMap<>();

    static {
        INTEGER_FACTORIES.put(BasicType.BYTE, l -> BasicObject.createByte((byte) l));
        INTEGER_FACTORIES.put(BasicType.INT16, l -> BasicObject.createInt16((short) l));
        INTEGER_FACTORIES.put(BasicType.UINT16, l -> BasicObject.createUint16((short) l));
        INTEGER_FACTORIES.put(BasicType.INT32, l -> BasicObject.createInt32((int) l));
        INTEGER_FACTORIES.put(BasicType.UINT32, l -> BasicObject.createUint32((int) l));
        INTEGER_FACTORIES.put(BasicType.INT64, BasicObject::createInt64);
        INTEGER_FACTORIES.put(BasicType.UINT64, BasicObject::createUint64);
    }

    private static final Map<BasicType, Function<String, DbusObject>> STRING_FACTORIES = new HashMap<>();

    static {
        STRING_FACTORIES.put(BasicType.STRING, BasicObject::createString);
        STRING_FACTORIES.put(BasicType.OBJECT_PATH, BasicObject::createObjectPath);
    }

    @Override
    public Binder<?> wrap(Primitive annotation, Binder<?> binder) {
        BasicType outerType = annotation.value();
        return transformBinder(binder, outerType);
    }

    /**
     * @param binder    The binder to transform.
     * @param outerType The encoded type of the new binder.
     * @return The wrapped binder.
     */
    public static Binder<?> transformBinder(Binder<?> binder, BasicType outerType) {
        if (outerType == BasicType.VARIANT) {
            return wrap(
                    binder,
                    outerType,
                    DbusObject::getValue,
                    VariantObject::create
            );
        }

        BasicType innerType = (BasicType) binder.getType();

        if (innerType == outerType) { return binder; }

        if (innerType.isInteger() && outerType.isInteger()) {
            LongFunction<DbusObject> innerFactory = INTEGER_FACTORIES.get(innerType);
            LongFunction<DbusObject> outerFactory = INTEGER_FACTORIES.get(outerType);
            return wrap(
                    binder,
                    outerType,
                    o -> innerFactory.apply(o.longValue()),
                    o -> outerFactory.apply(o.longValue())
            );
        }

        if (STRING_FACTORIES.containsKey(innerType) && STRING_FACTORIES.containsKey(outerType)) {
            Function<String, DbusObject> innerFactory = STRING_FACTORIES.get(innerType);
            Function<String, DbusObject> outerFactory = STRING_FACTORIES.get(outerType);
            return wrap(
                    binder,
                    outerType,
                    o -> innerFactory.apply(o.stringValue()),
                    o -> outerFactory.apply(o.stringValue())
            );
        }

        throw new UnsupportedOperationException("Cannot transform " + innerType + " to " + outerType);
    }

    private static <T> Binder<T> wrap(Binder<T> binder,
                                      // outer type
                                      TypeDefinition wrappedType,
                                      // outer -> inner
                                      Function<DbusObject, DbusObject> decoder,
                                      // inner -> outer
                                      Function<DbusObject, DbusObject> encoder) {
        return new Binder<T>() {
            @Override
            public TypeDefinition getType() {
                return wrappedType;
            }

            @Override
            public T decode(DbusObject object) {
                return binder.decode(decoder.apply(object));
            }

            @Override
            public DbusObject encode(T obj) {
                return encoder.apply(binder.encode(obj));
            }
        };
    }
}
