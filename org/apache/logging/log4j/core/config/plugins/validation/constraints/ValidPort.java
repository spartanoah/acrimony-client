/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.plugins.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.logging.log4j.core.config.plugins.validation.Constraint;
import org.apache.logging.log4j.core.config.plugins.validation.validators.ValidPortValidator;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.PARAMETER})
@Constraint(value=ValidPortValidator.class)
public @interface ValidPort {
    public String message() default "The port number is invalid";
}

