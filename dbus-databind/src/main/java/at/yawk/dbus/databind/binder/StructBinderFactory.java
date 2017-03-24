/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind.binder;

import at.yawk.dbus.databind.annotation.StructMember;
import at.yawk.dbus.databind.annotation.Struct;
import at.yawk.dbus.protocol.object.DbusObject;
import at.yawk.dbus.protocol.object.StructObject;
import at.yawk.dbus.protocol.type.StructTypeDefinition;
import at.yawk.dbus.protocol.type.TypeDefinition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

public class StructBinderFactory implements BinderFactory {
    @Getter
    private static final StructBinderFactory instance = new StructBinderFactory();
    
    private final Map<Class,Binder<?>> binderMap = new LinkedHashMap<Class,Binder<?>>() {
        @Override
        protected boolean removeEldestEntry(Entry<Class,Binder<?>> eldest) {
            return size() > 100;
        }
    };
    
    private StructBinderFactory() {}

    @Override
    public Binder<?> getBinder(BinderFactoryContext ctx, Type type) {
        Class clazz = TypeUtil.getRawType(type);
        
        if(clazz.getAnnotation(Struct.class) == null) {
            return null;
        }
        
        Binder<?> binder = binderMap.get(clazz);
        
        if(binder == null) {
            binder = new StructBinder(ctx, clazz);
            binderMap.put(clazz, binder);
        }

        return binder;
    }

    @Override
    public Binder<?> getDefaultEncodeBinder(BinderFactoryContext ctx, Type type) {
        return getBinder(ctx, type);
    }

    @Override
    public Binder<?> getDefaultDecodeBinder(BinderFactoryContext ctx, TypeDefinition typeDefinition) {
        return null;
    }

    private static class StructBinder<X> implements Binder<X> {
        private final static Pattern GETTER = Pattern.compile("(is|get)(\\p{javaUpperCase}.*)");
        private final static Pattern SETTER = Pattern.compile("(set)(\\p{javaUpperCase}.*)");
        
        private final StructTypeDefinition definition;
        private final TreeMap<Integer,Method> setterMap = new TreeMap<>();
        private final TreeMap<Integer,Method> getterMap = new TreeMap<>();
        private final TreeMap<Integer,Binder> binderMap = new TreeMap<>();
        private final Class<? extends X> clazz;
        
        @SuppressWarnings("unchecked")
        public StructBinder(BinderFactoryContext ctx, Class<? extends X> clazz) {
            this.clazz = clazz;
            Map<String,Integer> memberMap = new HashMap<>();
            Map<String,Method> getter = new HashMap<>();
            Map<String,Method> setter = new HashMap<>();
            for(Method m: clazz.getMethods()) {
                Matcher getterMatcher = GETTER.matcher(m.getName());
                Matcher setterMatcher = SETTER.matcher(m.getName());
                String propertyName = null;
                if(getterMatcher.matches() && m.getParameterCount() == 0) {
                    getter.put(getterMatcher.group(2), m);
                    propertyName = getterMatcher.group(2);
                } else if(setterMatcher.matches() && m.getParameterCount() == 1) {
                    setter.put(setterMatcher.group(2), m);
                    propertyName = setterMatcher.group(2);
                }
                if (propertyName != null) {
                    StructMember sm = m.getAnnotation(StructMember.class);
                    if (sm != null) {
                        memberMap.put(propertyName, sm.position());
                    }
                }
            }
            for(Entry<String,Integer> e: memberMap.entrySet()) {
                Class<?> clazzType = null;
                Method setterMethod = setter.get(e.getKey());
                Method getterMethod = getter.get(e.getKey());
                if(setterMethod != null) {
                    setterMap.put(e.getValue(), setterMethod);
                    Class<?> clazzTypeSetter = setterMethod.getParameterTypes()[0];
                    if(clazzType != null && clazzTypeSetter != clazzType) {
                        throw new IllegalStateException("Getter and setter for member " + e.getKey() + " of " + clazz.getName());
                    } else {
                        clazzType = clazzTypeSetter;
                    }
                }
                if(getterMethod != null) {
                    getterMap.put(e.getValue(), getterMethod);
                    Class<?> clazzTypeGetter = getterMethod.getReturnType();
                    if(clazzType != null && clazzTypeGetter != clazzType) {
                        throw new IllegalStateException("Getter and setter for member " + e.getKey() + " of " + clazz.getName());
                    } else {
                        clazzType = clazzTypeGetter;
                    }
                }
                binderMap.put(e.getValue(), ctx.getBinder(clazzType));
            }
            
            List<TypeDefinition> td = new ArrayList<>(binderMap.size());
            for(Entry<Integer,Binder> sm: binderMap.entrySet()) {
                td.add(sm.getValue().getType());
            }
            definition = new StructTypeDefinition(td);
        }

        @Override
        public TypeDefinition getType() {
            return definition;
        }

        @Override
        public X decode(DbusObject busObject) {
            try {
                X object = clazz.getConstructor().newInstance();
                for(int i = 0; i < setterMap.size(); i++) {
                    if(setterMap.containsKey(i) && binderMap.containsKey(i)) {
                        Method setter = setterMap.get(i);
                        Binder binder = binderMap.get(i);
                        setter.invoke(object, binder.decode(busObject.get(i)));
                    }
                }
                return object;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public DbusObject encode(X obj) {
            try {
                List<DbusObject> data = new ArrayList<>(getterMap.size());
                for (int i = 0; i < getterMap.size(); i++) {
                    if (getterMap.containsKey(i) && binderMap.containsKey(i)) {
                        Method getter = getterMap.get(i);
                        Binder binder = binderMap.get(i);
                        data.set(i, binder.encode(getter.invoke(obj)));
                    }
                }
                return StructObject.create(definition, data);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

    }
}
