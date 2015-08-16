package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.DbusMessage;
import at.yawk.dbus.protocol.MatchRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;

/**
 * @author yawkat
 */
@Slf4j
class ListenerHolder {
    @GuardedBy("lock")
    private final Map<MatchRule, List<Consumer<DbusMessage>>> listeners = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * @return {@code true} if this was the first listener of this rule and the rule should be registered.
     */
    public boolean addListener(MatchRule rule, Consumer<DbusMessage> listener) {
        lock.writeLock().lock();
        try {
            List<Consumer<DbusMessage>> forRule = listeners.computeIfAbsent(rule, r -> new ArrayList<>());
            forRule.add(listener);
            return forRule.size() == 1;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return {@code true} if this was the last listener of this rule and the rule should be unregistered.
     */
    public boolean removeListener(MatchRule rule, Consumer<DbusMessage> listener) {
        lock.writeLock().lock();
        try {
            List<Consumer<DbusMessage>> consumers = listeners.get(rule);
            if (consumers != null &&
                consumers.remove(listener) &&
                consumers.isEmpty()) {

                listeners.remove(rule);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void post(DbusMessage message) {
        log.trace("recv {}", message);
        lock.readLock().lock();
        try {
            listeners.forEach((rule, listeners) -> {
                if (rule.matches(message)) {
                    for (Consumer<DbusMessage> listener : listeners) {
                        try {
                            listener.accept(message);
                        } catch (Throwable t) {
                            log.error("Exception in listener", t);
                        }
                    }
                }
            });
        } finally {
            lock.readLock().unlock();
        }
    }
}
