package org.msh.etbm.commons.apidoc.annotations;

import org.hibernate.annotations.Type;

import java.lang.annotation.*;

/**
 * Created by rmemoria on 28/4/15.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocField {
    String description();
}
