package org.msh.etbm.rest.gxalert;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.rest.StandardResult;
import org.msh.etbm.services.gxalert.GxalertService;
import org.msh.tb.entities.GxalertData;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Rest API to promote integration between gxalert and etbmanager
 *
 * Created by ricardo on 12/12/14.
 */
@Name("gxalertRest")
@Path("/gxalert")
public class GxalertRest {

    /**
     * Sasve data sent from gxalert
     * @param form form containing the data to be saved
     * @return instance of StandardResult
     */
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public StandardResult add(GxalertForm form) {
        GxalertData data = new GxalertData();
        data.setAssay(form.getAssay());
        data.setAssayHostTestCode(form.getAssayHostTestCode());
        try {
            PropertyUtils.copyProperties(data, form);
        } catch (Exception e) {
            e.printStackTrace();
            new StandardResult(false, e.getMessage());
        }

        GxalertService srv = (GxalertService) Component.getInstance("gxalertService");

        srv.saveData(data);

        return new StandardResult(true, data.getId());
    }
}
