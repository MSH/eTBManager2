package org.msh.etbm.commons.apidoc.annotations;

import org.hibernate.annotations.Type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.FIELD)
public @interface ApiDocField {
    String description();
}
