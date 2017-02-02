/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.client.annotation;

import java.lang.annotation.*;
import javax.annotation.RegEx;

/**
 * @author yawkat
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExceptionMapping.RepeatableExceptionMapping.class)
public @interface ExceptionMapping {
    String value() default "";

    @RegEx String pattern() default "";

    Class<? extends Exception> exception();

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface RepeatableExceptionMapping {
        ExceptionMapping[] value();
    }
}
