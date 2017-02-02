/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
