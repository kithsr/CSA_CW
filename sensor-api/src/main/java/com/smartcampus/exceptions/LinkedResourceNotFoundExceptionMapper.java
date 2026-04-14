package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        // Create our custom JSON error body with the 422 status code
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 422);
        
        // 422 Unprocessable Entity
        return Response.status(422) 
                       .entity(errorMessage)
                       .build();
    }
}