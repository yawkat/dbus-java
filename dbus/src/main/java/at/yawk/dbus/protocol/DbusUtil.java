package at.yawk.dbus.protocol;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class DbusUtil {
    /**
     * Parse a UUID without dashes.
     */
    public static UUID parseUuid(String hex) {
        ByteBuffer bytes = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(hex));
        return new UUID(bytes.getLong(), bytes.getLong());
    }

    /**
     * Print a UUID without dashes.
     */
    public static String printUuid(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    /**
     * @return The stdout of the command.
     */
    public static String callCommand(String... command) throws Exception {
        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();
        if (process.waitFor() != 0) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.warn("Process ERR: {}", line);
                }
            }
            throw new IOException("Non-null exit status");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = process.getInputStream().read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        return bos.toString();
    }
}
