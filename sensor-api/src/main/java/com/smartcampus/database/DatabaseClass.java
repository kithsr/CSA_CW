package com.smartcampus.database;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseClass {
    // Thread-safe maps for our in-memory database
    private static Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    
    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }
}