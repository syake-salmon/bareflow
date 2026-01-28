package io.github.ss.bareflow.core.definition;

import java.util.Objects;

/**
 * Retry policy for a step or flow.
 * Pure specification model with no execution logic.
 */
public class RetryPolicy {
    /** Maximum number of retry attempts (>= 1) */
    private int maxAttempts;

    /** Backoff duration in milliseconds between attempts */
    private long backoffMillis;

    public RetryPolicy() {
        // NOP
    }

    public RetryPolicy(int maxAttempts, long backoffMillis) {
        this.maxAttempts = maxAttempts;
        this.backoffMillis = backoffMillis;
    }

    // ===== Getters / Setters =====
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }

    public void setBackoffMillis(long backoffMillis) {
        this.backoffMillis = backoffMillis;
    }

    // ===== Utility =====
    public boolean isValid() {
        return maxAttempts > 0 && backoffMillis >= 0;
    }

    @Override
    public String toString() {
        return "RetryPolicy{" +
                "maxAttempts=" + maxAttempts +
                ", backoffMillis=" + backoffMillis +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RetryPolicy))
            return false;
        RetryPolicy that = (RetryPolicy) o;
        return maxAttempts == that.maxAttempts &&
                backoffMillis == that.backoffMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAttempts, backoffMillis);
    }
}
