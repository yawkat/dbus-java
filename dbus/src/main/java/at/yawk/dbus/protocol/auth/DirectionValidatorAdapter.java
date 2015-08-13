package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.AuthDirection;
import at.yawk.dbus.protocol.auth.command.Command;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class DirectionValidatorAdapter extends ChannelHandlerAdapter {
    private final AuthDirection inbound;
    private final AuthDirection outbound;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        validate(msg, inbound);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        validate(msg, outbound);
        super.write(ctx, msg, promise);
    }

    private void validate(Object msg, AuthDirection expectedProtocol) {
        if (msg instanceof Command) {
            Command command = (Command) msg;
            if (command.getDirection() != null && command.getDirection() != expectedProtocol) {
                throw new DecoderException("Invalid command (wrong protocol direction): " + command);
            }
        }
    }
}
