/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.object.DbusObject;
import java.util.List;
import lombok.Getter;

/**
 * @author yawkat
 */
@Getter
public class Response {
    private final List<DbusObject> reply;
    /**
     * The error name or {@code null} if this is not an error.
     */
    private final String errorName;

    Response(List<DbusObject> reply, String errorName) {
        this.reply = reply;
        this.errorName = errorName;
    }

    public boolean isError() {
        return errorName != null;
    }

    public static Response success(List<DbusObject> reply) {
        return new Response(reply, null);
    }

    public static Response error(String error, List<DbusObject> reply) {
        return new Response(reply, error);
    }
}
