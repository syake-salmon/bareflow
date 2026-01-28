package io.github.ss.bareflow.core.definition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pure specification model representing a flow.
 * This class intentionally contains no execution logic.
 */
public class FlowDefinition {
    /** Unique identifier of the flow */
    private String id;

    /** Ordered list of steps */
    private List<StepDefinition> steps;

    /** Optional error handling strategy */
    private OnErrorDefinition onError;

    /** Optional metadata (free-form) */
    private Map<String, Object> metadata;

    public FlowDefinition() {
        // NOP
    }

    public FlowDefinition(String id,
            List<StepDefinition> steps,
            OnErrorDefinition onError,
            Map<String, Object> metadata) {
        this.id = id;
        this.steps = steps;
        this.onError = onError;
        this.metadata = metadata;
    }

    // ===== Getters / Setters =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDefinition> steps) {
        this.steps = steps;
    }

    public OnErrorDefinition getOnError() {
        return onError;
    }

    public void setOnError(OnErrorDefinition onError) {
        this.onError = onError;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // ===== Utility =====
    public boolean hasOnError() {
        return onError != null;
    }

    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }

    @Override
    public String toString() {
        return "FlowDefinition{" +
                "id='" + id + '\'' +
                ", steps=" + steps +
                ", onError=" + onError +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FlowDefinition))
            return false;
        FlowDefinition that = (FlowDefinition) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(steps, that.steps) &&
                Objects.equals(onError, that.onError) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, steps, onError, metadata);
    }
}
