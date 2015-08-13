package at.yawk.dbus.protocol;

import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class DbusConnectorTest {
    @Test
    public void testDefault() throws Exception {
        DbusConnector connector = new DbusConnector();
        connector.connectSystem();
        connector.getChannel().closeFuture().sync();
    }
}