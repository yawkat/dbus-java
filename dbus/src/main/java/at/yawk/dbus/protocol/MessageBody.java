package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.DbusObject;
import java.util.List;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MessageBody {
    private List<DbusObject> arguments;
}
