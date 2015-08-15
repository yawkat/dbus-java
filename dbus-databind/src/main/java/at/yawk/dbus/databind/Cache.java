package at.yawk.dbus.databind;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * @author yawkat
 */
class Cache<K, V> {
    private final Map<K, V> entries = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    V computeIfAbsent(K key, Function<K, V> factory) {
        lock.readLock().lock();
        try {
            V v = entries.get(key);
            if (v != null) { return v; }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            // recheck
            V v = entries.get(key);
            if (v == null) {
                v = factory.apply(key);
                entries.put(key, Objects.requireNonNull(v));
            }
            return v;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
