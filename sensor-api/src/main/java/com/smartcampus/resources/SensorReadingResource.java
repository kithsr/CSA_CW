package com.smartcampus.resources;

import com.smartcampus.database.DatabaseClass;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;
    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private Map<String, List<SensorReading>> allReadings = DatabaseClass.getSensorReadings();

    // The constructor catches the ID passed down from the parent
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET / : Fetch the history
    @GET
    public Response getReadings() {
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"errorMessage\": \"Sensor not found\", \"errorCode\": 404}")
                    .build();
        }
        
        // Return the list of readings, or an empty list if none exist yet
        List<SensorReading> readings = allReadings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    // POST / : Append new reading AND update parent
    @POST
    public Response addReading(SensorReading reading) {
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"errorMessage\": \"Sensor not found\", \"errorCode\": 404}")
                    .build();
        }

        // 1. Save the reading to the historical log
        allReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // 2. THE SIDE EFFECT: Update the parent Sensor's currentValue
        Sensor parentSensor = sensors.get(sensorId);
        parentSensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}