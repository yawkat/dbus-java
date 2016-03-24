package at.yawk.dbus.client.request;

import java.io.EOFException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.jcip.annotations.GuardedBy;

/**
 * @author yawkat
 */
class ChannelRequestStateHolder<T> {
    @GuardedBy("this")
    private final Map<Integer, CompletableFuture<T>> pendingFutures = new HashMap<>();
    @GuardedBy("this")
    private boolean running = true;

    /**
     * Close all pending futures and make this state holder invalid.
     */
    private synchronized void onHangup() {
        if (running) {
            running = false;
            pendingFutures.forEach((k, v) -> v.completeExceptionally(new EOFException()));
            pendingFutures.clear();
        }
    }

    /**
     * Create a new completable future for the given id.
     */
    CompletableFuture<T> registerPending(int serial) {
        CompletableFuture<T> future = new CompletableFuture<>();
        synchronized (this) {
            if (!running) { throw new IllegalStateException("Already hung up"); }
            CompletableFuture<T> oldFuture = pendingFutures.putIfAbsent(serial, future);
            if (oldFuture != null) { throw new IllegalArgumentException("Duplicate serial " + serial); }
        }
        return future;
    }

    /**
     * Complete a request state for the given id if it's still pending.
     */
    synchronized void complete(int serial, T value) {
        CompletableFuture<T> future = pendingFutures.remove(serial);
        if (future != null) {
            future.complete(value);
        }
    }

    /**
     * @return {@code true} if a request of the given id is pending.
     */
    synchronized boolean isPending(int serial) {
        return pendingFutures.containsKey(serial);
    }

    /**
     * Return a future listener that will invoke {@link #onHangup()} when called. This listener will not hold a strong
     * reference to this state holder.
     */
    Runnable createCleaner() {
        return new Cleaner(this);
    }

    private static final class Cleaner implements Runnable {
        private final Reference<ChannelRequestStateHolder> ref;

        private Cleaner(ChannelRequestStateHolder holder) {
            this.ref = new WeakReference<>(holder);
        }

        @Override
        public void run() {
            ChannelRequestStateHolder holder = ref.get();
            if (holder != null) {
                holder.onHangup();
                ref.clear();
            }
        }
    }
}
