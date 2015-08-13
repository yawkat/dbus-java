package at.yawk.dbus.protocol.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Channel properties used by the decoders
 *
 * @author yawkat
 */
interface Local {
    String PREFIX = Local.class.getName() + '.';

    /**
     * The last message serial.
     */
    AttributeKey<Integer> LAST_SERIAL = AttributeKey.newInstance(PREFIX + "serial");

    /**
     * Generate a serial for the given channel.
     */
    static int generateSerial(ChannelHandlerContext ctx) {
        int serial;Attribute<Integer> attr = ctx.attr(LAST_SERIAL);
        Integer lastSerial;
        do {
            lastSerial = attr.get();
            serial = (lastSerial == null ? 0 : lastSerial) + 1;
            if (serial == 0) { serial = 1; }
        } while (attr.compareAndSet(lastSerial, serial));
        return serial;
    }
}
