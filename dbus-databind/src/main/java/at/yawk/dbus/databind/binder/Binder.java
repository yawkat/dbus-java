/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind.binder;

import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.util.function.Function;

/**
 * @author yawkat
 */
public interface Binder<T> {
    static <T> Binder<T> of(TypeDefinition type, Function<DbusObject, T> decoder, Function<T, DbusObject> encoder) {
        return new Binder<T>() {
            @Override
            public TypeDefinition getType() {
                return type;
            }

            @Override
            public T decode(DbusObject object) {
                return decoder.apply(object);
            }

            @Override
            public DbusObject encode(T obj) {
                return encoder.apply(obj);
            }
        };
    }

    TypeDefinition getType() throws TypeNotAvailableException;

    T decode(DbusObject object);

    DbusObject encode(T obj);
}
