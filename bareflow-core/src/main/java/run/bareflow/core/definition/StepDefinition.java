package run.bareflow.core.definition;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable model representing a single step in a flow.
 *
 * StepDefinition is a pure structural description used by FlowEngine:
 * - name: logical step identifier
 * - module / operation: target module and operation for StepInvoker
 * - input: raw input mapping (evaluated by StepEvaluator)
 * - output: raw output mapping (evaluated by StepEvaluator)
 * - retryPolicy: optional retry configuration for system-level errors
 * - onError: optional step-level error handling policy
 *
 * No validation or transformation is performed here.
 * Parsing, validation, and defaulting are responsibilities of the
 * FlowDefinitionResolver or higher-level runtime components.
 */
public class StepDefinition {
    private final String name;
    private final String module;
    private final String operation;

    private final Map<String, Object> input;
    private final Map<String, Object> output;

    private final RetryPolicy retryPolicy;
    private final OnErrorDefinition onError;

    public StepDefinition(
            String name,
            String module,
            String operation,
            Map<String, Object> input,
            Map<String, Object> output,
            RetryPolicy retryPolicy,
            OnErrorDefinition onError) {

        this.name = name;
        this.module = module;
        this.operation = operation;

        this.input = input == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(input);

        this.output = output == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(output);

        this.retryPolicy = retryPolicy;
        this.onError = onError;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public String getOperation() {
        return operation;
    }

    /**
     * Raw input mapping.
     * Evaluated by StepEvaluator before invocation.
     */
    public Map<String, Object> getInput() {
        return input;
    }

    /**
     * Raw output mapping.
     * Evaluated by StepEvaluator after invocation.
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * Optional retry policy for system-level errors.
     * If null, no retry is performed at the step level.
     */
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Optional step-level error handling policy.
     * Overrides the flow-level default if present.
     */
    public OnErrorDefinition getOnError() {
        return onError;
    }
}