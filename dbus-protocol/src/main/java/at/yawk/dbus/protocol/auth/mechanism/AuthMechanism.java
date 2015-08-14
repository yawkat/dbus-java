package at.yawk.dbus.protocol.auth.mechanism;

import at.yawk.dbus.protocol.auth.AuthChannel;
import java.util.concurrent.CompletionStage;

/**
 * @author yawkat
 */
public interface AuthMechanism {
    CompletionStage<?> startAuth(AuthChannel channel) throws Exception;
}
