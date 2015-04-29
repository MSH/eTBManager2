package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
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
    public Object generateDoc() {
        return null;
/*
        EtbmanagerApp app = EtbmanagerApp.instance();

        String version = app.getImplementationVersion();
        String basePath = app.getConfiguration().getSystemURL() + "/resources/api";
        List<String> packages = new ArrayList<String>();
        packages.add("org.msh.etbm.rest");
        boolean playgroundEnabled = true;
        JSONDoc.MethodDisplay methodDisplay = JSONDoc.MethodDisplay.URI;

        return docScanner.getJSONDoc(version, basePath, packages, playgroundEnabled, methodDisplay);
*/
    }
}
