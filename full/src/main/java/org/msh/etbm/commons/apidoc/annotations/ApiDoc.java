package org.msh.etbm.commons.apidoc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Information about an API implemented in class level
 *
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.TYPE)
public @interface ApiDoc {
    String group();
    String description();
}
