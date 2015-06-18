package org.msh.etbm.rest.quantb;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.rest.authentication.Authenticated;
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
        group = "quantb",
        summary = "Support for QuanTB integration with e-TB Manager")
@Authenticated
public class QuantbRest {

    @Path("/export")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @POST
    @ApiDocMethod(summary = "Return data to perform medicine forecasting",
    description = "This API is primarily used by QuanTB to promote integration with e-TB Manager. " +
            "This API returns all information available in e-TB Manager in order to create a new forecasting " +
            "in QuanTB. This information includes: Medicines, Treatment regimens, Number of cases on treatment, Medicine inventory")
    public QuantbData export() {
        QuantbServices srv = (QuantbServices) App.getComponent("quantbServices");
        QuantbData data = srv.export();

        return data;
    }

}
