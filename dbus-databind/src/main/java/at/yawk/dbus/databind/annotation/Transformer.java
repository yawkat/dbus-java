package at.yawk.dbus.databind.annotation;

import at.yawk.dbus.databind.binder.AnnotationBinderTransformer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yawkat
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transformer {
    Class<? extends AnnotationBinderTransformer> value();
}
