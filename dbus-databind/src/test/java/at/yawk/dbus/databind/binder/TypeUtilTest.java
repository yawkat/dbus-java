package at.yawk.dbus.databind.binder;

import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class TypeUtilTest {
    @Test
    public void testGetRawType() throws NoSuchFieldException {
        // class
        assertEquals(TypeUtil.getRawType(String.class), String.class);

        // parameterized
        abstract class A extends AbstractList<String> {}
        assertEquals(TypeUtil.getRawType(A.class.getGenericSuperclass()), AbstractList.class);

        // Array
        class B {
            List<String>[] field;
        }
        Type fieldType = B.class.getDeclaredField("field").getGenericType();
        assertEquals(TypeUtil.getRawType(fieldType), List[].class);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypesTestNG")
    @Test
    public void testGetComponentType() throws NoSuchFieldException {
        assertEquals(TypeUtil.getComponentType(String[].class), String.class);
        assertEquals(TypeUtil.getComponentType(int[].class), int.class);

        class A {
            List<String> component;
            List<String>[] array;
        }
        Type componentType = A.class.getDeclaredField("component").getGenericType();
        Type arrayType = A.class.getDeclaredField("array").getGenericType();
        assertEquals(TypeUtil.getComponentType(arrayType), componentType);
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypesTestNG")
    @Test
    public void testGetTypeVariable() throws NoSuchFieldException {
        class B {
            List<String> field;
        }
        Type fieldType = B.class.getDeclaredField("field").getGenericType();

        assertEquals(TypeUtil.getTypeVariable(fieldType, List.class, "E"), String.class);
        assertEquals(TypeUtil.getTypeVariable(fieldType, Collection.class, "E"), String.class);
        assertEquals(TypeUtil.getTypeVariable(fieldType, Iterable.class, "T"), String.class);
        assertEquals(TypeUtil.getTypeVariable(List.class, Iterable.class, "T"), Object.class);
    }

    @Test
    public void testGetInheritanceTree() throws Exception {
        class B {
            Iterable<String> iterable;
            List<String> list;
        }
        Type iterableType = B.class.getDeclaredField("iterable").getGenericType();
        Type listType = B.class.getDeclaredField("list").getGenericType();
        System.out.println(TypeUtil.getInheritanceTree(iterableType, listType));
    }
}