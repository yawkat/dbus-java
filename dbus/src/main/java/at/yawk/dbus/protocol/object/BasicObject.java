package at.yawk.dbus.protocol.object;

import at.yawk.dbus.protocol.type.BasicType;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * DbusObject extension for {@link BasicType}s.
 *
 * @author yawkat
 */
@Getter
@EqualsAndHashCode
@ToString
public abstract class BasicObject implements DbusObject {
    static final long MASK_BYTE = 0xffL;
    static final long MASK_SHORT = 0xffffL;
    static final long MASK_INT = 0xffffffffL;

    static final int BOOLEAN_TRUE = 1;
    static final int BOOLEAN_FALSE = 0;

    private final BasicType type;

    BasicObject(BasicType type) {
        this.type = type;
    }

    /**
     * Get this object as a signed number. The returned number will be signed: For example, a {@link BasicType#UINT16}
     * with value {@code 0xffff} will return a {@link Short} of value {@code -1}.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    public Number signedNumberValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return this object cast to byte.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public byte byteValue() {
        return (byte) longValue();
    }

    /**
     * Return this object cast to short.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public short shortValue() {
        return (short) longValue();
    }

    /**
     * Return this object cast to int.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public int intValue() {
        return (int) longValue();
    }

    /**
     * Return this object cast to long.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public long longValue() {
        return signedNumberValue().longValue();
    }

    /**
     * Return this object cast to double.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    @Override
    public double doubleValue() {
        return signedNumberValue().doubleValue();
    }

    /**
     * Get this object as a number. The returned may not be signed: For example, a {@link BasicType#UINT16}
     * with value {@code 0xffff} will return an {@link Integer} of value {@code 0xffff}.
     *
     * @throws UnsupportedOperationException if this is not numeric type.
     */
    public Number realNumberValue() throws UnsupportedOperationException {
        return signedNumberValue();
    }

    /**
     * @throws UnsupportedOperationException if this is not a {@link BasicType#BOOLEAN} type.
     */
    @Override
    public boolean booleanValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException if this is not a {@link BasicType#isStringLike() string-like} type.
     */
    @Override
    public String stringValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    ///// FACTORIES /////

    /**
     * Create a new boolean object.
     *
     * <b>Boolean objects are not numeric.</b>
     */
    public static BasicObject createBoolean(boolean value) {
        return new IntegerBasicObject(BasicType.BOOLEAN, value ? BOOLEAN_TRUE : BOOLEAN_FALSE);
    }

    public static BasicObject createByte(byte value) {
        return new IntegerBasicObject(BasicType.BYTE, value & MASK_BYTE);
    }

    public static BasicObject createInt16(short value) {
        return new IntegerBasicObject(BasicType.INT16, value & MASK_SHORT);
    }

    public static BasicObject createUint16(short value) {
        return new IntegerBasicObject(BasicType.UINT16, value & MASK_SHORT);
    }

    public static BasicObject createInt32(int value) {
        return new IntegerBasicObject(BasicType.INT32, value & MASK_INT);
    }

    public static BasicObject createUint32(int value) {
        return new IntegerBasicObject(BasicType.UINT32, value & MASK_INT);
    }

    public static BasicObject createUnixFd(int value) {
        return new IntegerBasicObject(BasicType.UNIX_FD, value & MASK_INT);
    }

    public static BasicObject createInt64(long value) {
        return new IntegerBasicObject(BasicType.INT64, value);
    }

    public static BasicObject createUint64(long value) {
        return new IntegerBasicObject(BasicType.UINT64, value);
    }

    public static BasicObject createDouble(double value) {
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        class DoubleBasicObject extends BasicObject {
            public DoubleBasicObject() {
                super(BasicType.DOUBLE);
            }

            @Override
            public void serialize(AlignableByteBuf buf) {
                buf.alignWrite(8);
                buf.writeDouble(value);
            }

            @Override
            public Number signedNumberValue() throws UnsupportedOperationException {
                return value;
            }

            @Override
            public Number realNumberValue() throws UnsupportedOperationException {
                return value;
            }

            @Override
            public double doubleValue() {
                return value;
            }
        }
        return new DoubleBasicObject();
    }

    /**
     * Create a new dbus object of type {@link BasicType#STRING} with the given string value.
     */
    public static BasicObject createString(String value) {
        return createString(BasicType.STRING, value);
    }

    /**
     * Create a new dbus object of the given type with the given string value.
     *
     * @throws IllegalArgumentException if the given type is not {@link BasicType#isStringLike() string-like}.
     * @throws IllegalArgumentException if the value contains a NUL character.
     */
    public static BasicObject createString(BasicType type, String value) {
        if (!type.isStringLike()) {
            throw new IllegalArgumentException("Type is not string-like");
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (byte e : bytes) {
            if (e == 0) {
                throw new IllegalArgumentException("Value may not contain NUL");
            }
        }
        return new StringBasicObject(type, Unpooled.wrappedBuffer(bytes), value);
    }

    public static BasicObject deserialize(BasicType type, AlignableByteBuf buf) {
        if (type.isStringLike()) {
            return StringBasicObject.deserialize0(type, buf);
        } else if (type == BasicType.DOUBLE) {
            buf.alignRead(8);
            return createDouble(buf.readDouble());
        } else {
            return IntegerBasicObject.deserialize0(type, buf);
        }
    }
}
