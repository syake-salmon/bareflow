package io.github.ss.bareflow.core.definition;

/**
 * Retry policy for a step.
 * Immutable model representing retry behavior.
 */
public class RetryPolicy {
    private final int maxAttempts;
    private final long backoffMillis;

    public RetryPolicy(int maxAttempts, long backoffMillis) {
        this.maxAttempts = maxAttempts;
        this.backoffMillis = backoffMillis;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }
}