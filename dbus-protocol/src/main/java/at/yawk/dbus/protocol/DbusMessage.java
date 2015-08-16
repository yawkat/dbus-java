package at.yawk.dbus.protocol;

import javax.annotation.Nullable;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class DbusMessage {
    private MessageHeader header;
    @Nullable private MessageBody body;
}
