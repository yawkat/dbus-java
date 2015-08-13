package at.yawk.dbus.protocol.auth.command;

import java.util.List;

/**
 * @author yawkat
 */
public class Cancel extends Command {
    public static final String NAME = "CANCEL";

    public Cancel() {
        super(AuthDirection.FROM_CLIENT, NAME);
    }

    public static Cancel parse(List<String> args) {
        if (!args.isEmpty()) { throw new IllegalArgumentException("No arguments expected"); }
        return new Cancel();
    }
}
