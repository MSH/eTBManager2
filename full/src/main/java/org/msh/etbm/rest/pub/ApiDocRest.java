package org.msh.etbm.rest.pub;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.etbm.commons.apidoc.ApiDocGenerator;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.commons.apidoc.model.ApiDocument;
import org.msh.etbm.commons.apidoc.model.ApiGroup;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@ApiDoc(group = "public.doc",
        summary = "Routes to support the REST API on-line documentation. Also a way to discovery services")
public class ApiDocRest {

    public ApiDocRest() {
    }

    @Path("/apidoc/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @ApiDocMethod(summary = "Return a JSON object containing information about all routes available in the system")
    public ApiDocument returnGroups() {
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


    @Path("/apidoc/routes")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @ApiDocMethod(summary = "Return the methods of a given group name (in JSON format)")
    public ApiGroup getMethodsGroup(@QueryParam("grp") String groupname) {
        ApiDocGenerator gen = new ApiDocGenerator();

        ApiGroup grp = gen.generateGroupInfo(groupname);

        return grp;
    }
}
