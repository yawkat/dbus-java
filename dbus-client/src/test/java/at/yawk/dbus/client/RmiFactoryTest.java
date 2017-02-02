/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import at.yawk.dbus.client.request.Request;
import at.yawk.dbus.client.request.RequestExecutor;
import at.yawk.dbus.client.request.Response;
import at.yawk.dbus.databind.DataBinder;
import at.yawk.dbus.protocol.MessageType;
import at.yawk.dbus.protocol.object.BasicObject;
import at.yawk.dbus.protocol.object.VariantObject;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class RmiFactoryTest {
    private CollectingExecutor executor;
    private A instance;

    private void setUp(Function<Request, Response> executor) {
        this.executor = new CollectingExecutor(executor);
        RmiFactory factory = new RmiFactory(new DataBinder(), this.executor);
        instance = factory.createRmiInstance(A.class);
    }

    @Test
    public void testGetProperty() {
        setUp(request -> Response.success(Collections.singletonList(
                VariantObject.create(BasicObject.createString("hi")))));

        Assert.assertEquals(instance.getTest(), "hi");
        executor.assertEquals(
                new RequestImpl()
                        .type(MessageType.METHOD_CALL)
                        .objectPath("/path")
                        .interfaceName("org.freedesktop.DBus.Properties")
                        .member("Get")
                        .arguments(BasicObject.createString("at.yawk"),
                                   BasicObject.createString("Test"))
        );
    }

    @Test
    public void testCall() {
        setUp(request -> Response.success(Collections.emptyList()));

        instance.doSomething("arg");
        executor.assertEquals(
                new RequestImpl()
                        .type(MessageType.METHOD_CALL)
                        .objectPath("/path")
                        .interfaceName("at.yawk")
                        .member("Something")
                        .arguments(BasicObject.createString("arg"))
        );
    }

    @Test
    public void testSignal() {
        setUp(request -> Response.success(Collections.emptyList()));

        instance.signal("arg");
        executor.assertEquals(
                new RequestImpl()
                        .type(MessageType.SIGNAL)
                        .objectPath("/path")
                        .interfaceName("at.yawk")
                        .member("Signal")
                        .arguments(BasicObject.createString("arg"))
        );
    }

    @Test(expectedExceptions = NoSuchElementException.class,
            expectedExceptionsMessageRegExp = "Hi")
    public void testException() {
        setUp(request -> Response.error("Hi", Collections.emptyList()));
        instance.doSomething("arg");
    }

    @ObjectPath("/path")
    @Interface("at.yawk")
    interface A {
        @Member("Test")
        @GetProperty
        String getTest();

        @Member("Something")
        @Call
        @ExceptionMapping(pattern = "H.", exception = NoSuchElementException.class)
        void doSomething(String arg);

        @Member("Signal")
        @Signal
        void signal(String arg);
    }

}