package dev.sim0n.caesium.exception;

import lombok.Getter;

@Getter
public class CaesiumMissingDependencyException extends CaesiumException {

    public static final String MESSAGE_CONCAT_DEFAULT = "is missing in the classpath.";

    private final String reference;

    public CaesiumMissingDependencyException(final String reference) {
        this(reference, MESSAGE_CONCAT_DEFAULT);
    }
    public CaesiumMissingDependencyException(String reference, String messageConcat) {
        this(reference, messageConcat, null);
    }

    public CaesiumMissingDependencyException(String reference, String messageConcat, Throwable cause) {
        super(reference+" "+messageConcat, cause);
        this.reference = reference;
    }


}
