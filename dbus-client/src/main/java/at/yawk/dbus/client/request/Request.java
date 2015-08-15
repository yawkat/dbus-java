package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public interface Request {
    String getBus();

    MessageType getType();

    ObjectPathObject getObjectPath();

    StringObject getInterfaceName();

    StringObject getMember();

    @Nullable
    StringObject getDestination();

    List<DbusObject> getArguments();
}
