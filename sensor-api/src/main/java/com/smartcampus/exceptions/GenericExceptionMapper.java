package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    
    @Override
    public Response toResponse(Throwable exception) {
        // Let JAX-RS built-in exceptions (415, 404, 405 etc.) pass through correctly
        if (exception instanceof javax.ws.rs.WebApplicationException) {
            return ((javax.ws.rs.WebApplicationException) exception).getResponse();
        }

        ErrorMessage errorMessage = new ErrorMessage("An unexpected internal server error occurred. Please contact support if the issue persists.", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorMessage)
                    .build();
    }
}
