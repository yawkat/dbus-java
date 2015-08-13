package at.yawk.dbus.protocol.auth.mechanism;

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
}
