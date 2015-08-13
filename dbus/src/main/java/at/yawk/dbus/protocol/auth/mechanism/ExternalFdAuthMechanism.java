package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.DbusUtil;
import at.yawk.dbus.protocol.auth.AuthChannel;
import at.yawk.dbus.protocol.auth.UnexpectedCommandException;
import at.yawk.dbus.protocol.auth.command.Auth;
import at.yawk.dbus.protocol.auth.command.Begin;
import at.yawk.dbus.protocol.auth.command.Ok;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public class ExternalFdAuthMechanism implements AuthMechanism {
    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        String uid = DbusUtil.callCommand("id", "-u").trim();
        assert uid.matches("\\d+");
        return channel.send(new Auth("EXTERNAL", uid.getBytes())).thenAccept(cmd -> {
            if (cmd instanceof Ok) {
                channel.send(new Begin());
            } else if (cmd instanceof at.yawk.dbus.protocol.auth.command.Error) {
                throw new MechanismException((at.yawk.dbus.protocol.auth.command.Error) cmd);
            } else {
                throw new UnexpectedCommandException(cmd);
            }
        });
    }
}
