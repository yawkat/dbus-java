package at.yawk.dbus.protocol.auth.command;

import java.util.List;

/**
 * @author yawkat
 */
public class Begin extends Command {
    public static final String NAME = "BEGIN";

    public Begin() {
        super(AuthDirection.FROM_CLIENT, NAME);
    }

    public static Begin parse(List<String> args) {
        if (!args.isEmpty()) { throw new IllegalArgumentException("No arguments expected"); }
        return new Begin();
    }
}
