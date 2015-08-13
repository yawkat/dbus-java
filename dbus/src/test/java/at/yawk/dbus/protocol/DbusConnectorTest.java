package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
@Slf4j
public class DbusConnectorTest {
    @Test
    public void testDefault() throws Exception {
        DbusChannel channel = new DbusConnector().connectSystem();

        DbusMessage message = new DbusMessage();

        MessageHeader header = new MessageHeader();
        header.setMessageType(MessageType.METHOD_CALL);
        header.addHeader(HeaderField.PATH, ObjectPathObject.create("/org/freedesktop/UPower/devices/DisplayDevice"));
        header.addHeader(HeaderField.DESTINATION, BasicObject.createString("org.freedesktop.UPower"));
        header.addHeader(HeaderField.MEMBER, BasicObject.createString("org.freedesktop.DBus.Properties.Get"));
        message.setHeader(header);

        MessageBody body = new MessageBody();
        body.add(BasicObject.createString("org.freedesktop.UPower.Device"));
        body.add(BasicObject.createString("State"));
        message.setBody(body);

        channel.write(message);

        channel.closeStage().toCompletableFuture().get();
    }
}