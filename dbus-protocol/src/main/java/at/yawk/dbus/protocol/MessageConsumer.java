package at.yawk.dbus.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yawkat
 */
public interface MessageConsumer {
    MessageConsumer DISCARD = new MessageConsumer() {
        Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

        @Override
        public boolean requireAccept(MessageHeader header) {
            logger.trace("Discarding {}", header);
            return false;
        }

        @Override
        public void accept(DbusMessage message) {}
    };

    /**
     * Check if this consumer would like to decode the message with the given header. This is optional: if {@code
     * false} is returned, {@link #accept(DbusMessage)} may still be called.
     */
    boolean requireAccept(MessageHeader header);

    void accept(DbusMessage message);
}
