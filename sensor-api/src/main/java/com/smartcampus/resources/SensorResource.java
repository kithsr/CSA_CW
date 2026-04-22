package com.smartcampus.resources;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;

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

/**
 * JAX-RS resource class for managing Sensor entities.
 * Exposes registration and retrieval operations under /api/v1/sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // In-memory data stores shared across the application
    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the referenced roomId exists
     * before persisting — throws LinkedResourceNotFoundException (422) if not.
     */
    @POST
    public Response addSensor(Sensor sensor, @Context UriInfo uriInfo) {

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Sensor ID is required\"}")
                           .build();
        }

        // Validate referential integrity: the parent room must exist before linking the sensor
        if (sensor.getRoomId() == null || !rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Cannot create sensor: The specified roomId '" + sensor.getRoomId() + "' does not exist in the system.");
        }

        sensors.put(sensor.getId(), sensor);

        // Register this sensor ID on the parent room to maintain the bidirectional link
        Room room = rooms.get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        // Return 201 Created with Location header pointing to the new resource
        URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors?type={type}
     * Returns all sensors, optionally filtered by type using a query parameter.
     * @QueryParam is used over @PathParam because type is an optional filter on a
     * collection, not an identifier for a specific resource.
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());

        // Apply type filter if provided
        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                                   .filter(s -> type.equalsIgnoreCase(s.getType()))
                                   .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    /**
     * Sub-Resource Locator for /api/v1/sensors/{sensorId}/readings
     * This method has no HTTP verb — it delegates handling to SensorReadingResource,
     * passing the sensorId down via constructor injection.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}