/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.error;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic exception when an unhandled exception occurs in a request.
 *
 * @author yawkat
 */
public class RemoteException extends Exception {
    public RemoteException(String message, List<DbusObject> body) {
        super(message + ": " + toStringRoot(body));
    }

    private static String toStringRoot(List<DbusObject> objects) {
        if (objects.size() == 1) {
            return toString(objects.get(0));
        } else {
            return toString(objects);
        }
    }

    private static String toString(List<DbusObject> objects) {
        return objects.stream()
                .map(RemoteException::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String toString(DbusObject object) {
        TypeDefinition type = object.getType();
        if (type instanceof BasicType) {
            if (((BasicType) type).isStringLike()) {
                return object.stringValue();
            } else if (((BasicType) type).isInteger()) {
                return String.valueOf(object.longValue());
            } else { // double
                return String.valueOf(object.doubleValue());
            }
        } else if (type instanceof StructTypeDefinition ||
                   type instanceof ArrayTypeDefinition) {
            return toString(object.getValues());
        } else if (type instanceof DictTypeDefinition) {
            return object.getEntries().entrySet().stream()
                    .map(e -> toString(e.getKey()) + "=" + toString(e.getValue()))
                    .collect(Collectors.joining(", ", "{", "}"));
        } else {
            return object.toString();
        }
    }
}
