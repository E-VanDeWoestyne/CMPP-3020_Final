package exceptions;

public class InvalidDataException extends Exception {
    public InvalidDataException(String message){
        super("INVALID DATA: " + message);
    }
}
