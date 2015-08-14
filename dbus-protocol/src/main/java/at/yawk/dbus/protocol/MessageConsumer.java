package at.yawk.dbus.protocol;

/**
 * @author yawkat
 */
public interface MessageConsumer {
    MessageConsumer DISCARD = new MessageConsumer() {
        @Override
        public boolean requireAccept(MessageHeader header) {
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
