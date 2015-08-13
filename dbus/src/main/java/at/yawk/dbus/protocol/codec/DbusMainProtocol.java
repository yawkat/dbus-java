package at.yawk.dbus.protocol.codec;

import at.yawk.dbus.protocol.MessageConsumer;
import io.netty.channel.ChannelHandlerAppender;

/**
 * @author yawkat
 */
public class DbusMainProtocol extends ChannelHandlerAppender {
    public DbusMainProtocol(MessageConsumer consumer) {
        add(new ByteCollector());

        add(new MessageHeaderCodec());

        add(new BodyDecoder());
        add(new BodyEncoder());

        add(new IncomingMessageAdapter(consumer));
    }
}
