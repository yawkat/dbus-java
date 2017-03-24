/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind;

import at.yawk.dbus.databind.annotation.Transformer;
import at.yawk.dbus.databind.binder.*;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author yawkat
 */
public class DataBinder implements BinderFactoryContext {
    private final List<BinderFactory> binderFactories = new ArrayList<>();

    private final Cache<Class<? extends Annotation>, Optional<AnnotationBinderTransformer<?>>> transformerCache =
            new Cache<>();
    private final Cache<Type, Binder<?>> binderCache = new Cache<>();
    private final Cache<Type, Binder<?>> defaultEncoderCache = new Cache<>();
    private final Cache<TypeDefinition, Binder<?>> defaultDecoderCache = new Cache<>();

    {
        binderFactories.add(PrimitiveBinderFactory.getInstance());
        binderFactories.add(ArrayBinderFactory.getInstance());
        binderFactories.add(DictBinderFactory.getInstance());
        binderFactories.add(ObjectBinderFactory.getInstance());
        binderFactories.add(StructBinderFactory.getInstance());
    }

    private <A extends Annotation> Optional<AnnotationBinderTransformer<?>> getBinderTransformer(
            Class<A> annotationClass) {
        return transformerCache.computeIfAbsent(
                annotationClass,
                ac -> {
                    Transformer transformerAnnotation = ac.getAnnotation(Transformer.class);
                    if (transformerAnnotation == null) { return Optional.empty(); }
                    try {
                        return Optional.of(transformerAnnotation.value().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    private Binder<?> applyBinderTransforms(Binder<?> binder, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            Optional<AnnotationBinderTransformer<?>> transformer = getBinderTransformer(annotation.annotationType());
            if (transformer.isPresent()) {
                binder = ((AnnotationBinderTransformer) transformer.get()).wrap(annotation, binder);
            }
        }
        return binder;
    }

    public void addBinderFactory(BinderFactory factory) {
        binderFactories.add(0, factory);
    }

    public Binder<?> getBinder(Type type, AnnotatedElement metaElement) {
        return getBinder(type, Arrays.asList(metaElement.getAnnotations()));
    }

    public Binder<?> getBinder(Type type, List<Annotation> annotations) {
        Binder<?> baseBinder = getBinder(type);
        return applyBinderTransforms(baseBinder, annotations);
    }

    @SuppressWarnings("unchecked")
    public <T> Binder<T> getBinder(Class<T> type) {
        return (Binder<T>) getBinder((Type) type);
    }

    @Override
    public Binder<?> getBinder(Type type) {
        return binderCache.computeIfAbsent(type, t -> {
            for (BinderFactory factory : binderFactories) {
                Binder<?> binder = factory.getBinder(this, type);
                if (binder != null) {
                    return binder;
                }
            }
            throw new UnsupportedOperationException("Cannot bind " + type);
        });
    }

    @Override
    public Binder<?> getDefaultEncodeBinder(Type type) {
        return defaultEncoderCache.computeIfAbsent(type, t -> {
            for (BinderFactory factory : binderFactories) {
                Binder<?> binder = factory.getDefaultEncodeBinder(this, type);
                if (binder != null) {
                    return binder;
                }
            }
            throw new UnsupportedOperationException("Cannot bind " + type);
        });
    }

    @Override
    public Binder<?> getDefaultDecodeBinder(TypeDefinition typeDefinition) {
        return defaultDecoderCache.computeIfAbsent(typeDefinition, t -> {
            for (BinderFactory factory : binderFactories) {
                Binder<?> binder = factory.getDefaultDecodeBinder(this, typeDefinition);
                if (binder != null) {
                    return binder;
                }
            }
            throw new UnsupportedOperationException("Cannot bind " + typeDefinition);
        });
    }

    public Binder<Object> getDefaultBinder() {
        return getBinder(Object.class);
    }
}
