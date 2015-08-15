package at.yawk.dbus.databind.request;

import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public interface Request {
    MessageType getType();

    ObjectPathObject getObjectPath();

    String getInterfaceName();

    String getMember();

    @Nullable
    String getDestination();

    List<DbusObject> getArguments();
}
