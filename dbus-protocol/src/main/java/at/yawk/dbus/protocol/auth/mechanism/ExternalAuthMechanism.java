package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.DbusUtil;
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
public class ExternalAuthMechanism implements AuthMechanism {
    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        String uid = DbusUtil.callCommand("id", "-u").trim();
        assert uid.matches("\\d+");
        return channel.send(new Auth("EXTERNAL", uid.getBytes())).thenAccept(cmd -> {
            MechanismException.handleCommand(cmd);
            if (cmd instanceof Ok) {
                channel.send(new Begin());
            } else {
                throw new UnexpectedCommandException(cmd);
            }
        });
    }
}
