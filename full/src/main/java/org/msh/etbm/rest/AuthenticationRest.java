package org.msh.etbm.rest;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.services.auth.AuthenticationService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST API to cover user authentication operations
 *
 * Created by ricardo on 23/11/14.
 */
@Name("authenticationRest")
@Path("/auth")
public class AuthenticationRest {

    @In AuthenticationService authenticationService;

    /**
     * Log user in the system by checking its username, password and workspace, and if user
     * successfully logs, returns its token id
     * @param form instance of AuthenticationForm containing user credentials
     * @return the token ID inside the standard result
     */
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public StandardResult login(AuthenticationForm form) {
        String token = authenticationService.login(form.getLogin(), form.getPassword(), form.getWorkspace());

        return new StandardResult(token != null, token);
    }


    /**
     * Logs the user out of the system, i.e, invalidate the user token
     * @param token a valid user token
     * @return
     */
    public StandardResult logout(@QueryParam("tk") String token) {
        return new StandardResult(true, null);
    }
}
