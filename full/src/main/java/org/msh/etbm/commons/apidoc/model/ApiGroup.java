package org.msh.etbm.commons.apidoc.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 28/4/15.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ApiGroup {

    private String summary;
    private String description;
    private String name;
    private List<ApiRoute> routes;
    private List<ApiReturn> returnCodes;


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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
