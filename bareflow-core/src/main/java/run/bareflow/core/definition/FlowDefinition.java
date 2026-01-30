package run.bareflow.core.definition;

import java.util.List;
import java.util.Map;

/**
 * Immutable model representing an entire flow definition.
 *
 * FlowDefinition is a pure structural representation of a flow:
 * - name: logical identifier of the flow
 * - steps: ordered list of StepDefinition objects
 * - onError: optional flow-level default error handling policy
 * - metadata: optional user-defined metadata with no effect on execution
 *
 * This class contains no behavior and no validation logic.
 * Validation, loading, and parsing are responsibilities of the
 * FlowDefinitionResolver or higher-level runtime components.
 *
 * FlowEngine consumes FlowDefinition as-is and executes steps sequentially.
 */
public class FlowDefinition {
    private final String name;
    private final List<StepDefinition> steps;
    private final OnErrorDefinition onError; // optional flow-level default
    private final Map<String, Object> metadata; // optional metadata

    public FlowDefinition(
            String name,
            List<StepDefinition> steps,
            OnErrorDefinition onError,
            Map<String, Object> metadata) {

        this.name = name;
        this.steps = List.copyOf(steps);
        this.onError = onError;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public String getName() {
        return name;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    /**
     * Returns the flow-level default OnErrorDefinition.
     * Step-level onError overrides this value.
     */
    public OnErrorDefinition getOnError() {
        return onError;
    }

    /**
     * Optional metadata for the flow.
     * Typical use cases:
     * - version
     * - description
     * - tags
     * - author
     * - createdAt
     *
     * Metadata has no effect on execution and is ignored by FlowEngine.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}