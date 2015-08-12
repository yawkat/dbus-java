package at.yawk.dbus.protocol;

import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public enum MessageType {
    METHOD_CALL(1),
    METHOD_RETURN(2),
    ERROR(3),
    SIGNAL(4),;

    private static final MessageType[] BY_ID;

    @Getter private final byte id;

    MessageType(int id) {
        this.id = (byte) id;
    }

    @Nullable
    public static MessageType byId(byte id) {
        return ((id >= 0) && (id < BY_ID.length)) ? BY_ID[id] : null;
    }

    static {
        int maxId = Arrays.stream(values()).mapToInt(MessageType::getId).max().getAsInt();
        BY_ID = new MessageType[maxId + 1];
        for (MessageType messageType : values()) {
            BY_ID[messageType.getId()] = messageType;
        }
    }
}
