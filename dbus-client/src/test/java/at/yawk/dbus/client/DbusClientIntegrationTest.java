package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class DbusClientIntegrationTest {
    private DbusClient client;

    @BeforeMethod
    public void setUp() throws Exception {
        client = new DbusClient();
        client.connectSystem();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
    }

    @Test(timeOut = 5000L) // timeout for netty issues
    public void test() {
        // this will probably throw or something on failure
        List<String> names = client.implement(TestItf.class).listNames();
        Assert.assertTrue(names.size() > 3);
    }

    @SystemBus
    @Interface("org.freedesktop.DBus")
    @Destination("org.freedesktop.DBus")
    @ObjectPath("/")
    interface TestItf {
        @Call
        @Member("ListNames")
        List<String> listNames();
    }
}