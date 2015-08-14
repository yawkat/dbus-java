package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.AuthChannel;
import at.yawk.dbus.protocol.auth.UnexpectedCommandException;
import at.yawk.dbus.protocol.auth.command.Auth;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Ok;
import at.yawk.dbus.protocol.auth.command.Rejected;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public class AnonymousAuthMechanism implements AuthMechanism {
    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        return channel.send(new Auth())
                .thenCompose(cmd -> {
                    if (cmd instanceof Rejected) {
                        return channel.send(new Auth("ANONYMOUS", null));
                    } else {
                        throw new UnexpectedCommandException(cmd);
                    }
                })
                .thenAccept(cmd -> {
                    MechanismException.handleCommand(cmd);
                    if (cmd instanceof Ok) {
                        channel.send(new Begin());
                    } else {
                        throw new UnexpectedCommandException(cmd);
                    }
                });
    }
}
