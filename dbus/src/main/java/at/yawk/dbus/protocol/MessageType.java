package at.yawk.dbus.protocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public enum MessageType {
    METHOD_CALL(1, HeaderField.PATH, HeaderField.MEMBER),
    METHOD_RETURN(2, HeaderField.REPLY_SERIAL),
    ERROR(3, HeaderField.REPLY_SERIAL, HeaderField.ERROR_NAME),
    SIGNAL(4, HeaderField.PATH, HeaderField.MEMBER, HeaderField.INTERFACE);

    private static final MessageType[] BY_ID;

    @Getter private final byte id;
    @Getter private final Set<HeaderField> requiredHeaders;

    MessageType(int id, HeaderField... requiredHeaders) {
        this.id = (byte) id;
        this.requiredHeaders = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(requiredHeaders)));
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
