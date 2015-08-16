package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import at.yawk.dbus.databind.annotation.Primitive;
import at.yawk.dbus.protocol.type.BasicType;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
    }

    @Test(timeOut = 5000L) // timeout for netty issues
    public void test() throws Exception {
        client.connectSystem();
        client.connectSession();

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

    @Test(timeOut = 5000L)
    public void testSignal() throws Exception {
        client.connectSession();

        TestSignal itf = client.implement(TestSignal.class);

        AtomicInteger callCount = new AtomicInteger();
        Runnable listener = callCount::incrementAndGet;
        itf.onNameAcquired(listener);

        itf.requestName("at.yawk.dbus.SignalTestName", 3);
        Thread.sleep(100);

        Assert.assertTrue(callCount.get() >= 1);
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

    @SessionBus
    @Interface("org.freedesktop.DBus")
    @Destination("org.freedesktop.DBus")
    @ObjectPath("/")
    interface TestSignal {
        @Listener
        @Member("NameAcquired")
        @ObjectPath("/org/freedesktop/DBus")
        void onNameAcquired(Runnable listener);

        @Call
        @Member("RequestName")
        @Primitive(BasicType.UINT32)
        int requestName(String name, @Primitive(BasicType.UINT32) int flags);
    }
}