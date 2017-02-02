/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.Type;

/**
 * @author yawkat
 */
public interface BinderFactoryContext {
    /**
     * Get a binder that may encode and decode the given type.
     */
    Binder<?> getBinder(Type type);

    /**
     * Get a binder that may encode (not necessarily decode to!) the given type.
     */
    Binder<?> getDefaultEncodeBinder(Type type);

    /**
     * Get a binder that may decode (not necessarily encode to!) the given type.
     */
    Binder<?> getDefaultDecodeBinder(TypeDefinition typeDefinition);
}
