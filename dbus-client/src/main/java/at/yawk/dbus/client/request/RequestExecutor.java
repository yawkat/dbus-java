package at.yawk.dbus.client.request;

/**
 * @author yawkat
 */
public interface RequestExecutor {
    Response execute(Request request) throws Exception;
}
