package org.msh.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * fields annotated with that will have its properties validated too.
 * Must be a POJO object. No effect on primitives types
 *
 * Created by Ricardo Memoria on 18/4/15.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface InnerValidation {
}
