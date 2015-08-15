package at.yawk.dbus.databind.annotation;

import java.lang.annotation.*;
import javax.annotation.RegEx;

/**
 * @author yawkat
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExceptionMapping.RepeatableExceptionMapping.class)
public @interface ExceptionMapping {
    @RegEx String pattern();

    Class<? extends Exception> exception();

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface RepeatableExceptionMapping {
        ExceptionMapping[] value();
    }
}
