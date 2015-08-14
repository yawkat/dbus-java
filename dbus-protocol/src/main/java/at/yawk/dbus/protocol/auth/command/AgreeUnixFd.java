package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AgreeUnixFd extends Command {
    public static final String NAME = "AGREE_UNIX_FD";

    public AgreeUnixFd() {
        super(AuthDirection.FROM_SERVER, NAME);
    }

    public static AgreeUnixFd parse(List<String> args) {
        if (!args.isEmpty()) { throw new IllegalArgumentException("No arguments expected"); }
        return new AgreeUnixFd();
    }
}
