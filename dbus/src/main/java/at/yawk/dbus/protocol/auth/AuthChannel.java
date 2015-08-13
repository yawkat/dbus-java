package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.Command;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public interface AuthChannel {
    CompletionStage<Command> send(Command command);
}
