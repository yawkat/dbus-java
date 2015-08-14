package at.yawk.dbus.protocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class DbusAddress {
    private final String protocol;
    private final Map<String, String> properties;

    public static Builder builder(String protocol) {
        return new Builder(protocol);
    }

    public static DbusAddress fromUnixSocket(Path location) {
        return builder("unix")
                .property("path", location.toString())
                .build();
    }

    public static DbusAddress fromTcpAddress(InetSocketAddress address) {
        return builder("tcp")
                .property("host", address.getHostString())
                .property("port", Integer.toString(address.getPort()))
                .build();
    }

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

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        String value = properties.get(key);
        if (value == null) { throw new NoSuchElementException(key); }
        return value;
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

    public static class Builder {
        private final String protocol;
        private final Map<String, String> properties = new HashMap<>();

        Builder(String protocol) {
            this.protocol = protocol;
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public DbusAddress build() {
            return new DbusAddress(protocol, properties);
        }
    }
}
