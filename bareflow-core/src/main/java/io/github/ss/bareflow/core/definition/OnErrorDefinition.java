package io.github.ss.bareflow.core.definition;

import java.util.Objects;

/**
 * Error handling strategy for a flow.
 * Pure specification model with no execution logic.
 */
public class OnErrorDefinition {
    /** Strategy type: STOP, CONTINUE, RETRY */
    private Strategy strategy;

    /** Used only when strategy == CONTINUE */
    private String nextStepId;

    /** Used only when strategy == RETRY */
    private RetryPolicy retryPolicy;

    public OnErrorDefinition() {
        // NOP
    }

    public OnErrorDefinition(Strategy strategy, String nextStepId, RetryPolicy retryPolicy) {
        this.strategy = strategy;
        this.nextStepId = nextStepId;
        this.retryPolicy = retryPolicy;
    }

    // ===== Getters / Setters =====
    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public String getNextStepId() {
        return nextStepId;
    }

    public void setNextStepId(String nextStepId) {
        this.nextStepId = nextStepId;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    // ===== Utility =====
    public boolean isStop() {
        return strategy == Strategy.STOP;
    }

    public boolean isContinue() {
        return strategy == Strategy.CONTINUE;
    }

    public boolean isRetry() {
        return strategy == Strategy.RETRY;
    }

    @Override
    public String toString() {
        return "OnErrorDefinition{" +
                "strategy=" + strategy +
                ", nextStepId='" + nextStepId + '\'' +
                ", retryPolicy=" + retryPolicy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OnErrorDefinition))
            return false;
        OnErrorDefinition that = (OnErrorDefinition) o;
        return strategy == that.strategy &&
                Objects.equals(nextStepId, that.nextStepId) &&
                Objects.equals(retryPolicy, that.retryPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategy, nextStepId, retryPolicy);
    }

    // ===== Strategy Enum =====
    public enum Strategy {
        STOP,
        CONTINUE,
        RETRY
    }
}