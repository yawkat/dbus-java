package at.yawk.dbus.protocol;

/**
 * @author yawkat
 */
public enum HeaderField {
    PATH(1),
    INTERFACE(2),
    MEMBER(3),
    ERROR_NAME(4),
    REPLY_SERIAL(5),
    SENDER(6),
    SIGNATURE(7),
    UNIX_FDS(8),;

    private final byte id;

    HeaderField(int id) {
        this.id = (byte) id;
    }
}
