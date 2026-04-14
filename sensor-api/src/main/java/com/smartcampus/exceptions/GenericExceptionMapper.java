package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // In a real production environment, you would log the actual exception here 
        // to a secure internal file so developers can debug it later.
        // System.err.println("CRITICAL INTERNAL ERROR: " + exception.getMessage());
        // exception.printStackTrace();

        // Create a completely generic, safe error message for the external client
        ErrorMessage errorMessage = new ErrorMessage("An unexpected internal server error occurred. Please contact support if the issue persists.", 500);
        
        // 500 Internal Server Error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR) 
                       .entity(errorMessage)
                       .build();
    }
}
