package exceptions;

public class RoomUnavailableException extends Exception {
    public RoomUnavailableException(String message) {
        super("AVAILABILITY ERROR: " + message);
    }
}