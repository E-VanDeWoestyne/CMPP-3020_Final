package exceptions;

public class CapacityExceededException extends Exception {
    public CapacityExceededException(String room_id, int capacity, String event_name, int attendees) {
        super(String.format("CAPACITY ERROR: Room %s (Capacity: %d) is too small for '%s' (Attendees: %d).", 
              room_id, capacity, event_name, attendees));
    }
}