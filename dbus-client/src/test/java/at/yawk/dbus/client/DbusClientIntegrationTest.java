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
        client.connectSession();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
    }

    @Test(timeOut = 5000L) // timeout for netty issues
    public void test() {
        TestItf itf = client.implement(TestItf.class);

        // these will probably throw or something on failure

        List<String> systemNames = itf.listNamesSystem();
        System.out.println(systemNames);
        Assert.assertTrue(systemNames.size() > 3);

        List<String> sessionNames = itf.listNamesSession();
        System.out.println(sessionNames);
        Assert.assertTrue(sessionNames.size() > 3);

        Assert.assertNotEquals(systemNames, sessionNames);
    }

    @Interface("org.freedesktop.DBus")
    @Destination("org.freedesktop.DBus")
    @ObjectPath("/")
    interface TestItf {
        @SystemBus
        @Call
        @Member("ListNames")
        List<String> listNamesSystem();

        @SessionBus
        @Call
        @Member("ListNames")
        List<String> listNamesSession();
    }
}