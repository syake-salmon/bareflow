package run.bareflow.core.exception;

/**
 * Represents a business-level error that occurs during step execution.
 * This exception indicates a logical or domain-specific failure and
 * should not trigger retry logic.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
}
