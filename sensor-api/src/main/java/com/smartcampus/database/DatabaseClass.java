package com.smartcampus.database;

import com.smartcampus.models.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseClass {
    // We MUST use ConcurrentHashMap to prevent thread collisions
    private static Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    public static Map<String, Room> getRooms() {
        return rooms;
    }
}