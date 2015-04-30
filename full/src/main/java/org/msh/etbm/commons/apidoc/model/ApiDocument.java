package org.msh.etbm.commons.apidoc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 28/4/15.
 */
public class ApiDocument {

    private String basePath;
    private String version;
    private List<ApiGroup> groups = new ArrayList<ApiGroup>();
    private List<ApiReturn> defaultResponses = new ArrayList<ApiReturn>();

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ApiGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ApiGroup> groups) {
        this.groups = groups;
    }

    public List<ApiReturn> getDefaultResponses() {
        return defaultResponses;
    }

    public void setDefaultResponses(List<ApiReturn> defaultResponses) {
        this.defaultResponses = defaultResponses;
    }
}
