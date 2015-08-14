package at.yawk.dbus.protocol.auth;

import at.yawk.dbus.protocol.auth.command.Command;

/**
 * @author yawkat
 */
public class UnexpectedCommandException extends RuntimeException {
    public UnexpectedCommandException(Command command) {
        super(command.toString());
    }
}
