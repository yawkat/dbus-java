/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client;

import at.yawk.dbus.client.error.RemoteException;
import at.yawk.dbus.client.request.ChannelRequestExecutor;
import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.client.request.RequestExecutor;
import at.yawk.dbus.client.request.Response;
import at.yawk.dbus.databind.DataBinder;
import at.yawk.dbus.protocol.DbusAddress;
import at.yawk.dbus.protocol.DbusChannel;
import at.yawk.dbus.protocol.DbusConnector;
import at.yawk.dbus.protocol.MatchRule;
import at.yawk.dbus.protocol.object.DbusObject;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import net.jcip.annotations.GuardedBy;

/**
 * @author yawkat
 */
public class DbusClient implements Closeable {
    @Getter private final DataBinder binder = new DataBinder();
    @Getter private final RmiFactory rootFactory = new RmiFactory(binder, new BusSelectingRequestExecutor());

    // lazily initialized - this is quite heavy (creates a netty Bootstrap) and not all paths use it
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final DbusConnector connector = new DbusConnector();

    @GuardedBy("busMapLock")
    private final Map<String, BusHolder> busMap = new HashMap<>();
    private final ReadWriteLock busMapLock = new ReentrantReadWriteLock();

    /**
     * Add a channel to this client with the given bus name. This channel will be closed with this client.
     */
    public void addChannel(String busName, DbusChannel channel) {
        busMapLock.writeLock().lock();
        try {
            busMap.put(busName, new BusHolder(channel, new ChannelRequestExecutor(channel)));
        } finally {
            busMapLock.writeLock().unlock();
        }
        // remove on close
        channel.closeStage().thenRun(() -> {
            busMapLock.writeLock().lock();
            try {
                busMap.remove(busName, channel);
            } finally {
                busMapLock.writeLock().unlock();
            }
        });
    }

    public void connect(String busName, DbusAddress address) throws Exception {
        addChannel(busName, getConnector().connect(address));
    }

    public void connectSystem() throws Exception {
        addChannel("system", getConnector().connectSystem());
    }

    public void connectSession() throws Exception {
        addChannel("session", getConnector().connectSession());
    }

    public <I> I implement(Class<I> interfaceClass) {
        return rootFactory.createRmiInstance(interfaceClass, null);
    }
    
    public <I> I implement(Class<I> interfaceClass, DBUSDestination destination) {
        return rootFactory.createRmiInstance(interfaceClass, destination);
    }

    @Override
    public void close() throws IOException {
        busMapLock.writeLock().lock();
        try {
            // this is a forced shutdown
            busMap.forEach((name, holder) -> holder.channel.close());
            busMap.clear();
        } finally {
            busMapLock.writeLock().unlock();
        }
    }

    @Value
    private static class BusHolder {
        private final DbusChannel channel;
        private final ChannelRequestExecutor executor;
    }

    private class BusSelectingRequestExecutor implements RequestExecutor {
        @Override
        public Response execute(Request request) throws Exception {
            return selectBus(request.getBus()).execute(request);
        }

        @Override
        public Response execute(Request request, long timeout, TimeUnit unit) throws Exception {
            return selectBus(request.getBus()).execute(request, timeout, unit);
        }

        @Override
        public Runnable listen(String bus, MatchRule rule, Consumer<List<DbusObject>> listener) throws RemoteException {
            return selectBus(bus).listen(bus, rule, listener);
        }

        private ChannelRequestExecutor selectBus(String busName) {
            Objects.requireNonNull(busName, "bus");

            BusHolder holder;
            busMapLock.readLock().lock();
            try {
                holder = busMap.get(busName);
            } finally {
                busMapLock.readLock().unlock();
            }
            if (holder == null) {
                throw new NoSuchElementException("Not connected to bus " + busName);
            }
            return holder.executor;
        }
    }
}
