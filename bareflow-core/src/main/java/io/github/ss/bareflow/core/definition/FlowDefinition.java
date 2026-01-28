package io.github.ss.bareflow.core.definition;

import java.util.List;

/**
 * Flow-level definition.
 * Immutable model representing an entire flow.
 */
public class FlowDefinition {
    private final String name;
    private final List<StepDefinition> steps;
    private final OnErrorDefinition onError; // optional flow-level default

    public FlowDefinition(String name,
            List<StepDefinition> steps,
            OnErrorDefinition onError) {
        this.name = name;
        this.steps = List.copyOf(steps);
        this.onError = onError;
    }

    public String getName() {
        return name;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public OnErrorDefinition getOnError() {
        return onError;
    }
}