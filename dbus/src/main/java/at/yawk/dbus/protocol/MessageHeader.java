package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.DbusObject;
import java.nio.ByteOrder;
import java.util.Map;
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
    private Map<HeaderField, DbusObject> headers;
}
