package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import at.yawk.dbus.client.error.PatternResponseValidator;
import at.yawk.dbus.client.error.ResponseValidator;
import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.client.request.RequestExecutor;
import at.yawk.dbus.client.request.Response;
import at.yawk.dbus.databind.DataBinder;
import at.yawk.dbus.databind.binder.Binder;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * @author yawkat
 */
@ToString(of = {
        "messageType",
        "requestType",
        "objectPath",
        "interfaceName",
        "member",
        "destination",
        "arguments",
}, doNotUseGetters = true)
class CallSiteBuilder implements Request {
    @Getter String bus;
    MessageType messageType;
    RequestType requestType;
    String objectPath;
    String interfaceName;
    String member;
    String destination;
    List<CallSiteAction> actions = new ArrayList<>();
    @Getter List<DbusObject> arguments = new ArrayList<>();
    List<ResponseValidator> responseValidators = new ArrayList<>();

    // todo: properly support array returns
    Binder<?> returnBinder;

    ObjectPathObject objectPathObject;
    StringObject interfaceObject;
    StringObject memberObject;
    StringObject destinationObject;

    /**
     * @param childTransient If set to {@code true}, the returned call site will not be decorated from anything but
     *                       {@link #decorateFromCall(Object[])}.
     */
    CallSiteBuilder createChild(boolean childTransient) {
        CallSiteBuilder child = new CallSiteBuilder();
        child.bus = bus;
        child.requestType = requestType;
        child.messageType = messageType;
        child.objectPath = objectPath;
        child.objectPathObject = objectPathObject;
        child.interfaceName = interfaceName;
        child.member = member;
        child.actions = childTransient ? actions : new ArrayList<>(actions);
        child.responseValidators = childTransient ? responseValidators : new ArrayList<>(responseValidators);
        child.arguments = new ArrayList<>(arguments);
        child.returnBinder = returnBinder;
        return child;
    }

    @Override
    public MessageType getType() {
        return messageType;
    }

    @Override
    public ObjectPathObject getObjectPath() {
        if (objectPathObject == null) { objectPathObject = ObjectPathObject.create(objectPath); }
        return objectPathObject;
    }

    @Override
    public StringObject getInterfaceName() {
        if (interfaceObject == null) { interfaceObject = StringObject.create(interfaceName); }
        return interfaceObject;
    }

    @Override
    public StringObject getMember() {
        if (memberObject == null) { memberObject = StringObject.create(member); }
        return memberObject;
    }

    @Nullable
    @Override
    public StringObject getDestination() {
        if (destination == null) { return null; }
        if (destinationObject == null) { destinationObject = StringObject.create(destination); }
        return destinationObject;
    }

    void decorateFromClass(Class<?> clazz) {
        decorateFromAnnotations(clazz);
    }

    @SuppressWarnings("unchecked")
    void decorateFromMethod(DataBinder dataBinder, Method method) {
        decorateFromAnnotations(method);

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type parameter = genericParameterTypes[i];
            Binder binder = dataBinder.getBinder(parameter);
            final int finalI = i;
            actions.add((site, args) -> site.arguments.add(binder.encode(args[finalI])));
        }

        if (method.getReturnType() != void.class) {
            returnBinder = dataBinder.getBinder(method.getGenericReturnType(), method);
        }
    }

    private void decorateFromAnnotations(AnnotatedElement element) {
        ifPresent(element, Destination.class, a -> this.destination = a.value());
        ifPresent(element, Interface.class, a -> this.interfaceName = a.value());
        ifPresent(element, Member.class, a -> this.member = a.value());
        ifPresent(element, ObjectPath.class, a -> this.objectPath = a.value());
        ifPresent(element, SubInterface.class, a -> this.interfaceName += '.' + a.value());
        ifPresent(element, Bus.class, a -> this.bus = a.value());
        ifPresent(element, SystemBus.class, a -> this.bus = "system");
        ifPresent(element, UserBus.class, a -> this.bus = "user");
        ifPresent(element, Call.class, a -> {
            this.requestType = RequestType.CALL;
            this.messageType = MessageType.METHOD_CALL;
        });
        ifPresent(element, Signal.class, a -> {
            this.requestType = RequestType.SIGNAL;
            this.messageType = MessageType.SIGNAL;
        });
        ifPresent(element, GetProperty.class, a -> {
            this.requestType = RequestType.GET_PROPERTY;
            this.messageType = MessageType.METHOD_CALL;

            actions.add((site, args) -> {
                site.arguments.add(BasicObject.createString(site.interfaceName));
                site.arguments.add(BasicObject.createString(site.member));
                site.interfaceName = "org.freedesktop.DBus.Properties";
                site.member = "Get";
            });
        });
        ifPresent(element, ExceptionMapping.class, this::decorateFromExceptionMapping);
        ifPresent(element, ExceptionMapping.RepeatableExceptionMapping.class, rep -> {
            for (ExceptionMapping mapping : rep.value()) {
                decorateFromExceptionMapping(mapping);
            }
        });
    }

    @SneakyThrows
    private void decorateFromExceptionMapping(ExceptionMapping mapping) {
        PatternResponseValidator validator = new PatternResponseValidator(
                Pattern.compile(mapping.pattern()), mapping.exception());
        addValidator(validator);
    }

    void addValidator(ResponseValidator validator) {
        responseValidators.add(0, validator);
    }

    void decorateFromCall(Object[] args) throws Exception {
        for (CallSiteAction action : actions) {
            action.apply(this, args);
        }
    }

    Object submit(RequestExecutor executor) throws Exception {
        Response response = executor.execute(this);
        for (ResponseValidator validator : responseValidators) {
            validator.validate(response);
        }
        List<DbusObject> reply = response.getReply();
        if (returnBinder == null) {
            return null; // void
        } else {
            return returnBinder.decode(reply.get(0));
        }
    }

    private static <A extends Annotation> void ifPresent(AnnotatedElement element, Class<A> annotationClass,
                                                         Consumer<A> consumer) {
        A annotation = element.getAnnotation(annotationClass);
        if (annotation != null) {
            consumer.accept(annotation);
        }
    }

    private interface CallSiteAction {
        void apply(CallSiteBuilder site, Object[] methodArgs) throws Exception;
    }

    private enum RequestType {
        CALL,
        SIGNAL,
        GET_PROPERTY,
    }
}
