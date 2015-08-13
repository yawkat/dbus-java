package at.yawk.dbus.protocol.auth.command;

import java.util.List;
import javax.xml.bind.DatatypeConverter;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class Data extends Command {
    public static final String NAME = "DATA";
    private final byte[] data;

    public Data(byte[] data) {
        super(null, NAME, DatatypeConverter.printHexBinary(data));
        this.data = data;
    }

    public static Data parse(List<String> args) {
        if (args.size() != 1) { throw new IllegalArgumentException("Expected exactly one argument"); }
        return new Data(DatatypeConverter.parseHexBinary(args.get(0)));
    }
}
