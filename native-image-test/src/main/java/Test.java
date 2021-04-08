import at.yawk.dbus.client.DbusClient;
import at.yawk.dbus.client.annotation.*;

import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        DbusClient client = new DbusClient();
        client.connectSystem();
        TestItf implement = client.implement(TestItf.class);
        List<String> systemNames = implement.listNamesSystem();
        if (systemNames.size() <= 3) {
            throw new AssertionError();
        }
        System.exit(0);
    }

    @Interface("org.freedesktop.DBus")
    @Destination("org.freedesktop.DBus")
    @ObjectPath("/")
    interface TestItf {
        @SystemBus
        @Call
        @Member("ListNames")
        List<String> listNamesSystem();
    }
}
