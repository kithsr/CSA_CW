package com.smartcampus.resources;

import com.smartcampus.database.DatabaseClass;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import javax.ws.rs.*; // Using javax.ws.rs to match the coursework rubric perfectly!
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    // 1. POST /api/v1/sensors : Register a new sensor
    @POST
    public Response addSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Business Logic: Verify the room actually exists before creating the sensor
        if (sensor.getRoomId() == null || !rooms.containsKey(sensor.getRoomId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Invalid roomId. The specified room does not exist.\"}")
                           .build();
        }

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Sensor ID is required\"}")
                           .build();
        }

        sensors.put(sensor.getId(), sensor);

        // Automatically link the sensor to the room's internal list
        Room room = rooms.get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri).entity(sensor).build();
    }

    // 2. GET /api/v1/sensors?type=... : Filtered Retrieval
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());

        // Business Logic: If the user provided a query parameter, filter the list
        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                                   .filter(s -> type.equalsIgnoreCase(s.getType()))
                                   .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }
}