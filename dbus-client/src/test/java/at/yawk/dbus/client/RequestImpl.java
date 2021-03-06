/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client;

import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author yawkat
 */
@Setter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
class RequestImpl implements Request {
    String bus;
    MessageType type;
    String objectPath;
    String interfaceName;
    String member;
    String destination;
    List<DbusObject> arguments = Collections.emptyList();

    @SneakyThrows
    public static boolean requestsEqual(Request a, Request b) {
        for (Method method : Request.class.getDeclaredMethods()) {
            if (!Objects.equals(method.invoke(a), method.invoke(b))) {
                return false;
            }
        }
        return true;
    }

    @SneakyThrows
    public static String requestToString(Request request) {
        StringBuilder builder = new StringBuilder("Request{");
        for (Method method : Request.class.getDeclaredMethods()) {
            builder.append(method.getName()).append('=')
                    .append(method.invoke(request))
                    .append(' ');
        }
        builder.setLength(builder.length() - 1);
        return builder.append('}').toString();
    }

    public RequestImpl arguments(DbusObject... arguments) {
        return arguments(Arrays.asList(arguments));
    }

    public RequestImpl arguments(List<DbusObject> arguments) {
        this.arguments = arguments;
        return this;
    }

    @Override
    public String getBus() {
        return bus;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public ObjectPathObject getObjectPath() {
        return ObjectPathObject.create(objectPath);
    }

    @Override
    public StringObject getInterfaceName() {
        return StringObject.create(interfaceName);
    }

    @Override
    public StringObject getMember() {
        return StringObject.create(member);
    }

    @Nullable
    @Override
    public StringObject getDestination() {
        return destination == null ? null : StringObject.create(destination);
    }

    @Override
    public List<DbusObject> getArguments() {
        return arguments;
    }
}
