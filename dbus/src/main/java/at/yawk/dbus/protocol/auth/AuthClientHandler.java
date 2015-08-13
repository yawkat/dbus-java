package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.DbusUtil;
import at.yawk.dbus.protocol.auth.command.*;
import at.yawk.dbus.protocol.auth.command.Error;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
class AuthClientHandler extends SimpleChannelInboundHandler<Command> {
    private ChannelPromise completionFuture;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return msg instanceof Command;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Command msg) throws Exception {
        if (msg instanceof Data) {
            data(ctx.channel(), (Data) msg);
        } else if (msg instanceof Error) {
            error(ctx.channel(), (Error) msg);
        } else if (msg instanceof Ok) {
            ok(ctx.channel(), (Ok) msg);
        } else if (msg instanceof Rejected) {
            rejected(ctx.channel(), (Rejected) msg);
        } else if (msg instanceof AgreeUnixFd) {
            agreeUnixFd(ctx.channel(), (AgreeUnixFd) msg);
        } else {
            throw new AuthenticationException("Unhandled message " + msg);
        }
    }

    public ChannelFuture startAuth(Channel channel) throws Exception {
        String uid = DbusUtil.callCommand("id", "-u").trim();
        assert uid.matches("\\d+");

        channel.write(Unpooled.wrappedBuffer(new byte[]{ 0 })); // lead with single 0 byte
        channel.writeAndFlush(new Auth("EXTERNAL", uid.getBytes()));
        return completionFuture = channel.newPromise();
    }

    private void data(Channel channel, Data data) {
        throw new UnsupportedOperationException();
    }

    private void error(Channel channel, Error error) {
        throw new UnsupportedOperationException();
    }

    private void ok(Channel channel, Ok ok) {
        write(channel, new NegotiateUnixFd());
    }

    private void rejected(Channel channel, Rejected rejected) {
        throw new UnsupportedOperationException();
    }

    private void agreeUnixFd(Channel channel, AgreeUnixFd agreeUnixFd) {
        write(channel, new Begin());
        completionFuture.setSuccess();
    }

    private ChannelFuture write(Channel channel, Command msg) {
        return channel.writeAndFlush(msg, channel.voidPromise());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in channel", cause);
        completionFuture.setFailure(cause);
    }
}
