package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.*;

/**
 * Created by rmemoria on 29/4/15.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocReturn {
    String statusCode();
    String description();
}
