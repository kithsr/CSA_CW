package com.smartcampus.models;

import java.util.UUID;

public class SensorReading {
    private String id;        // Unique reading event ID
    private long timestamp;   // Epoch time (ms) when captured
    private double value;     // The actual metric value

    // Empty constructor required for JSON parsing
    public SensorReading() {
        // Auto-generate a UUID if one isn't provided
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
