/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.protocol;

/**
 * @author yawkat
 */
public interface MessageConsumer {
    MessageConsumer DISCARD = new MessageConsumer() {
        @Override
        public boolean requireAccept(MessageHeader header) {
            return false;
        }

        @Override
        public void accept(DbusMessage message) {}
    };

    /**
     * Check if this consumer would like to decode the message with the given header. This is optional: if {@code
     * false} is returned, {@link #accept(DbusMessage)} may still be called.
     */
    boolean requireAccept(MessageHeader header);

    void accept(DbusMessage message);
}
