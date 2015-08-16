package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import at.yawk.dbus.databind.annotation.Primitive;
import at.yawk.dbus.protocol.type.BasicType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extended integration tests that use non-standard dbus interfaces.
 *
 * @author yawkat
 */
public class DbusClientExtendedIntegrationTest {
    private DbusClient client;

    @BeforeMethod
    public void setUp() throws Exception {
        client = new DbusClient();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
    }

    @Test(timeOut = 5000L, enabled = false)
    public void test() throws Exception {
        client.connectSystem();

        TestItf itf = client.implement(TestItf.class);
        System.out.println(itf.getConnectivity());
    }

    @SystemBus
    @Destination("org.freedesktop.NetworkManager")
    @ObjectPath("/org/freedesktop/NetworkManager")
    @Interface("org.freedesktop.NetworkManager")
    interface TestItf {
        @GetProperty
        @Member("Connectivity")
        @Primitive(BasicType.UINT32)
        int getConnectivity();
    }
}