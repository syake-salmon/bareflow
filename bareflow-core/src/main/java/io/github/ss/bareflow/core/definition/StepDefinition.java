package io.github.ss.bareflow.core.definition;

import java.util.Map;
import java.util.Objects;

/**
 * Pure specification model representing a single step in a flow.
 * This class intentionally contains no execution logic.
 */
public class StepDefinition {
    /** Unique identifier of the step */
    private String id;

    /** Module name */
    private String module;

    /** Operation name within the module */
    private String operation;

    /** Input parameters */
    private Map<String, Object> input;

    /** Output mapping */
    private Map<String, String> output;

    /** Optional retry policy for this step */
    private RetryPolicy retryPolicy;

    public StepDefinition() {
        // NOP
    }

    public StepDefinition(String id,
            String module,
            String operation,
            Map<String, Object> input,
            Map<String, String> output,
            RetryPolicy retryPolicy) {
        this.id = id;
        this.module = module;
        this.operation = operation;
        this.input = input;
        this.output = output;
        this.retryPolicy = retryPolicy;
    }

    // ===== Getters / Setters =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, String> getOutput() {
        return output;
    }

    public void setOutput(Map<String, String> output) {
        this.output = output;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    // ===== Utility =====
    public boolean hasInput() {
        return input != null && !input.isEmpty();
    }

    public boolean hasOutput() {
        return output != null && !output.isEmpty();
    }

    public boolean hasRetryPolicy() {
        return retryPolicy != null;
    }

    @Override
    public String toString() {
        return "StepDefinition{" +
                "id='" + id + '\'' +
                ", module='" + module + '\'' +
                ", operation='" + operation + '\'' +
                ", input=" + input +
                ", output=" + output +
                ", retryPolicy=" + retryPolicy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StepDefinition))
            return false;
        StepDefinition that = (StepDefinition) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(module, that.module) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(input, that.input) &&
                Objects.equals(output, that.output) &&
                Objects.equals(retryPolicy, that.retryPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, module, operation, input, output, retryPolicy);
    }
}
