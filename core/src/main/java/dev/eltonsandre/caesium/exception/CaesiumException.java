package dev.eltonsandre.caesium.exception;

public class CaesiumException extends RuntimeException {

    public CaesiumException(String message) {
        super(message);
    }

    public CaesiumException(String message, Throwable cause) {
        super(message, cause);
    }

}
