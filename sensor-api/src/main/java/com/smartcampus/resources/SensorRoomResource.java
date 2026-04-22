package com.smartcampus.resources;

import com.smartcampus.database.DatabaseClass;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor; 
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.smartcampus.exceptions.DataNotFoundException;
import com.smartcampus.exceptions.RoomNotEmptyException;

/**
 * JAX-RS resource class for managing Room entities.
 * Exposes CRUD operations under the /api/v1/rooms path.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // In-memory ConcurrentHashMap acting as the data store for rooms
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    /**
     * GET /api/v1/rooms
     * Returns a list of all rooms currently registered in the system.
     */
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room and returns 201 Created with a Location header.
     */
    @POST
    public Response addRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Room ID is required\"}")
                           .build();
        }

        rooms.put(room.getId(), room);

        // Build the Location header URI pointing to the newly created resource
        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Retrieves a specific room by its ID. Throws DataNotFoundException (404) if not found.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room with ID " + roomId + " was not found in the system.");
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room only if it has no active sensors.
     * Throws RoomNotEmptyException (409) to enforce referential integrity.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Prevent deletion if sensors still reference this room
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("The room is currently occupied by active hardware. Please remove all sensors before decommissioning the room.");
        }

        rooms.remove(roomId);
        return Response.noContent().build();
    }

    /**
     * GET /api/v1/rooms/{roomId}/sensors
     * Returns all sensors deployed in the specified room (deep nesting).
     */
    @GET
    @Path("/{roomId}/sensors")
    public Response getSensorsInRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Room not found\"}")
                           .build();
        }

        Map<String, Sensor> allSensors = DatabaseClass.getSensors();
        List<Sensor> roomSensors = new ArrayList<>();

        for (String sensorId : room.getSensorIds()) {
            if (allSensors.containsKey(sensorId)) {
                roomSensors.add(allSensors.get(sensorId));
            }
        }

        return Response.ok(roomSensors).build();
    }
}