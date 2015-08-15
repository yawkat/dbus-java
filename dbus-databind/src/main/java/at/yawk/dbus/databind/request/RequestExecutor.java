package at.yawk.dbus.databind.request;

/**
 * @author yawkat
 */
public interface RequestExecutor {
    Response execute(Request request) throws Exception;
}
