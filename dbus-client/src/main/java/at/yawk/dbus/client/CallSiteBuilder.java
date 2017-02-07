/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.Bus;
import at.yawk.dbus.client.annotation.Call;
import at.yawk.dbus.client.annotation.Destination;
import at.yawk.dbus.client.annotation.ExceptionMapping;
import at.yawk.dbus.client.annotation.GetProperty;
import at.yawk.dbus.client.annotation.Interface;
import at.yawk.dbus.client.annotation.Listener;
import at.yawk.dbus.client.annotation.Member;
import at.yawk.dbus.client.annotation.ObjectPath;
import at.yawk.dbus.client.annotation.SessionBus;
import at.yawk.dbus.client.annotation.Signal;
import at.yawk.dbus.client.annotation.SubInterface;
import at.yawk.dbus.client.annotation.SystemBus;
import at.yawk.dbus.client.annotation.Timeout;
import at.yawk.dbus.client.error.PatternResponseValidator;
import at.yawk.dbus.client.error.ResponseValidator;
import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.client.request.RequestExecutor;
import at.yawk.dbus.client.request.Response;
import at.yawk.dbus.databind.DataBinder;
import at.yawk.dbus.databind.binder.Binder;
import at.yawk.dbus.databind.binder.PrimitiveAnnotationBinderTransformer;
import at.yawk.dbus.databind.binder.TypeUtil;
import at.yawk.dbus.protocol.MatchRule;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.ObjectPathObject;
import at.yawk.dbus.protocol.object.StringObject;
import at.yawk.dbus.protocol.type.BasicType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@ToString(of = {
        "messageType",
        "objectPath",
        "interfaceName",
        "member",
        "destination",
        "arguments",
}, doNotUseGetters = true)
@Slf4j
class CallSiteBuilder implements Request {
    @Getter String bus;
    MessageType messageType;
    String objectPath;
    String interfaceName;
    String member;
    String destination;
    List<CallSiteAction> actions = new ArrayList<>();
    @Getter List<DbusObject> arguments = new ArrayList<>();
    List<ResponseValidator> responseValidators = new ArrayList<>();

    boolean markedWithListener;
    boolean eavesdrop;
    Consumer<List<DbusObject>> listener;

    // todo: properly support array returns
    Binder<?> returnBinder;
    /**
     * Unwrap the returned variant before passing to the return binder; used by property get
     */
    boolean unwrapReturnVariant;

    // todo: bake these if possible
    ObjectPathObject objectPathObject;
    StringObject interfaceObject;
    StringObject memberObject;
    StringObject destinationObject;

    int timeout = -1;
    TimeUnit timeoutUnit;

    /**
     * @param childTransient If set to {@code true}, the returned call site will not be decorated from anything but
     *                       {@link #decorateFromCall(Object[])}.
     */
    CallSiteBuilder createChild(boolean childTransient) {
        CallSiteBuilder child = new CallSiteBuilder();
        child.bus = bus;
        child.messageType = messageType;

        child.objectPath = objectPath;
        child.objectPathObject = objectPathObject;

        child.interfaceName = interfaceName;
        child.interfaceObject = interfaceObject;

        child.destination = destination;
        child.destinationObject = destinationObject;

        child.member = member;
        child.memberObject = memberObject;

        child.actions = childTransient ? actions : new ArrayList<>(actions);
        child.responseValidators = childTransient ? responseValidators : new ArrayList<>(responseValidators);
        child.arguments = new ArrayList<>(arguments);
        child.markedWithListener = markedWithListener;
        child.eavesdrop = eavesdrop;
        child.returnBinder = returnBinder;
        child.unwrapReturnVariant = unwrapReturnVariant;

        child.timeout = timeout;
        child.timeoutUnit = timeoutUnit;
        return child;
    }

    @Override
    public MessageType getType() {
        return messageType;
    }

