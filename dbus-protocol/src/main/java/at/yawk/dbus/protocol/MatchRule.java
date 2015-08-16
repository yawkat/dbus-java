package at.yawk.dbus.protocol;

import at.yawk.dbus.protocol.object.ObjectPathObject;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MatchRule {
    @Nullable MessageType messageType;
    @Nullable String sender;
    @Nullable String interfaceName;
    @Nullable String member;
    @Nullable ObjectPathObject path;
    @Nullable ObjectPathObject pathNamespace;
    @Nullable String destination;
    @Nullable Map<Integer, String> arguments;
    @Nullable Map<Integer, ObjectPathObject> argumentPaths;
    @Nullable ObjectPathObject arg0Namespace;
    boolean eavesdrop = false;

    public void setSender(@Nonnull String sender) {
        DbusUtil.validateConnectionName(sender);
        this.sender = sender;
    }

    public void setInterfaceName(@Nonnull String interfaceName) {
        DbusUtil.validateConnectionName(interfaceName);
        this.interfaceName = interfaceName;
    }

    public void setMember(@Nonnull String member) {
        DbusUtil.validateMemberName(member);
        this.member = member;
    }

    public void setPath(@Nullable ObjectPathObject path) {
        this.path = path;
    }

    public void setPathNamespace(@Nullable ObjectPathObject pathNamespace) {
        this.pathNamespace = pathNamespace;
    }

    public void setDestination(@Nonnull String destination) {
        DbusUtil.validateConnectionName(destination);
        this.destination = destination;
    }

    public void setArguments(@Nullable Map<Integer, String> arguments) {
        this.arguments = arguments;
    }

    public void setArgumentPaths(@Nullable Map<Integer, ObjectPathObject> argumentPaths) {
        this.argumentPaths = argumentPaths;
    }

    public void setArg0Namespace(@Nullable ObjectPathObject arg0Namespace) {
        this.arg0Namespace = arg0Namespace;
    }

    public void setEavesdrop(boolean eavesdrop) {
        this.eavesdrop = eavesdrop;
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        append(builder, "type", messageType, MessageType::getName);
        append(builder, "sender", sender);
        append(builder, "interface", interfaceName);
        append(builder, "member", member);
        append(builder, "path", path, ObjectPathObject::getSequence);
        append(builder, "path_namespace", pathNamespace, ObjectPathObject::getSequence);
        append(builder, "destination", destination);
        if (arguments != null) {
            arguments.forEach((i, v) -> {
                if (i < 0) { throw new IllegalArgumentException("Negative argument index"); }
                if (i > 63) { throw new IllegalArgumentException("Argument index too large"); }
                append(builder, "arg" + i, v);
            });
        }
        if (argumentPaths != null) {
            argumentPaths.forEach((i, v) -> {
                if (i < 0) { throw new IllegalArgumentException("Negative argument index"); }
                if (i > 63) { throw new IllegalArgumentException("Argument index too large"); }
                append(builder, "arg" + i + "path", v.getSequence());
            });
        }
        append(builder, "arg0Namespace", arg0Namespace, ObjectPathObject::getSequence);
        append(builder, "eavesdrop", eavesdrop, Object::toString);

        // remove trailing comma
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        return builder.toString();
    }

    private static void append(StringBuilder builder, String key, @Nullable CharSequence item) {
        append(builder, key, item, Function.identity());
    }

    private static <T> void append(StringBuilder builder, String key, @Nullable T item, Function<T, CharSequence>
            toString) {
        if (item != null) {
            builder.append(key).append('=');
            escape(toString.apply(item), builder);
            builder.append(',');
        }
    }

    static void escape(CharSequence input, StringBuilder output) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
            case '\\':
                output.append("'\\'");
                break;
            case ',':
                output.append("','");
                break;
            case '\'':
                output.append("\\'");
                break;
            default:
                output.append(c);
                break;
            }
        }
    }
}
