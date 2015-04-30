package org.msh.etbm.rest.pub;

import javax.validation.constraints.NotNull;

/**
 * Authentication form sent by the client to log a user into the system.
 *
 * Created by ricardo on 23/11/14.
 */
public class AuthenticationForm {
    @NotNull
    private String login;

    @NotNull
    private String password;

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
