package at.yawk.dbus.protocol;

import java.io.Closeable;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public interface DbusChannel extends Closeable {
    void setMessageConsumer(MessageConsumer consumer);

    /**
     * Send the given message to this channel.
     */
    void write(DbusMessage message);

    /**
     * Gracefully disconnect this channel.
     */
    void disconnect();

    /**
     * Forcibly close this channel.
     */
    @Override
    void close();

    /**
     * Get a {@link CompletionStage} that completes when this channel is closed.
     */
    CompletionStage<?> closeStage();
}
