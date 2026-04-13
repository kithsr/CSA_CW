package com.smartcampus.exceptions;

import com.smartcampus.models.ErrorMessage; // Assuming you made this earlier!
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Create our custom JSON error body
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 409);
        
        return Response.status(Response.Status.CONFLICT) // 409 Conflict
                       .entity(errorMessage)
                       .build();
    }
}
