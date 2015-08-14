package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Rejected extends Command {
    public static final String NAME = "REJECTED";
    private final List<String> mechanisms;

    public Rejected(List<String> mechanisms) {
        super(AuthDirection.FROM_SERVER, NAME, String.join(" ", mechanisms));
        this.mechanisms = mechanisms;
    }

    public static Rejected parse(List<String> args) {
        return new Rejected(args);
    }
}
