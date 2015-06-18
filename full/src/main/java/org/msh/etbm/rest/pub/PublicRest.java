package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.commons.apidoc.annotations.ApiDocQueryParam;
import org.msh.etbm.rest.StandardResult;
import org.msh.etbm.services.pub.SendNewPasswordService;
import org.msh.etbm.services.pub.SupportedLocales;
import org.msh.etbm.services.pub.UserRegistrationService;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Set of pub rest api
 *
 * Created by ricardo on 24/11/14.
 */
@ApiDoc(summary = "public api for GUI supporting. No authentication required", group = "public")
@Name("publicRest")
@Path("/pub")
public class PublicRest {

    /**
     * Send a new password to the user by its e-mail address
     * @param email the user's e-mail address assigned to its account in the system
     * @return StandardResult containing the success or error (in case the e-mail is invalid)
     */
    @ApiDocMethod(summary = "Send a new password to an user from the e-mail address assigned to its account")
    @Path("/newpassword")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public StandardResult sendNewPassword(
            @ApiDocQueryParam("User's e-mail address assigned to his/her account")
            @QueryParam("email") String email) {
        try {
            SendNewPasswordService srv = (SendNewPasswordService) App.getComponent("sendNewPasswordService");

            boolean success = srv.execute(email);

            return new StandardResult(success, null);
        }
        catch (Exception e) {
            return new StandardResult(false, e.getMessage());
        }
    }


    @ApiDocMethod(summary = "Register a new user by assigning default configurations",
    description = "The availability of this API depends on system configurations and may not be available for security reasons. " +
            "This API is mostly used to allow an user to self-register based on default system configurations, like workspace," +
            " health facility and user profile. Once registered, the system will send an e-mail with the user's temporary password" +
            " to enter in the system.")
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


    @ApiDocMethod(summary="Return information about the running instance" )
    @Path("/about")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public AboutResult about() {
        EtbmanagerApp app = EtbmanagerApp.instance();
        AboutResult res = new AboutResult();

        res.setBasePath(app.getConfiguration().getPageRootURL());
        res.setBuildDate(app.getBuildDate());
        res.setBuildNumber(app.getBuildNumber());
        res.setCountryCode(app.getCountryCode());
        res.setImplementationTitle(app.getImplementationTitle());
        res.setImplementationVersion(app.getImplementationVersion());

        SupportedLocales locales = (SupportedLocales)App.getComponent("supportedLocales");
        List<LocaleInfo> lst = new ArrayList<LocaleInfo>();
        for (Locale locale: locales.getLocales()) {
            LocaleInfo inf = new LocaleInfo();
            inf.setId(locale.getLanguage());
            inf.setName(locale.getDisplayName());
            lst.add(inf);
        }

        if (lst.size() > 0) {
            res.setSuportedLocales(lst);
        }

        return res;
    }

}
