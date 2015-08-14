package at.yawk.dbus.protocol.type;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class TypeParserTest {
    @Test
    public void testParseBasic() throws MalformedTypeDefinitionException {
        assertEquals(TypeParser.parseTypeDefinition("i"), BasicType.INT32);
    }

    @Test
    public void testParseArray() throws MalformedTypeDefinitionException {
        ArrayTypeDefinition expected = new ArrayTypeDefinition(BasicType.INT32);
        assertEquals(TypeParser.parseTypeDefinition("ai"), expected);
    }

    @Test
    public void testParseStruct() throws MalformedTypeDefinitionException {
        StructTypeDefinition expected = new StructTypeDefinition(Arrays.asList(BasicType.INT32, BasicType.INT32));
        assertEquals(TypeParser.parseTypeDefinition("(ii)"), expected);
    }

    @Test
    public void testParseDict() throws MalformedTypeDefinitionException {
        DictTypeDefinition expected = new DictTypeDefinition(BasicType.INT32, BasicType.INT32);
        assertEquals(TypeParser.parseTypeDefinition("a{ii}"), expected);
    }

    @Test(expectedExceptions = MalformedTypeDefinitionException.class)
    public void testInvalidDict1() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("a{iii}");
    }

    @Test(expectedExceptions = MalformedTypeDefinitionException.class)
    public void testInvalidDict2() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("a{i}");
    }

    @Test(expectedExceptions = BufferUnderflowException.class)
    public void testInvalidDict3() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("a{ii");
    }

    @Test(expectedExceptions = MalformedTypeDefinitionException.class)
    public void testInvalidDict4() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("a{ii)");
    }

    @Test(expectedExceptions = MalformedTypeDefinitionException.class)
    public void testInvalidStruct1() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("(ii}");
    }

    @Test(expectedExceptions = BufferUnderflowException.class)
    public void testInvalidStruct2() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("(ii");
    }

    @Test(expectedExceptions = BufferOverflowException.class)
    public void testOverflow() throws MalformedTypeDefinitionException {
        TypeParser.parseTypeDefinition("(ii)i");
    }
}