    @Override
    public ObjectPathObject getObjectPath() {
        if (objectPathObject == null && objectPath != null) {
            objectPathObject = ObjectPathObject.create(objectPath);
        }
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

        if (markedWithListener) {
            if (genericParameterTypes.length != 1) {
                throw new IllegalArgumentException("Invalid parameter count on listener " + method);
            }
            int listenerParameterIndex = 0;

            Type listenerParameter = genericParameterTypes[listenerParameterIndex];
            Class<?> raw = TypeUtil.getRawType(listenerParameter);
            if (raw == Runnable.class) {
                actions.add((site, args) -> {
                    Runnable runnable = (Runnable) args[listenerParameterIndex];
                    site.listener = o -> runnable.run();
                });
            } else if (raw == Consumer.class) {
                actions.add((site, args) -> {
                    Consumer consumer = (Consumer) args[listenerParameterIndex];
                    site.listener = o -> consumer.accept(decodeReply(o));
                });
                returnBinder = dataBinder.getBinder(
                        TypeUtil.getTypeVariable(listenerParameter, Consumer.class, "T"));
            } else if (raw.isInterface()) {
                // find the non-default non-static method
                Method targetMethod = null;
                for (Method m : raw.getMethods()) {
                    if (m.isSynthetic()) continue;
                    if (Modifier.isStatic(m.getModifiers())) continue;
                    if (m.isDefault()) continue;

                    if (targetMethod != null) {
                        throw new IllegalArgumentException(
                                "Listener type " + raw.getName() + " is not a functional interface");
                    }
                    targetMethod = m;
                }
                if (targetMethod == null) {
                    throw new IllegalArgumentException(
                            "Listener type " + raw.getName() + " is not a functional interface");
                }
                // build parameter binders
                List<Binder<?>> binders = new ArrayList<>();
                for (AnnotatedType parameterType : targetMethod.getAnnotatedParameterTypes()) {
                    binders.add(dataBinder.getBinder(parameterType.getType(), parameterType));
                }
                Method finalTargetMethod = targetMethod;
                actions.add((site, args) -> {
                    Object listener = args[listenerParameterIndex];
                    site.listener = l -> {
                        // happens sometimes for some reason
                        if (l.isEmpty() && !binders.isEmpty()) return;

                        Object[] parameters = new Object[binders.size()];
                        for (int i = 0; i < parameters.length; i++) {
                            parameters[i] = binders.get(i).decode(l.get(i));
                        }
                        try {
                            finalTargetMethod.invoke(listener, parameters);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                });
            } else {
                throw new IllegalArgumentException("Unsupported listener type " + raw.getName());
            }

            if (method.getReturnType() != void.class) {
                throw new IllegalArgumentException("Unsupported return type on listener method " + method);
            }

        } else {
            for (int i = 0; i < genericParameterTypes.length; i++) {
                Type parameter = genericParameterTypes[i];
                Annotation[] annotations = method.getParameterAnnotations()[i];
                Binder binder = dataBinder.getBinder(parameter, Arrays.asList(annotations));
                final int finalI = i;
                actions.add((site, args) -> site.arguments.add(binder.encode(args[finalI])));
            }

            if (method.getReturnType() != void.class) {
                returnBinder = dataBinder.getBinder(method.getGenericReturnType(), method);
                if (unwrapReturnVariant) {
                    returnBinder = PrimitiveAnnotationBinderTransformer.transformBinder(
                            returnBinder, BasicType.VARIANT);
                }
            }
        }
    }

    private void decorateFromAnnotations(AnnotatedElement element) {
        ifPresent(element, Listener.class, a -> {
            markedWithListener = true;
            eavesdrop = a.eavesdrop();
            if (messageType == null) {
                messageType = MessageType.SIGNAL;
            }
        });
        ifPresent(element, Destination.class, a -> this.destination = a.value());
        ifPresent(element, Interface.class, a -> this.interfaceName = a.value());
        ifPresent(element, Member.class, a -> this.member = a.value());
        ifPresent(element, ObjectPath.class, a -> this.objectPath = a.value());
        ifPresent(element, SubInterface.class, a -> this.interfaceName += '.' + a.value());
        ifPresent(element, Bus.class, a -> this.bus = a.value());
        ifPresent(element, SystemBus.class, a -> this.bus = "system");
        ifPresent(element, SessionBus.class, a -> this.bus = "session");
        ifPresent(element, Call.class, a -> this.messageType = MessageType.METHOD_CALL);
        ifPresent(element, Signal.class, a -> this.messageType = MessageType.SIGNAL);
        ifPresent(element, GetProperty.class, a -> {
            this.messageType = MessageType.METHOD_CALL;
            this.unwrapReturnVariant = true;

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
        ifPresent(element, Timeout.class, to -> {
            this.timeout = to.value();
            this.timeoutUnit = to.unit();
        });
    }

    @SneakyThrows
    private void decorateFromExceptionMapping(ExceptionMapping mapping) {
        Pattern pattern = mapping.pattern().isEmpty() ?
                Pattern.compile(mapping.value(), Pattern.LITERAL) :
                Pattern.compile(mapping.pattern());

        PatternResponseValidator validator = new PatternResponseValidator(pattern, mapping.exception());
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
        log.trace("Submitting call site {}", this);
        if (markedWithListener) {
            assert listener != null;
            MatchRule rule = new MatchRule();
            rule.setMessageType(getType());
            rule.setPath(getObjectPath());
            if (interfaceName != null) rule.setInterfaceName(interfaceName);
            // this breaks some listens and shouldn't really be used anyway
            //rule.setDestination(destination);
            if (member != null) rule.setMember(member);
            rule.setEavesdrop(eavesdrop);
            executor.listen(bus, rule, listener);
            return null;
        } else {
            assert listener == null;
            Response response = timeout > 0 ?
                    executor.execute(this, timeout, timeoutUnit) :
                    executor.execute(this);
            for (ResponseValidator validator : responseValidators) {
                validator.validate(response);
            }
            List<DbusObject> reply = response.getReply();
            if (returnBinder == null) {
                return null; // void
            } else {
                return decodeReply(reply);
            }
        }
    }

    private Object decodeReply(List<DbusObject> reply) {
        return returnBinder.decode(reply.get(0));
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
}
