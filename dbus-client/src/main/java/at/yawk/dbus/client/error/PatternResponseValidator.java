/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.error;

import at.yawk.dbus.client.request.Response;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author yawkat
 */
public class PatternResponseValidator implements ResponseValidator {
    private final Pattern pattern;
    private final Function<String, Exception> exceptionFactory;

    @SuppressWarnings("unchecked")
    public PatternResponseValidator(Pattern pattern, Class<? extends Exception> exceptionClass)
            throws NoSuchMethodException, IllegalAccessException {
        this.pattern = pattern;

        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(
                exceptionClass,
                MethodType.methodType(void.class, String.class)
        );
        this.exceptionFactory = MethodHandleProxies.asInterfaceInstance(Function.class, constructor);
    }

    public PatternResponseValidator(Pattern pattern, Function<String, Exception> exceptionFactory) {
        this.pattern = pattern;
        this.exceptionFactory = exceptionFactory;
    }

    @Override
    public void validate(Response response) throws Exception {
        if (response.getErrorName() != null && pattern.matcher(response.getErrorName()).matches()) {
            throw exceptionFactory.apply(response.getErrorName());
        }
    }
}
