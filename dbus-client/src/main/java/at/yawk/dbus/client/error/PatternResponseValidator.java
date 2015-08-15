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
        if (response.getError() != null && pattern.matcher(response.getError()).matches()) {
            throw exceptionFactory.apply(response.getError());
        }
    }
}
