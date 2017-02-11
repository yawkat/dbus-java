/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client;

import at.yawk.dbus.databind.DataBinder;
import at.yawk.dbus.client.error.ResponseValidator;
import at.yawk.dbus.client.request.RequestExecutor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class RmiFactory {
    private final DataBinder binder;
    private final RequestExecutor executor;
    private final CallSiteBuilder baseSite;

    public RmiFactory(DataBinder binder, RequestExecutor executor) {
        this.binder = binder;
        this.executor = executor;
        this.baseSite = new CallSiteBuilder();
        log.trace("Base call site is {}", baseSite);

        addValidator(ResponseValidator.HANDLE_ERROR);
    }

    public RmiFactory(RmiFactory parent) {
        this.binder = parent.binder;
        this.executor = parent.executor;
        this.baseSite = parent.baseSite.createChild(false);
        log.trace("Inherited call site is {}", baseSite);
    }

    public void addValidator(ResponseValidator validator) {
        baseSite.addValidator(validator);
    }

    @SuppressWarnings("unchecked")
    public <I> I createRmiInstance(Class<I> type) {
        return createRmiInstance(type, null);
    }
    
    @SuppressWarnings("unchecked")
    public <I> I createRmiInstance(Class<I> type, final DBUSDestination destination) {
        CallSiteBuilder classSite = baseSite.createChild(false);
        
        classSite.decorateFromClass(type);
        log.trace("Class call site for {} is {}", classSite, classSite);

        Map<Method, CallSiteBuilder> sites = new HashMap<>();
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class ||
                Modifier.isStatic(method.getModifiers())) { continue; }

            CallSiteBuilder methodSite = classSite.createChild(false);
            methodSite.decorateFromMethod(binder, method);
            log.trace("Method call site for {} is {}", method, methodSite);

            sites.put(method, methodSite);
        }

        InvocationHandler invocationHandler = (proxy, method, args) -> {
            CallSiteBuilder site = sites.get(method);
            if (site == null) {
                // todo: Object methods
                assert false;
            }

            site = site.createChild(true);
            site.decorateFromCall(args);

            if(destination != null && destination.getBus() != null) {
                site.bus = destination.getBus();
            }
            if(destination != null && destination.getDestination() != null) {
                site.destination = destination.getDestination();
            }
            if(destination != null && destination.getObjectPath() != null) {
                site.objectPath = destination.getObjectPath();
            }
            
            return site.submit(executor);
        };

        return (I) Proxy.newProxyInstance(RmiFactory.class.getClassLoader(), new Class[]{ type }, invocationHandler);
    }
}
