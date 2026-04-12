package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider 
public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException> {

    @Override
    public Response toResponse(DataNotFoundException ex) {
        // Automatically packages the error into our clean JSON model
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 404);
        
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(errorMessage)
                       .build();
    }
}