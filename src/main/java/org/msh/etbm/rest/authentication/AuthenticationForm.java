package org.msh.etbm.rest.authentication;

import org.msh.etbm.commons.apidoc.annotations.ApiDocField;

import javax.validation.constraints.NotNull;

/**
 * Authentication form sent by the client to log a user into the system.
 *
 * Created by ricardo on 23/11/14.
 */
public class AuthenticationForm {
    @ApiDocField(description = "User's login")
    @NotNull
    private String login;

    @ApiDocField(description = "User's password")
    @NotNull
    private String password;

    @ApiDocField(description = "Workspace ID that user is part of")
    @NotNull
    private Integer workspace;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Integer workspace) {
        this.workspace = workspace;
    }
}
