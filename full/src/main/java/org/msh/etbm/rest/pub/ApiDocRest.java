package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.etbm.commons.apidoc.ApiDocGenerator;
import org.msh.etbm.commons.apidoc.model.ApiDocument;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates automatic documentation about the REST API
 *
 * Created by rmemoria on 28/4/15.
 */
@Name("restDocRest")
@BypassInterceptors
@Path("/pub")
public class ApiDocRest {

    public ApiDocRest() {
    }

    @Path("/apidoc")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public ApiDocument generateDoc() {
        EtbmanagerApp app = (EtbmanagerApp) App.getComponent("etbmanagerApp");

        ApiDocGenerator gen = new ApiDocGenerator();
        try {
            ApiDocument doc = gen.generate("org.msh.etbm.rest", app.getConfiguration().getSystemURL(), app.getImplementationVersion());

            return doc;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
