package at.yawk.dbus.protocol.type;

import at.yawk.dbus.protocol.object.AlignableByteBuf;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * @author yawkat
 */
public enum BasicType implements TypeDefinition {
    BYTE('y', 1),
    BOOLEAN('b', 1),
    INT16('n', 2),
    UINT16('q', 2),
    INT32('i', 4),
    UINT32('u', 4),
    INT64('x', 8),
    UINT64('t', 8),
    DOUBLE('d', 8),
    UNIX_FD('h', 4),
    STRING('s', Const.LENGTH_STRING_LIKE),
    OBJECT_PATH('o', Const.LENGTH_STRING_LIKE),
    SIGNATURE('g', Const.LENGTH_STRING_LIKE),;

    private static final BasicType[] TYPES_BY_CODE;

    @Getter private final char code;
    private final String codeString;
    /**
     * Fixed length or {@link BasicType.Const#LENGTH_STRING_LIKE}.
     */
    private final int length;

    BasicType(char code, int length) {
        this.code = code;
        this.codeString = String.valueOf(code);
        this.length = length;
    }

    static {
        int maxCode = Arrays.stream(values()).mapToInt(BasicType::getCode).max().getAsInt();
        TYPES_BY_CODE = new BasicType[maxCode + 1];
        for (BasicType type : values()) {
            TYPES_BY_CODE[type.code] = type;
        }
    }

    @Nullable
    public static BasicType byCode(char code) {
        return code < TYPES_BY_CODE.length ? TYPES_BY_CODE[code] : null;
    }

    @Override
    public String serialize() {
        return codeString;
    }

    @Override
    public BasicObject deserialize(AlignableByteBuf buf) {
        return BasicObject.deserialize(this, buf);
    }

    public boolean isStringLike() {
        return length == Const.LENGTH_STRING_LIKE;
    }

    /**
     * Get the fixed length of this type in bytes.
     *
     * @throws UnsupportedOperationException if this is a string-like type and does not have a fixed length.
     */
    public int getLength() throws UnsupportedOperationException {
        if (isStringLike()) { throw new UnsupportedOperationException(); }
        assert length >= 0;
        return length;
    }

    private static class Const {
        private static final int LENGTH_STRING_LIKE = -1;
    }
}
