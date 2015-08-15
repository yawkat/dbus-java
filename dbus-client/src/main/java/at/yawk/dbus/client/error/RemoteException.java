package at.yawk.dbus.client.error;

/**
 * Generic exception when an unhandled exception occurs in a request.
 *
 * @author yawkat
 */
public class RemoteException extends Exception {
    public RemoteException(String message) {
        super(message);
    }
}
