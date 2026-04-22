package com.smartcampus.models;

import java.util.UUID;

/**
 * Represents a single timestamped measurement recorded by a Sensor.
 * Each reading is immutable in practice — it forms part of the historical audit log.
 */
public class SensorReading {

    private String id;        // Unique reading event ID (UUID recommended)
    private long timestamp;   // Epoch time in milliseconds when the reading was captured
    private double value;     // The actual metric value recorded by the hardware

    /**
     * Default constructor required by Jackson for JSON deserialisation.
     * Automatically assigns a UUID and the current timestamp if not provided.
     */
    public SensorReading() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
