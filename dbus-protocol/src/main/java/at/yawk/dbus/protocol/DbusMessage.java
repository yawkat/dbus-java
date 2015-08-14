package at.yawk.dbus.protocol;

import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class DbusMessage {
    private MessageHeader header;
    private MessageBody body;
}
