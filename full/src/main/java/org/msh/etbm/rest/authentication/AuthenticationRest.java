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
@ApiDoc(group = "authentication", summary = "Public routes used for authentication purposes")
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
    @ApiDocMethod(summary = "Authenticate a valid user name and password in a given workspace",
            description = "This API must be executed prior to every API call that requires authentication. " +
                    "On providing a valid user name, password and workspace identification, this route will return " +
                    "an authentication token that must be used (and reused) in future API calls that require authentication. " +
                    "The authentication token will expire after 24 hours idle time, but as long as you keep using the same authentication " +
                    "token within 24h idle-time, it will never expire.<p>" +
                    "When calling an API that requires authentication, the authentication token must be included in the request as a cookie " +
                    "or a query param using the name authToken. If an invalid token is sent, a 402 (invalid authentication token) will be returned" +
                    "from the request."
    )
    public String login(AuthenticationForm form) {
        String token = authenticationService.login(form.getLogin(), form.getPassword(), form.getWorkspace());

        return token;
    }


    @Path("/workspaces")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiDocMethod(summary = "Return the list of workspaces available for a valid user name and password")
    public List<AuthWorkspace> getWorkspaces(AuthenticationForm form) {
        return authenticationService.getWorkspaces(form.getLogin(), form.getPassword());
    }
}
