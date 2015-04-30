package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.*;

/**
 * Information about an API implemented in class level
 *
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDoc {
    String group();
    String description();
    ApiDocReturn[] returnCodes() default {};
}
