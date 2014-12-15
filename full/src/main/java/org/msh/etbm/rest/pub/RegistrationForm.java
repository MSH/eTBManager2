package org.msh.etbm.rest.pub;

import javax.validation.constraints.NotNull;

/**
 * Form data sent from the client to register a new user in the system.
 *
 * Created by ricardo on 24/11/14.
 */
public class RegistrationForm {

    @NotNull
    private String name;

    @NotNull
    private String login;

    @NotNull
    private String email;

    @NotNull
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
