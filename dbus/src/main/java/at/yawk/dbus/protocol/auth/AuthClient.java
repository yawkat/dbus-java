package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.AuthDirection;
import io.netty.channel.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author yawkat
 */
public class AuthClient extends ChannelHandlerAdapter {
    private final AuthClientHandler clientHandler = new AuthClientHandler();

    private final List<ChannelHandler> handlers = Arrays.asList(
            new CommandCodec(),
            new DirectionValidatorAdapter(AuthDirection.FROM_SERVER, AuthDirection.FROM_CLIENT),
            clientHandler
    );

    public ChannelFuture startAuth(Channel channel) throws Exception {
        return clientHandler.startAuth(channel);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        for (ChannelHandler handler : handlers) {
            ctx.pipeline().addBefore(ctx.invoker(), ctx.name(), null, handler);
        }
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        for (ChannelHandler handler : handlers) {
            ctx.pipeline().remove(handler);
        }
        super.handlerRemoved(ctx);
    }
}
