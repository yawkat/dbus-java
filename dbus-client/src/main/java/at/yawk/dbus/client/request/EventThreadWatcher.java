package at.yawk.dbus.client.request;

/**
 * Helper class that makes sure requests don't block the event threads.
 *
 * @author yawkat
 */
class EventThreadWatcher {
    private final ThreadLocal<Boolean> inHotThread = new ThreadLocal<>();

    public void lock() {
        inHotThread.set(true);
    }

    public void unlock() {
        inHotThread.remove();
    }

    public void checkLock() {
        if (isLocked()) {
            throw new IllegalStateException("In hot thread, cannot block here!");
        }
    }

    public boolean isLocked() {
        return Boolean.TRUE.equals(inHotThread.get());
    }
}
