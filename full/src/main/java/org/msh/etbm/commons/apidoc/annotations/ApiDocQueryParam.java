package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.*;

/**
 * Created by rmemoria on 29/4/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ApiDocQueryParam {
    String value();
}
