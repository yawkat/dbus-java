/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
