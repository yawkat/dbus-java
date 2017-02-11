/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.auth.AuthClient;
import at.yawk.dbus.protocol.auth.mechanism.AuthMechanism;
import at.yawk.dbus.protocol.auth.mechanism.ExternalAuthMechanism;
import at.yawk.dbus.protocol.codec.DbusMainProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CompletionStage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class DbusConnector implements Closeable {
    private final Bootstrap bootstrap;
    /**
     * The consumer to use for initial messages.
     */
    @Setter private MessageConsumer initialConsumer = MessageConsumer.DISCARD;
    @Setter private AuthMechanism authMechanism;

    private EpollEventLoopGroup epollEventLoopGroup = null;
    private NioEventLoopGroup nioEventLoopGroup = null;
    
    public DbusConnector() {
        bootstrap = new Bootstrap();
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.config().setAutoRead(false);
            }
        });
    }

    /**
     * Connect to the dbus server at the given {@link SocketAddress}.
     */
    public DbusChannel connect(SocketAddress address) throws Exception {
        Bootstrap localBootstrap = bootstrap.clone();

        synchronized (bootstrap) {
            if (address instanceof DomainSocketAddress) {
                if(epollEventLoopGroup == null) {
                    epollEventLoopGroup = new EpollEventLoopGroup();
                }
                localBootstrap.group(epollEventLoopGroup);
                localBootstrap.channelFactory(EpollDomainSocketChannel::new);
            } else {
                if(nioEventLoopGroup == null) {
                    nioEventLoopGroup = new NioEventLoopGroup();
                }
                localBootstrap.group(nioEventLoopGroup);
                localBootstrap.channelFactory(NioSocketChannel::new);
            }
        }

        Channel channel = localBootstrap.connect(address).sync().channel();

        AuthClient authClient = new AuthClient();
        if (LoggingInboundAdapter.isEnabled()) {
            channel.pipeline().addLast(new LoggingInboundAdapter());
        }

        channel.pipeline().addLast("auth", authClient);
        channel.config().setAutoRead(true);
        log.trace("Pipeline is now {}", channel.pipeline());

        // I really don't get why dbus does this
        channel.write(Unpooled.wrappedBuffer(new byte[]{ 0 }));

        if (authMechanism == null) {
            authMechanism = new ExternalAuthMechanism();
        }
        CompletionStage<?> completionPromise = authClient.startAuth(channel, authMechanism);

        SwappableMessageConsumer swappableConsumer = new SwappableMessageConsumer(initialConsumer);
        completionPromise.toCompletableFuture().thenRun(() -> {
            channel.pipeline().replace("auth", "main", new DbusMainProtocol(swappableConsumer));
            log.trace("Pipeline is now {}", channel.pipeline());
        }).get();

        DbusChannelImpl dbusChannel = new DbusChannelImpl(channel, swappableConsumer);

        dbusChannel.write(MessageFactory.methodCall(
                "/",
                "org.freedesktop.DBus",
                "org.freedesktop.DBus",
                "Hello"
        ));

        return dbusChannel;
    }

    public DbusChannel connect(DbusAddress address) throws Exception {
        log.info("Connecting to dbus server at {}", address);

        switch (address.getProtocol()) {
        case "unix":
            if (address.hasProperty("path")) {
                return connect(new DomainSocketAddress(address.getProperty("path")));
            } else if (address.hasProperty("abstract")) {
                return connect(new DomainSocketAddress('\0' + address.getProperty("abstract")));
            } else {
                throw new IllegalArgumentException("Neither path nor abstract given in dbus url");
            }
        case "tcp":
            String host = address.getProperty("host");
            int port = Integer.parseInt(address.getProperty("port"));
            return connect(new InetSocketAddress(host, port));
        default:
            throw new UnsupportedOperationException("Unsupported protocol " + address.getProtocol());
        }
    }

    public DbusChannel connectSession() throws Exception {
        String address = System.getenv("DBUS_SESSION_BUS_ADDRESS");
        if (address == null) {
            String machineId = new String(Files.readAllBytes(Paths.get("/etc/machine-id"))).trim();
            String response = DbusUtil.callCommand("dbus-launch", "--autolaunch", machineId);
            Properties properties = new Properties();
            properties.load(new StringReader(response));
            address = properties.getProperty("DBUS_SESSION_BUS_ADDRESS");
        }
        return connect(DbusAddress.parse(address));
    }

    public DbusChannel connectSystem() throws Exception {
        // this is the default system socket location defined in dbus
        return connect(DbusAddress.fromUnixSocket(Paths.get("/run/dbus/system_bus_socket")));
    }

    @Override
    public void close() throws IOException {
        if(epollEventLoopGroup != null) {
            epollEventLoopGroup.shutdownGracefully();
        }
        if(nioEventLoopGroup != null) {
            nioEventLoopGroup.shutdownGracefully();
        }
    }
}
