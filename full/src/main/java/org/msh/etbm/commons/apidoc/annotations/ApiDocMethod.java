package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.*;

/**
 * Provide further information about a route. Implemented in a method level
 *
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocMethod {
    /**
     * Detailed description about the method, displayed when user wants to get more information
     * about how the route works
     * @return
     */
    String description() default "";

    /**
     * Single line summary about the purpose of the method
     * @return
     */
    String summary();

    /**
     * List of return codes of this route.
     * @return
     */
    ApiDocReturn[] returnCodes() default {};
}
