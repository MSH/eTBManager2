package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.SystemConfigHome;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
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
    @Path("/hello/{id}")
    @Produces("application/json")
    public RestData hello(@PathParam("id") Integer id) {
        System.out.println("id = " + id);
        RestData data = new RestData();
        data.setAge(40);
        data.setBirthDate(new Date());
        data.setMiddleName("Memoria");
        data.setName(systemConfigHome.getSystemConfig().getAdminMail());
        data.setProp1(null);
        return data;
    }
}
