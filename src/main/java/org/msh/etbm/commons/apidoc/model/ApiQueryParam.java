package org.msh.etbm.commons.apidoc.model;

/**
 * Created by rmemoria on 29/4/15.
 */
public class ApiQueryParam {
    private String name;
    private String type;
    private String description;

    public ApiQueryParam(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApiQueryParam() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
