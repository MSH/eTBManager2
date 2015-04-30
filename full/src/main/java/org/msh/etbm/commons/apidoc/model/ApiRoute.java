package org.msh.etbm.commons.apidoc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 29/4/15.
 */
public class ApiRoute {

    public enum MethodType {
        GET,
        POST,
        PUT,
        DELETE
    }

    private String path;
    private String description;
    private MethodType type;
    private List<String> consumes;
    private List<String> produces;
    private ApiObject inputObject;
    private ApiObject returnObject;
    private List<ApiReturn> returnCodes = new ArrayList<ApiReturn>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MethodType getType() {
        return type;
    }

    public void setType(MethodType type) {
        this.type = type;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public ApiObject getInputObject() {
        return inputObject;
    }

    public void setInputObject(ApiObject inputObject) {
        this.inputObject = inputObject;
    }

    public ApiObject getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(ApiObject returnObject) {
        this.returnObject = returnObject;
    }

    public List<ApiReturn> getReturnCodes() {
        return returnCodes;
    }

    public void setReturnCodes(List<ApiReturn> returnCodes) {
        this.returnCodes = returnCodes;
    }
}
