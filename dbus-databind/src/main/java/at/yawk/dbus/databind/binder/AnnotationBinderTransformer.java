package at.yawk.dbus.databind.binder;

import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public interface AnnotationBinderTransformer<A extends Annotation> {
    Binder<?> wrap(A annotation, Binder<?> binder);
}
