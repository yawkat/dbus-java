package at.yawk.dbus.protocol;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * @author yawkat
 */
@AllArgsConstructor
@Setter
class SwappableMessageConsumer implements MessageConsumer {
    @Delegate private MessageConsumer consumer;
}
