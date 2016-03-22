package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.ArrayTypeDefinition;
import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.DictTypeDefinition;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class DbusObjectTest {
    @Test
    public void testSerializeBasic() {
        testSerialize(BasicObject.createBoolean(true));
        testSerialize(BasicObject.createInt16((short) 0x0fff));
        testSerialize(BasicObject.createInt32(0x0fffffff));
        testSerialize(BasicObject.createInt64(0x0fffffffffffL));
        testSerialize(BasicObject.createDouble(0.16682));
        testSerialize(BasicObject.createString("Test"));
        testSerialize(SignatureObject.create(Arrays.asList(BasicType.INT16, BasicType.INT32)));
    }

    @Test
    public void testSerializeArray() {
        ArrayTypeDefinition type = new ArrayTypeDefinition(BasicType.INT32);
        List<DbusObject> values = Arrays.asList(BasicObject.createInt32(1), BasicObject.createInt32(2));
        testSerialize(ArrayObject.create(type, values));
    }

    @Test
    public void testSerializeStruct() {
        StructTypeDefinition type = new StructTypeDefinition(Arrays.asList(BasicType.INT32, BasicType.INT32));
        List<DbusObject> values = Arrays.asList(BasicObject.createInt32(1), BasicObject.createInt32(2));
        testSerialize(StructObject.create(type, values));
    }

    @Test
    public void testSerializeDict() {
        DictTypeDefinition type = new DictTypeDefinition(BasicType.INT32, BasicType.INT32);
        Map<DbusObject, DbusObject> values = new HashMap<>();
        values.put(BasicObject.createInt32(0), BasicObject.createInt32(1));
        testSerialize(DictObject.create(type, values));
    }

    @Test
    public void testSerializeVariant() {
        testSerialize(VariantObject.create(BasicObject.createInt32(10)));
    }

    private static void testSerialize(DbusObject object) {
        for (int readerOffset = 9; readerOffset < 10; readerOffset++) {
            AlignableByteBuf buf = new AlignableByteBuf(Unpooled.buffer(), readerOffset, 8);
            object.serialize(buf);
            DbusObject des = object.getType().deserialize(buf);
            assertEquals(des, object);
            assertEquals(buf.readableBytes(), 0);
        }
    }
}