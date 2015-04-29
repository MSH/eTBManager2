package org.msh.etbm.rest.pub;


import org.msh.etbm.commons.apidoc.annotations.ApiDocField;

import javax.validation.constraints.NotNull;

/**
 * Form data sent from the client to register a new user in the system.
 *
 * Created by ricardo on 24/11/14.
 */
public class RegistrationForm {

    @NotNull
    @ApiDocField(description = "The full name of the user")
    private String name;

    @NotNull
    @ApiDocField(description = "The user login name (required during authentication)")
    private String login;

    @NotNull
    @ApiDocField(description = "The user e-mail address (Password will be sent to this address)")
    private String email;

    @NotNull
    @ApiDocField(description = "The user's organization name")
    private String organization;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
