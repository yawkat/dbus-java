/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.request;

import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public interface Request {
    String getBus();

    MessageType getType();

    ObjectPathObject getObjectPath();

    StringObject getInterfaceName();

    StringObject getMember();

    @Nullable
    StringObject getDestination();

    List<DbusObject> getArguments();
}
