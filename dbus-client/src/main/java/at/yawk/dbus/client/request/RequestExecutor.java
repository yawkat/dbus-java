/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.request;

import at.yawk.dbus.client.error.RemoteException;
import at.yawk.dbus.protocol.MatchRule;
import at.yawk.dbus.protocol.object.DbusObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author yawkat
 */
public interface RequestExecutor {
    /**
     * Execute a (blocking) request.
     */
    Response execute(Request request) throws Exception;

    /**
     * Execute a (blocking) request.
     */
    Response execute(Request request, long timeout, TimeUnit unit) throws Exception;

    /**
     * Add a listener for messages of the given rule.
     *
     * @return A runnable that, when called, will unregister this listener.
     */
    Runnable listen(String bus, MatchRule rule, Consumer<List<DbusObject>> listener) throws RemoteException;
}
