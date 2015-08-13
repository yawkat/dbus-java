package at.yawk.dbus.protocol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class DbusAddress {
    private final String protocol;
    private final Map<String, String> properties;

    public static DbusAddress parse(String repr) {
        int protocolPart = repr.indexOf(':');

        String protocol = repr.substring(0, protocolPart);
        Map<String, String> properties = new LinkedHashMap<>();

        int i = protocolPart + 1;
        while (true) {
            int valueSep = repr.indexOf('=', i);
            String key = repr.substring(i, valueSep);
            i = valueSep + 1;
            int entrySep = repr.indexOf(',', i);
            String value = repr.substring(i, entrySep == -1 ? repr.length() : entrySep);
            properties.put(key, value);

            if (entrySep == -1) { break; }
            i = entrySep + 1;
        }

        return new DbusAddress(protocol, properties);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(protocol).append(':');
        properties.forEach((k, v) -> builder.append(k).append('=').append(v).append(','));
        if (!properties.isEmpty()) {
            // remove trailing ,
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
