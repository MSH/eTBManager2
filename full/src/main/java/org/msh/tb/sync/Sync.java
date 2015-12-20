package org.msh.tb.sync;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation will be assigned to fields of type list that needs to be clear before synchronizing the entity.
 * Created by Mauricio on 03/12/2015.
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface Sync {
    boolean clearList() default false;
    boolean keyAttribute() default false;
    String internalKeyAttribute() default "";
}
