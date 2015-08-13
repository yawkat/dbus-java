package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.type.BasicType;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public enum HeaderField {
    PATH(1, BasicType.OBJECT_PATH),
    INTERFACE(2, BasicType.STRING),
    MEMBER(3, BasicType.STRING),
    ERROR_NAME(4, BasicType.STRING),
    REPLY_SERIAL(5, BasicType.UINT32),
    DESTINATION(6, BasicType.STRING),
    SENDER(7, BasicType.STRING),
    SIGNATURE(8, BasicType.SIGNATURE),
    UNIX_FD_COUNT(9, BasicType.UINT32),;

    private static final HeaderField[] BY_ID;

    @Getter private final byte id;
    @Getter private final TypeDefinition type;

    HeaderField(int id, TypeDefinition type) {
        this.id = (byte) id;
        this.type = type;
    }

    @Nullable
    public static HeaderField byId(byte id) {
        return ((id >= 0) && (id < BY_ID.length)) ? BY_ID[id] : null;
    }

    static {
        int maxId = Arrays.stream(values()).mapToInt(HeaderField::getId).max().getAsInt();
        BY_ID = new HeaderField[maxId + 1];
        for (HeaderField field : values()) {
            BY_ID[field.getId()] = field;
        }
    }
}
