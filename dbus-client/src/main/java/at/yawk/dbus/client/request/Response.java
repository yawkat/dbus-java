package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.object.DbusObject;
import java.util.List;
import lombok.Getter;

/**
 * @author yawkat
 */
@Getter
public class Response {
    private final List<DbusObject> reply;
    private final String error;

    Response(List<DbusObject> reply, String error) {
        this.reply = reply;
        this.error = error;
    }

    public static Response success(List<DbusObject> reply) {
        return new Response(reply, null);
    }

    public static Response error(String error) {
        return new Response(null, error);
    }
}
