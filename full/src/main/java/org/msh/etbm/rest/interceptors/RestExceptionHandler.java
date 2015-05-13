package org.msh.etbm.rest.interceptors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * General exception handler
 * Created by rmemoria on 5/5/15.
 */
@Provider
public class RestExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        return Response
                .status(500)
                .entity("Internal server error. The system administrator will be notified about this error.")
                .build();
    }
}
