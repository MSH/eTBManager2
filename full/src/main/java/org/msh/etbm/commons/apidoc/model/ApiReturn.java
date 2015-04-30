package org.msh.etbm.commons.apidoc.model;

/**
 * Created by rmemoria on 29/4/15.
 */
public class ApiReturn {
    private String statusCode;
    private String description;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
