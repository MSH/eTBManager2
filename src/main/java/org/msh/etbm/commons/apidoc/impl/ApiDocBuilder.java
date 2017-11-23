package org.msh.etbm.commons.apidoc.impl;

import org.msh.etbm.commons.apidoc.model.ApiGroup;
import org.msh.etbm.commons.apidoc.model.ApiRoute;

/**
 * Created by rmemoria on 29/4/15.
 */
public interface ApiDocBuilder {
    ApiGroup addGroup(String name);

    ApiRoute addRoute(ApiGroup grp, String path, ApiRoute.MethodType type);
}
