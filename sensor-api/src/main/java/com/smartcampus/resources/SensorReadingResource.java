package com.smartcampus.resources;

import com.smartcampus.database.DatabaseClass;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import com.smartcampus.exceptions.SensorUnavailableException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sub-resource class responsible for managing SensorReading entities.
 * Instantiated by SensorResource via a sub-resource locator — not registered directly.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId; // Injected by the parent SensorResource locator
    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private Map<String, List<SensorReading>> allReadings = DatabaseClass.getSensorReadings();

    /**
     * Constructor called by the sub-resource locator in SensorResource.
     * Receives the sensorId from the parent URL path segment.
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full historical log of readings for the specified sensor.
     */
    @GET
    public Response getReadings() {
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"errorMessage\": \"Sensor not found\", \"errorCode\": 404}")
                    .build();
        }

        // Return the reading history, or an empty list if no readings exist yet
        List<SensorReading> readings = allReadings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to the sensor's history and updates the parent
     * sensor's currentValue as a side effect.
     * Throws SensorUnavailableException (403) if the sensor is in MAINTENANCE status.
     */
    @POST
    public Response addReading(SensorReading reading) {
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"errorMessage\": \"Sensor not found\", \"errorCode\": 404}")
                    .build();
        }

        Sensor parentSensor = sensors.get(sensorId);

        // Reject readings from sensors in MAINTENANCE — hardware is physically offline
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is currently in MAINTENANCE mode. Physically disconnected hardware cannot accept new readings.");
        }

        // Persist the reading to the historical log
        allReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // Side effect: sync the parent sensor's currentValue with the latest measurement
        parentSensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}