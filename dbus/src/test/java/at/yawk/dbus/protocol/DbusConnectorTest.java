package at.yawk.dbus.protocol;

import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class DbusConnectorTest {
    @Test
    public void testDefault() throws Exception {
        DbusConnector connector = new DbusConnector();
        /*
        connector.setInitialConsumer(new MessageConsumer() {
            @Override
            public boolean requireAccept(MessageHeader header) {
                return false;
            }

            @Override
            public void accept(DbusMessage message) {

            }
        });
        */

        DbusChannel channel = connector.connectSystem();
        channel.closeStage().toCompletableFuture().get();
    }
}