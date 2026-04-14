package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 403);
        
        // 403 Forbidden
        return Response.status(Response.Status.FORBIDDEN) 
                       .entity(errorMessage)
                       .build();
    }
}