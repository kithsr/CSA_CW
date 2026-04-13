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
import com.smartcampus.exceptions.RoomNotEmptyException; // <-- NEW: Added Part 5 exception import

@Path("/rooms") 
@Produces(MediaType.APPLICATION_JSON) 
@Consumes(MediaType.APPLICATION_JSON) 
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
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Room ID is required\"}")
                           .build();
        }
        
        rooms.put(room.getId(), room);
        
        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri).entity(room).build();
    }

    // 3. GET /api/v1/rooms/{roomId} : Fetch detailed metadata for a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room with ID " + roomId + " was not found in the system.");
        }
        return Response.ok(room).build();
    }

    // 4. DELETE /api/v1/rooms/{roomId} : Room Deletion & Safety Logic
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build(); 
        }
        
        // NEW PART 5 LOGIC: Throwing the custom exception to trigger our Mapper!
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("The room is currently occupied by active hardware. Please remove all sensors before decommissioning the room.");
        }
        
        rooms.remove(roomId);
        return Response.noContent().build(); 
    }

    // 5. GET /api/v1/rooms/{roomId}/sensors : Deep Nesting (Part 4)
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