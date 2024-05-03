/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Documented
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.CLASS)
public @interface Contract {
    public ThreadingBehavior threading() default ThreadingBehavior.UNSAFE;
}

