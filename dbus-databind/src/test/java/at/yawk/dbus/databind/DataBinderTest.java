package at.yawk.dbus.databind;

import at.yawk.dbus.databind.annotation.Primitive;
import at.yawk.dbus.databind.binder.Binder;
import at.yawk.dbus.protocol.object.*;
import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.DictTypeDefinition;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class DataBinderTest {
    private DataBinder binder;

    @BeforeMethod
    public void setUp() throws Exception {
        binder = new DataBinder();
    }

    private Object decode(Type type, DbusObject object, Annotation... annotations) {
        return binder.getBinder(type, Arrays.asList(annotations)).decode(object);
    }

    @SuppressWarnings("unchecked")
    private DbusObject encode(Type type, Object o, Annotation... annotations) {
        return ((Binder) binder.getBinder(type, Arrays.asList(annotations))).encode(o);
    }

    @Test
    public void testPrimitiveBasic() {
        testConvert(String.class, BasicObject.createString("hi"), "hi");
        testConvert(byte.class, BasicObject.createByte((byte) 0x99), (byte) 0x99);
        testConvert(short.class, BasicObject.createInt16((short) 0xfff), (short) 0xfff);
        testConvert(int.class, BasicObject.createInt32(0xffffff), 0xffffff);
        testConvert(long.class, BasicObject.createInt64(0xffffffL), 0xffffffL);
        testConvert(double.class, BasicObject.createDouble(0.145), 0.145);
        testConvert(float.class, BasicObject.createDouble(0.145F), 0.145F);
    }

    @Test
    public void testPrimitiveConvertExplicit() {
        Primitive primitive = Mockito.mock(Primitive.class, Mockito.withSettings());
        Mockito.<Object>when(primitive.annotationType()).thenReturn(Primitive.class);

        Mockito.when(primitive.value()).thenReturn(BasicType.UINT64);
        testConvert(byte.class, BasicObject.createUint64(0x99L), (byte) 0x99, primitive);

        Mockito.when(primitive.value()).thenReturn(BasicType.VARIANT);
        testConvert(String.class, VariantObject.create(BasicObject.createString("test")), "test", primitive);
    }

    @Test
    public void testArray() throws NoSuchFieldException {
        ArrayTypeDefinition typeDefinition = new ArrayTypeDefinition(BasicType.STRING);
        ArrayObject arrayObject = ArrayObject.create(
                typeDefinition,
                Arrays.asList(BasicObject.createString("a"), BasicObject.createString("b"))
        );

        testConvert(String[].class, arrayObject, new String[]{ "a", "b" });

        class A {
            List<String> list;
            Collection<String> collection;
            Iterable<String> iterable;
            Set<String> set;
        }

        testConvert(A.class.getDeclaredField("list").getGenericType(), arrayObject,
                    Arrays.asList("a", "b"));
        testConvert(A.class.getDeclaredField("iterable").getGenericType(), arrayObject,
                    Arrays.asList("a", "b"));
        testConvert(A.class.getDeclaredField("collection").getGenericType(), arrayObject,
                    Arrays.asList("a", "b"));
        testConvert(A.class.getDeclaredField("set").getGenericType(), arrayObject,
                    new HashSet<>(Arrays.asList("a", "b")));
    }

    @Test
    public void testDict() throws NoSuchFieldException {
        DictTypeDefinition typeDefinition = new DictTypeDefinition(BasicType.STRING, BasicType.STRING);
        DictObject dictObject = DictObject.create(
                typeDefinition,
                new HashMap<DbusObject, DbusObject>() {{
                    put(BasicObject.createString("a"), BasicObject.createString("b"));
                }}
        );

        class A {
            Map<String, String> map;
        }

        testConvert(A.class.getDeclaredField("map").getGenericType(), dictObject,
                    new HashMap<String, String>() {{
                        put("a", "b");
                    }});
    }

    @Test
    public void testObject() {
        ArrayTypeDefinition typeDefinition = new ArrayTypeDefinition(BasicType.STRING);
        ArrayObject arrayObject = ArrayObject.create(
                typeDefinition,
                Arrays.asList(BasicObject.createString("a"), BasicObject.createString("b"))
        );
        testConvert(Object.class, arrayObject, Arrays.asList("a", "b"));
    }

    private void testConvert(Type javaType, DbusObject dbusRepresentation, Object javaRepresentation,
                             Annotation... annotations) {
        assertEquals(decode(javaType, dbusRepresentation, annotations), javaRepresentation);
        assertEquals(encode(javaType, javaRepresentation, annotations), dbusRepresentation);
    }
}