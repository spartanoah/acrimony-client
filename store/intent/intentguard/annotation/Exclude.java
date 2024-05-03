/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package store.intent.intentguard.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import store.intent.intentguard.annotation.Strategy;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Exclude {
    public Strategy[] value() default {Strategy.NO_STRATEGY};
}

