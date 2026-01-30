package run.bareflow.core.exception;

/**
 * Exception thrown when a step fails during execution.
 * This is a recoverable exception and may trigger retry logic
 * depending on the step's RetryPolicy or flow-level OnErrorDefinition.
 */
public class StepExecutionException extends RuntimeException {
    public StepExecutionException(String message) {
        super(message);
    }

    public StepExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StepExecutionException(Throwable cause) {
        super(cause);
    }
}
