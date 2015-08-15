package at.yawk.dbus.databind.annotation;

import at.yawk.dbus.databind.binder.PrimitiveAnnotationBinderTransformer;
import at.yawk.dbus.protocol.type.BasicType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transform the annotated value to the given basic type.
 *
 * @author yawkat
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Transformer(PrimitiveAnnotationBinderTransformer.class)
public @interface Primitive {
    BasicType value();
}
