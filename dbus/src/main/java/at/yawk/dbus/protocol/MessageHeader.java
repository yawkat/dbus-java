package at.yawk.dbus.protocol;

import java.nio.ByteOrder;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MessageHeader {
    private ByteOrder byteOrder;
    private MessageType messageType;
    private boolean noReplyExpected;
    private boolean noAutoStart;
    private boolean allowInteractiveAuthorization;
    private byte majorProtocolVersion;
    private long messageBodyLength;
    private int serial;
}
