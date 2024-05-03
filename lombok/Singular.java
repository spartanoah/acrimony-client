/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.FIELD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.SOURCE)
public @interface Singular {
    public String value() default "";

    public boolean ignoreNullCollections() default false;
}

