package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.rest.StandardResult;
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
public class AuthenticationRest {

    @In AuthenticationService authenticationService;

    /**
     * Log user in the system by checking its username, password and workspace, and if user
     * successfully logs, returns its token id
     * @param form instance of AuthenticationForm containing user credentials
     * @return the token ID inside the standard result
     */
    @Path("/login")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @POST
    public StandardResult login(AuthenticationForm form) {
        String token = authenticationService.login(form.getLogin(), form.getPassword(), form.getWorkspace());

        return new StandardResult(token != null, token);
    }


    @Path("/workspaces")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @POST
    public StandardResult getWorkspaces(AuthenticationForm form) {
        try {
            List<AuthWorkspace> lst = authenticationService.getWorkspaces(form.getLogin(), form.getPassword());

            return new StandardResult(true, lst);
        }
        catch (Exception e) {
            return new StandardResult(false, e.getMessage());
        }
    }
}
