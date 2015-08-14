package at.yawk.dbus.protocol;

import java.nio.file.Paths;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author yawkat
 */
public class DbusAddressTest {
    @Test
    public void testParse() {
        assertEquals(
                DbusAddress.parse("unix:path=/test"),
                DbusAddress.fromUnixSocket(Paths.get("/test"))
        );
    }

    @Test
    public void testToString() {
        assertEquals(
                DbusAddress.fromUnixSocket(Paths.get("/test")).toString(),
                "unix:path=/test"
        );
    }

    @Test
    public void testEscape() {
        assertEquals(DbusAddress.escape(" "), "%20");
    }

    @Test
    public void testUnescape() {
        assertEquals(DbusAddress.unescape("%20"), " ");
    }
}