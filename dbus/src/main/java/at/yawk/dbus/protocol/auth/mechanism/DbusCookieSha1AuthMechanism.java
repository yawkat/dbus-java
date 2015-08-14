package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.AuthChannel;
import at.yawk.dbus.protocol.auth.command.Auth;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public class DbusCookieSha1AuthMechanism implements AuthMechanism {
    @Override
    public CompletionStage<?> startAuth(AuthChannel channel) throws Exception {
        String username = System.getProperty("user.name");
        return channel.send(new Auth("DBUS_COOKIE_SHA1", null)).thenAccept(cmd -> {
            // todo
            MechanismException.handleCommand(cmd);
        });
    }
}
