package io.github.ss.bareflow.core.trace;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single step execution record.
 * Immutable entry containing:
 * - step name
 * - context snapshot before execution
 * - evaluated input
 * - output (if success)
 * - error (if failure)
 * - start/end timestamps
 */
public class StepTraceEntry {
    private final String stepName;
    private final Map<String, Object> beforeContext;
    private final Map<String, Object> input;
    private final Map<String, Object> output;
    private final Throwable error;
    private final Instant startTime;
    private final Instant endTime;

    public StepTraceEntry(String stepName,
            Map<String, Object> beforeContext,
            Map<String, Object> input,
            Map<String, Object> output,
            Throwable error,
            Instant startTime,
            Instant endTime) {

        this.stepName = stepName;
        this.beforeContext = beforeContext;
        this.input = input;
        this.output = output;
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

    public Map<String, Object> getInput() {
        return input;
    }

    public Map<String, Object> getOutput() {
        return output;
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