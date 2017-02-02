/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class NettyBootSpeedTest {
    @Test
    public void testBoot() throws InterruptedException {
        long start = System.currentTimeMillis();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.handler(new ChannelHandlerAdapter());

        long mid = System.currentTimeMillis();

        bootstrap.bind(10000).await();
        bootstrap.connect("localhost", 10000).await();

        long end = System.currentTimeMillis();
        System.out.println("Setup took " + (mid - start) + " ms");
        System.out.println("Boot took " + (end - start) + " ms");
    }
}
