package kz.edu.parking.common;

public class GarageException extends RuntimeException {
    public GarageException(String message) {
        super(message);
    }

    public GarageException(String message, Throwable cause) {
        super(message, cause);
    }
}
