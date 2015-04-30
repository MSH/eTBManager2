package org.msh.etbm.rest.quantb;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.rest.StandardResult;
import org.msh.etbm.services.quantb.QuantbData;
import org.msh.etbm.services.quantb.QuantbServices;
import org.msh.tb.application.App;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by rmemoria on 27/4/15.
 */
@Name("quantbRest")
@Path("/quantb")
@ApiDoc(
        group = "auth - QuanTB",
        description = "Support for QuanTB integration with e-TB Manager")
public class QuantbRest {

    @Path("/export")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiDocMethod(description = "Return data necessary to forecast medicine needs (medicines, regimens, cases and inventory). Authentication is required")
    public QuantbData export() {
        QuantbServices srv = (QuantbServices) App.getComponent("quantbServices");
        QuantbData data = srv.export();

        return data;
    }

}
