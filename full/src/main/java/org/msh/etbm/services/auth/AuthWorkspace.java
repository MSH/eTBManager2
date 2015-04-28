package org.msh.etbm.services.auth;

/**
 * Informatio about workspaces requested by a valid credential in a REST API call.
 * Mostly used by the authentication services
 *
 * Created by rmemoria on 27/4/15.
 */
public class AuthWorkspace {
    private Integer id;
    private String name;
    private String name2;
    private String description;
    private String unitName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
