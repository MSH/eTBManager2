package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.SystemConfigHome;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;

/**
 * Created by ricardo on 23/11/14.
 */
@Path("/test")
@Name("restTest")
public class RestTest {

    @In(create = true)
    SystemConfigHome systemConfigHome;

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public RestData hello() {
        RestData data = new RestData();
        data.setAge(40);
        data.setBirthDate(new Date());
        data.setMiddleName("Memoria");
        data.setName(systemConfigHome.getSystemConfig().getAdminMail());
        data.setProp1(null);
        return data;
    }
}
