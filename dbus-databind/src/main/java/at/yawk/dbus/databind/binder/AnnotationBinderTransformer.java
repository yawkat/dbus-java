/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.dbus.databind.binder;

import java.lang.annotation.Annotation;

/**
 * @author yawkat
 */
public interface AnnotationBinderTransformer<A extends Annotation> {
    Binder<?> wrap(A annotation, Binder<?> binder);
}
