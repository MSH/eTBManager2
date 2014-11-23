package org.msh.tb.test;

import org.jboss.seam.annotations.Name;

import javax.ws.rs.*;
import java.util.Date;

/**
 * Created by ricardo on 23/11/14.
 */
@Path("/test")
@Name("restTest")
public class RestTest {

    @GET
    @Path("/hello/{id}")
    @Produces("application/json")
    public RestData hello(@PathParam("id") Integer id) {
        System.out.println("id = " + id);
        RestData data = new RestData();
        data.setAge(40);
        data.setBirthDate(new Date());
        data.setMiddleName("Memoria");
        data.setName("Ricardo");
        data.setProp1(null);
        return data;
    }
}
