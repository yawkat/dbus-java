package at.yawk.dbus.protocol.auth;

/**
 * @author yawkat
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
