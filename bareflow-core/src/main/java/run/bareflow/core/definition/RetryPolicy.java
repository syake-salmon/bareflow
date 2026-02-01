package run.bareflow.core.definition;

/**
 * Immutable model representing the retry behavior for a step.
 *
 * RetryPolicy applies only to system-level failures:
 * - SystemException
 * - StepExecutionException
 *
 * BusinessException is never retried.
 *
 * Semantics:
 * - maxAttempts represents the total number of attempts, including the first.
 * For example, maxAttempts = 3 means:
 * attempt 1 → failure
 * attempt 2 → retry
 * attempt 3 → retry
 * attempt 4 → no further retry
 *
 * - delayMillis defines the delay between retry attempts.
 *
 * RetryPolicy is optional at the step level.
 * If null, no automatic retry is performed.
 */
public class RetryPolicy {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryPolicy(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getDelayMillis() {
        return delayMillis;
    }
}