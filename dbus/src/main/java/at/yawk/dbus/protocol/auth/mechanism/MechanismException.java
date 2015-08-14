package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.command.Command;
import at.yawk.dbus.protocol.auth.command.Error;
import at.yawk.dbus.protocol.auth.command.Rejected;

/**
 * Exception in a mechanism that prevents this mechanism from being used but still allows other authentication
 * attempts.
 *
 * @author yawkat
 */
public class MechanismException extends RuntimeException {
    public MechanismException(String message) {
        super(message);
    }

    public MechanismException(at.yawk.dbus.protocol.auth.command.Error error) {
        this(error.getMessage());
    }

    public MechanismException(at.yawk.dbus.protocol.auth.command.Rejected error) {
        this("Accepted mechanisms: " + error.getMechanisms());
    }

    public static void handleCommand(Command cmd) {
        if (cmd instanceof at.yawk.dbus.protocol.auth.command.Error) {
            throw new MechanismException((Error) cmd);
        }
        if (cmd instanceof at.yawk.dbus.protocol.auth.command.Rejected) {
            throw new MechanismException((Rejected) cmd);
        }
    }
}
