package run.bareflow.core.trace;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single step execution record.
 * Captures the state before execution, the evaluated input,
 * the raw output (if any), and the error (if any).
 *
 * All fields are immutable.
 */
public class StepTraceEntry {
    private final String stepName;
    private final Map<String, Object> beforeContext;
    private final Map<String, Object> evaluatedInput;
    private final Map<String, Object> rawOutput;
    private final Throwable error;
    private final Instant startTime;
    private final Instant endTime;

    public StepTraceEntry(
            String stepName,
            Map<String, Object> beforeContext,
            Map<String, Object> evaluatedInput,
            Map<String, Object> rawOutput,
            Throwable error,
            Instant startTime,
            Instant endTime) {

        this.stepName = stepName;
        this.beforeContext = beforeContext;
        this.evaluatedInput = evaluatedInput;
        this.rawOutput = rawOutput;
        this.error = error;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStepName() {
        return stepName;
    }

    public Map<String, Object> getBeforeContext() {
        return beforeContext;
    }

    public Map<String, Object> getEvaluatedInput() {
        return evaluatedInput;
    }

    public Map<String, Object> getRawOutput() {
        return rawOutput;
    }

    public Throwable getError() {
        return error;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public boolean isSuccess() {
        return error == null;
    }
}