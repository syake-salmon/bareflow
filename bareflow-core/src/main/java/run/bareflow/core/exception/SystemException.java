package run.bareflow.core.exception;

/**
 * Represents a system-level error that occurs during step execution.
 * This exception indicates an infrastructure or transient failure and
 * may trigger retry logic depending on the step's RetryPolicy or
 * flow-level OnErrorDefinition.
 */
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}
