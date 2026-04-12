package com.smartcampus.resources;

import com.smartcampus.database.DatabaseClass;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor; // <-- Added this crucial import
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/rooms") // This maps to /api/v1/rooms
@Produces(MediaType.APPLICATION_JSON) // Every response from here will be JSON
@Consumes(MediaType.APPLICATION_JSON) // Expects incoming data to be JSON
public class SensorRoomResource {

    // Access our thread-safe "database"
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    // 1. GET /api/v1/rooms : Provide a comprehensive list of all rooms
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    // 2. POST /api/v1/rooms : Enable creation of new rooms
    @POST
    public Response addRoom(Room room, @Context UriInfo uriInfo) {
        // Ensure the room has an ID
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Room ID is required\"}")
                           .build();
        }
        
        // Save to our in-memory map
        rooms.put(room.getId(), room);
        
        // Best Practice: Return a 201 Created status with the Location header pointing to the new resource
        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri).entity(room).build();
    }

    // 3. GET /api/v1/rooms/{roomId} : Fetch detailed metadata for a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Room not found\"}")
                           .build();
        }
        return Response.ok(room).build();
    }

    // 4. DELETE /api/v1/rooms/{roomId} : Room Deletion & Safety Logic
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        if (room == null) {
            // Idempotent behavior: If it doesn't exist, we still consider the deletion "successful" conceptually
            return Response.status(Response.Status.NOT_FOUND).build(); 
        }
        
        // Business Logic Constraint: Prevent data orphans
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            // Returns 409 Conflict. (We will upgrade this to a custom exception in Part 5)
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\": \"Cannot delete room: active sensors are assigned to it.\"}")
                           .build();
        }
        
        rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content represents a successful deletion
    }

    // 5. GET /api/v1/rooms/{roomId}/sensors : Deep Nesting (Part 4)
    @GET
    @Path("/{roomId}/sensors")
    public Response getSensorsInRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        // 1. Check if the room exists
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Room not found\"}")
                           .build();
        }

        // 2. Fetch the actual sensor objects that belong to this room
        Map<String, Sensor> allSensors = DatabaseClass.getSensors();
        List<Sensor> roomSensors = new ArrayList<>();

        for (String sensorId : room.getSensorIds()) {
            if (allSensors.containsKey(sensorId)) {
                roomSensors.add(allSensors.get(sensorId));
            }
        }

        // 3. Return the full list of sensor data
        return Response.ok(roomSensors).build();
    }
}