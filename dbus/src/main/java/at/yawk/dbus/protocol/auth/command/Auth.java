package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Auth extends Command {
    public static final String NAME = "AUTH";

    @Nullable private final String mechanism;
    @Nullable private final byte[] initialResponse;

    public Auth(@Nullable String mechanism, @Nullable byte[] initialResponse) {
        super(AuthDirection.FROM_CLIENT,
              NAME,
              mechanism,
              initialResponse == null ? null : DatatypeConverter.printHexBinary(initialResponse));
        if (mechanism == null && initialResponse != null) {
            throw new IllegalArgumentException("initialResponse requires mechanism");
        }
        this.mechanism = mechanism;
        this.initialResponse = initialResponse;
    }

    public Auth() {
        this(null, null);
    }

    public static Auth parse(List<String> args) {
        if (args.size() > 2) { throw new IllegalArgumentException("Too many arguments"); }
        return new Auth(args.isEmpty() ? null : args.get(0),
                        args.size() <= 1 ? null : DatatypeConverter.parseHexBinary(args.get(1)));
    }
}
