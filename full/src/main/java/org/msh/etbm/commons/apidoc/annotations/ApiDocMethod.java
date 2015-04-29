package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.METHOD)
public @interface ApiDocMethod {
    String description();
}
