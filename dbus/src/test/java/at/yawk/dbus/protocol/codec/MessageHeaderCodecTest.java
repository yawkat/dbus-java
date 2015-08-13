package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.HeaderField;
import at.yawk.dbus.protocol.MessageHeader;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.SignatureObject;
import at.yawk.dbus.protocol.type.BasicType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class MessageHeaderCodecTest {
    @Test
    public void testEncodeDecode() throws Exception {
        MessageHeader inHeader = new MessageHeader();
        // defaults
        inHeader.setByteOrder(ByteOrder.BIG_ENDIAN);
        inHeader.setMajorProtocolVersion((byte) 1);
        inHeader.setSerial(1);

        inHeader.setMessageType(MessageType.METHOD_CALL);
        inHeader.setMessageBodyLength(5);
        inHeader.addHeader(HeaderField.PATH, ObjectPathObject.create("/org/freedesktop/UPower/devices/DisplayDevice"));
        inHeader.addHeader(HeaderField.DESTINATION, BasicObject.createString("org.freedesktop.UPower"));
        inHeader.addHeader(HeaderField.MEMBER, BasicObject.createString("org.freedesktop.DBus.Properties.Get"));
        inHeader.addHeader(HeaderField.SIGNATURE, SignatureObject.create(Arrays.asList(BasicType.UINT16,
                                                                                       BasicType.UINT32)));

        MessageHeaderCodec codec = new MessageHeaderCodec();
        ByteBuf buffer = Unpooled.buffer();
        codec.encode(null, inHeader, buffer);

        List<Object> out = new ArrayList<>();
        codec.decode(null, buffer, out);

        Assert.assertEquals(out.size(), 1);
        Assert.assertEquals(out.get(0), inHeader);
    }
}