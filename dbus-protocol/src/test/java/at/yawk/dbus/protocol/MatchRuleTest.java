package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.ObjectPathObject;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class MatchRuleTest {
    @Test
    public void testEscape() {
        assertEquals(escape("'"), "\\'");
        assertEquals(escape("\\"), "'\\'");
        assertEquals(escape(","), "','");
        assertEquals(escape("a"), "a");
    }

    @Test
    public void testSerialize() {
        MatchRule rule = new MatchRule();
        rule.setMessageType(MessageType.SIGNAL);
        rule.setSender(":blablauniqueid");
        rule.setInterfaceName("at.yawk.MyInterface");
        rule.setMember("MySignal");
        rule.setPath(ObjectPathObject.create("/at/yawk/MyInterface"));
        rule.setPathNamespace(ObjectPathObject.create("/at/yawk"));
        rule.setDestination("at.yawk.MyDestination");
        Map<Integer, String> arguments = new HashMap<>();
        arguments.put(2, "myarg");
        rule.setArguments(arguments);
        Map<Integer, ObjectPathObject> argumentPaths = new HashMap<>();
        argumentPaths.put(1, ObjectPathObject.create("/at/yawk"));
        rule.setArgumentPaths(argumentPaths);
        rule.setArg0Namespace(ObjectPathObject.create("/at"));
        rule.setEavesdrop(true);

        assertEquals(
                rule.serialize(),
                "type=signal,sender=:blablauniqueid,interface=at.yawk.MyInterface,member=MySignal," +
                "path=/at/yawk/MyInterface,path_namespace=/at/yawk,destination=at.yawk.MyDestination,arg2=myarg," +
                "arg1path=/at/yawk,arg0Namespace=/at,eavesdrop=true"
        );
    }

    static String escape(String s) {
        StringBuilder output = new StringBuilder();
        MatchRule.escape(s, output);
        return output.toString();
    }
}