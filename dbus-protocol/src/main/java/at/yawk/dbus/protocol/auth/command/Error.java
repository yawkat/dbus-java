package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Error extends Command {
    public static final String NAME = "ERROR";
    private final String message;

    public Error(String message) {
        super(null, NAME, message);
        this.message = message;
    }

    public static Error parse(List<String> args) {
        return new Error(String.join(" ", args));
    }
}
