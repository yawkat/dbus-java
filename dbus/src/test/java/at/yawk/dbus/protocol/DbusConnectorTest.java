package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.auth.CommandCodec;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Error;
import at.yawk.dbus.protocol.auth.command.NegotiateUnixFd;
import at.yawk.dbus.protocol.auth.command.Ok;
import at.yawk.dbus.protocol.codec.DbusMainProtocol;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
@Slf4j
public class DbusConnectorTest {
    @Test//(enabled = false)
    public void testDefault() throws Exception {
        DbusChannel channel =
                new DbusConnector().connectSystem();//.connect(DbusAddress.fromUnixSocket(Paths.get("test")));

        DbusMessage message = new DbusMessage();

        MessageHeader header = new MessageHeader();
        header.setMessageType(MessageType.METHOD_CALL);
        header.addHeader(HeaderField.PATH, ObjectPathObject.create("/org/freedesktop/UPower/devices/DisplayDevice"));
        header.addHeader(HeaderField.DESTINATION, BasicObject.createString("org.freedesktop.UPower"));
        header.addHeader(HeaderField.INTERFACE, BasicObject.createString("org.freedesktop.DBus.Properties"));
        header.addHeader(HeaderField.MEMBER, BasicObject.createString("Get"));
        message.setHeader(header);

        MessageBody body = new MessageBody();
        body.add(BasicObject.createString("org.freedesktop.UPower.Device"));
        body.add(BasicObject.createString("State"));
        message.setBody(body);

        channel.write(message);

        channel.closeStage().toCompletableFuture().get();
    }

    @Test//(enabled = false)
    public void testServer() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(EpollServerDomainSocketChannel.class);
        bootstrap.group(new EpollEventLoopGroup());
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {

                ch.pipeline().addLast(new CommandCodec()).addLast(new SimpleChannelInboundHandler() {
                    @Override
                    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof NegotiateUnixFd) {
                            ch.writeAndFlush(new Error("error"));
                        }

                        if (msg instanceof Begin) {
                            ch.pipeline()
                                    .addLast(new LoggingInboundAdapter())
                                    .addLast(new DbusMainProtocol(new MessageConsumer() {
                                        @Override
                                        public boolean requireAccept(MessageHeader header) {
                                            return true;
                                        }

                                        @Override
                                        public void accept(DbusMessage message) {
                                            DbusMessage response = new DbusMessage();

                                            MessageHeader header = new MessageHeader();
                                            header.setMessageType(MessageType.ERROR);
                                            header.addHeader(HeaderField.REPLY_SERIAL, BasicObject.createUint32(
                                                    message.getHeader().getSerial()));
                                            //header.addHeader(HeaderField.SIGNATURE, SignatureObject.create(
                                            //        Collections.singletonList(BasicType.VARIANT)));
                                            header.addHeader(HeaderField.ERROR_NAME, BasicObject.createString(
                                                    "Error"));
                                            response.setHeader(header);

                                            MessageBody body = new MessageBody();
                                            //body.add(VariantObject.create(BasicObject.createString("testing!")));
                                            response.setBody(body);

                                            ch.writeAndFlush(response);
                                        }
                                    }));
                            ch.pipeline().remove((Class) getClass());
                            ch.pipeline().remove(CommandCodec.class);
                        }
                    }
                });
                ch.writeAndFlush(new Ok(UUID.randomUUID()));
            }
        });
        bootstrap.bind(new DomainSocketAddress(new File("test")));

        try {
            DbusUtil.callCommand(
                    ("dbus-send --address=unix:path=" + new File(".").getAbsolutePath() +
                     "/test --dest=org.freedesktop.UPower --print-reply " +
                     "/org/freedesktop/UPower/devices/DisplayDevice org.freedesktop.DBus.Properties.Get string:org" +
                     ".freedesktop.UPower.Device string:State")
                            .split(" "));
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeUnit.DAYS.sleep(1);
    }
}