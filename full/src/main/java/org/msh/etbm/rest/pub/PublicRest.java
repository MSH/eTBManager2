package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.commons.apidoc.annotations.ApiDocReturn;
import org.msh.etbm.rest.StandardResult;
import org.msh.etbm.services.pub.SendNewPasswordService;
import org.msh.etbm.services.pub.UserRegistrationService;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Set of pub rest api
 *
 * Created by ricardo on 24/11/14.
 */
@ApiDoc(description = "public api for GUI supporting. No authentication required", group = "public")
@Name("publicRest")
@Path("/pub")
public class PublicRest {

    /**
     * Send a new password to the user by its e-mail address
     * @param email the user's e-mail address assigned to its account in the system
     * @return StandardResult containing the success or error (in case the e-mail is invalid)
     */
    @ApiDocMethod(description = "Send a new password to an user from the e-mail address assigned to its account",
        returnCodes = {
                @ApiDocReturn(statusCode = "200", description = "Successfully executed"),
                @ApiDocReturn(statusCode = "500", description = "Unexpected error")
        })
    @Path("/newpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public StandardResult sendNewPassword(@QueryParam("email") String email) {
        try {
            SendNewPasswordService srv = (SendNewPasswordService) App.getComponent("sendNewPasswordService");

            boolean success = srv.execute(email);

            return new StandardResult(success, null);
        }
        catch (Exception e) {
            return new StandardResult(false, e.getMessage());
        }
    }


    @ApiDocMethod(description = "Self registration of a user, if configured in the system to accept it")
    @Path("/registeruser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public StandardResult registerNewUser(@Valid RegistrationForm form) {
        try {
            UserRegistrationService srv = (UserRegistrationService)App.getComponent("userRegistrationService");
            srv.register(form.getName(), form.getLogin(), form.getEmail(), form.getOrganization());

            return new StandardResult(true, null);
        }
        catch (Exception e) {
            return new StandardResult(false, e.getMessage());
        }
    }


    @ApiDocMethod(description="Return information about the running instance")
    @Path("/about")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public AboutResult about() {
        EtbmanagerApp app = EtbmanagerApp.instance();
        AboutResult res = new AboutResult();

        res.setBuildDate(app.getBuildDate());
        res.setBuildNumber(app.getBuildNumber());
        res.setCountryCode(app.getCountryCode());
        res.setImplementationTitle(app.getImplementationTitle());
        res.setImplementationVersion(app.getImplementationVersion());
        return res;
    }

}
