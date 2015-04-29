package org.msh.etbm.rest.quantb;

import org.jboss.seam.annotations.Name;
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
public class QuantbRest {

    @Path("/export")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @POST
    public StandardResult export() {
        QuantbServices srv = (QuantbServices) App.getComponent("quantbServices");
        QuantbData data = srv.export();

        return new StandardResult(true, data);
    }

}
