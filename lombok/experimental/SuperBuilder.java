/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.SOURCE)
public @interface SuperBuilder {
    public String builderMethodName() default "builder";

    public String buildMethodName() default "build";

    public boolean toBuilder() default false;

    public String setterPrefix() default "";
}

