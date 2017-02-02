/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind.binder;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

/**
 * @author yawkat
 */
@UtilityClass
public class TypeUtil {
    /*
     * We handle:
     * - Class
     * - GenericArrayType
     * - ParameterizedType
     */

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawType(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Class<?> component = getRawType(((GenericArrayType) type).getGenericComponentType());
            return Array.newInstance(component, 0).getClass();
        } else {
            throw unsupported(type);
        }
    }

    public static Type getComponentType(Type type) {
        if (type instanceof Class<?>) {
            Class componentType = ((Class) type).getComponentType();
            if (componentType == null) {
                throw new IllegalArgumentException("Not an array: " + ((Class) type).getName());
            }
            return componentType;
        } else if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        } else {
            throw unsupported(type);
        }
    }

    /**
     * Resolve the type variable on the given declared type.
     *
     * @param declared The declared type ({@code List<String>})
     * @param type     The class this type variable is defined on ({@code List})
     * @param name     The name of this type variable
     */
    public static Type getTypeVariable(Type declared, Class<?> type, String name) {
        Deque<Type> inheritanceTree = getInheritanceTree(type, declared);
        Map<TypeVariable<?>, Type> variableMap = new HashMap<>();

        for (Type t : inheritanceTree) {
            resolveTypeArguments(t, variableMap);
        }

        int i = findTypeVariableIndex(type, name);
        TypeVariable<?> parameter = type.getTypeParameters()[i];
        Type resolvedType = variableMap.get(parameter);

        if (resolvedType == null) {
            return findLowerBound(parameter);
        } else {
            return resolvedType;
        }
    }

    private static void resolveTypeArguments(Type declared, Map<TypeVariable<?>, Type> variableMap) {
        if (declared instanceof ParameterizedType) {
            Class<?> rawDeclared = getRawType(declared);
            Type[] actualTypeArguments = ((ParameterizedType) declared).getActualTypeArguments();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type arg = actualTypeArguments[i];
                TypeVariable<? extends Class<?>> variable = rawDeclared.getTypeParameters()[i];

                if (arg instanceof TypeVariable<?>) {
                    Type resolvedArg = variableMap.get(arg);
                    if (resolvedArg != null) {
                        arg = resolvedArg;
                    } else {
                        arg = findLowerBound((TypeVariable<?>) arg);
                    }
                }

                variableMap.put(variable, arg);
            }
        }
    }

    /**
     * Find a lower bound. Classes are preferred, then the first declared interface, then Object.
     */
    private static Type findLowerBound(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        Class<?> interfaceChoice = null;
        for (Type bound : bounds) {
            Class<?> rawBound = getRawType(bound);
            if (rawBound == Object.class) { continue; } // ignore object bounds

            if (!rawBound.isInterface()) {
                return rawBound;
            } else if (interfaceChoice == null) {
                interfaceChoice = rawBound;
            }
        }
        if (interfaceChoice == null) {
            return Object.class;
        } else {
            return interfaceChoice;
        }
    }

    private static int findTypeVariableIndex(Class<?> declaring, String name) {
        return findTypeVariableIndex(declaring, v -> v.getName().equals(name));
    }

    private static int findTypeVariableIndex(Class<?> declaring, Predicate<TypeVariable<?>> predicate) {
        TypeVariable<? extends Class<?>>[] typeParameters = declaring.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (predicate.test(typeParameters[i])) {
                return i;
            }
        }
        throw new NoSuchElementException("Could not find type variable on " + declaring.getName());
    }

    /**
     * Get the inheritance sequence between {@code baseType} and {@code extensionType}.
     *
     * For example, with the base type {@code Iterable<E>} and the extension type {@code java.util.ArrayList<E>}, this
     * will yield {@code [ArrayList<E>, AbstractList<E>, AbstractCollection<E>, Collection<E>, Iterable<E>]}.
     */
    static Deque<Type> getInheritanceTree(Type baseType, Type extensionType) {
        Deque<Type> tree = new ArrayDeque<>();
        if (!visitInheritanceTree(baseType, extensionType, tree)) {
            throw new IllegalArgumentException(baseType + " does not extend " + extensionType);
        }
        return tree;
    }

    private static boolean visitInheritanceTree(Type baseType, Type extensionType, Deque<Type> stack) {
        stack.addLast(extensionType);

        Class<?> rawBaseType = getRawType(baseType);
        Class<?> rawExtensionType = getRawType(extensionType);
        if (rawBaseType == rawExtensionType) {
            return true;
        }

        if (rawExtensionType.getSuperclass() != null &&
            visitInheritanceTree(baseType, rawExtensionType.getGenericSuperclass(), stack)) {
            return true;
        }

        for (Type itf : rawExtensionType.getGenericInterfaces()) {
            if (visitInheritanceTree(baseType, itf, stack)) {
                return true;
            }
        }

        stack.removeLast();
        return false;
    }

    private static RuntimeException unsupported(Type type) {
        return new UnsupportedOperationException(
                "Unsupported type class " + (type == null ? null : type.getClass().getName()));
    }
}
