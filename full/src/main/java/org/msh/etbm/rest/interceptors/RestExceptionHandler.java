package org.msh.etbm.rest.interceptors;

import org.codehaus.jackson.JsonParseException;
import org.msh.etbm.rest.exceptions.BadRequestException;
import org.msh.etbm.rest.exceptions.UnauthorizedException;

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
        Exception cause = (Exception)e.getCause();
        if (cause instanceof UnauthorizedException) {
            return Response
                    .status(401)
                    .entity("Invalid user name or password for the given workspace")
                    .build();
        }

        if (cause instanceof BadRequestException) {
            return Response
                    .status(400)
                    .entity("Bad request")
                    .build();
        }

        if (cause instanceof JsonParseException) {
            JsonParseException jsonError = (JsonParseException)cause;
            return Response
                    .status(400)
                    .entity(jsonError.getMessage())
                    .build();
        }

        // general error
        return Response
                .status(500)
                .entity("Internal server error. The system administrator will be notified about this error.")
                .build();
    }
}
