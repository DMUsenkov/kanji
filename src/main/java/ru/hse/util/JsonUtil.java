package ru.hse.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JSON serialization and deserialization
 */
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convert an object to JSON string
     * 
     * @param object The object to serialize
     * @return JSON string or null if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing object to JSON");
            return null;
        }
    }
    
    /**
     * Convert JSON string to an object
     * 
     * @param json The JSON string
     * @param clazz The class to deserialize to
     * @return The deserialized object or null if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing JSON to object");
            return null;
        }
    }
}
