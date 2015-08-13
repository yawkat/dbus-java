package at.yawk.dbus.protocol.object;

/**
 * @author yawkat
 */
public class DeserializerException extends RuntimeException {
    public DeserializerException(String message) {
        super(message);
    }

    public DeserializerException(Throwable cause) {
        super(cause);
    }
}
