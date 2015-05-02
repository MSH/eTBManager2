package org.msh.etbm.rest.authentication;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.services.auth.AuthWorkspace;
import org.msh.etbm.services.auth.AuthenticationService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST API to cover user authentication operations
 *
 * Created by ricardo on 23/11/14.
 */
@Name("authenticationRest")
@Path("/pub")
@ApiDoc(group = "public - authentication", description = "Public routes used for authentication purposes")
public class AuthenticationRest {

    @In AuthenticationService authenticationService;

    /**
     * Log user in the system by checking its username, password and workspace, and if user
     * successfully logs, returns its token id
     * @param form instance of AuthenticationForm containing user credentials
     * @return the token ID inside the standard result
     */
    @Path("/login")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiDocMethod(description = "Authenticate a valid user name and password in a given workspace")
    public String login(AuthenticationForm form) {
        String token = authenticationService.login(form.getLogin(), form.getPassword(), form.getWorkspace());

        return token;
    }


    @Path("/workspaces")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiDocMethod(description = "Return the list of workspaces available for a valid user name and password")
    public List<AuthWorkspace> getWorkspaces(AuthenticationForm form) {
        return authenticationService.getWorkspaces(form.getLogin(), form.getPassword());
    }
}
