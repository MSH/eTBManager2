package org.msh.etbm.commons.apidoc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 28/4/15.
 */
public class ApiGroup {

    private String description;
    private String name;
    private List<ApiRoute> routes = new ArrayList<ApiRoute>();
    private List<ApiReturn> returnCodes = new ArrayList<ApiReturn>();


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApiRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ApiRoute> routes) {
        this.routes = routes;
    }

    public List<ApiReturn> getReturnCodes() {
        return returnCodes;
    }

    public void setReturnCodes(List<ApiReturn> returnCodes) {
        this.returnCodes = returnCodes;
    }
}
