package run.bareflow.core.definition;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable model representing error-handling behavior for a step or flow.
 *
 * OnErrorDefinition controls how FlowEngine reacts when a step throws a
 * BusinessException or an unhandled error after retry attempts.
 *
 * Semantics:
 * - STOP:
 * The flow stops immediately and the exception is propagated.
 *
 * - CONTINUE:
 * The error is ignored and execution proceeds to the next step.
 * If an output mapping is provided, it is evaluated and merged into
 * the ExecutionContext.
 *
 * - RETRY:
 * The step is retried exactly once (business-level retry).
 * This is distinct from RetryPolicy, which handles system-level retries.
 * delayMillis specifies the wait time before retrying.
 *
 * Output mapping:
 * - The output map is optional.
 * - When provided, it is evaluated by StepEvaluator and merged into the
 * ExecutionContext only when an error occurs.
 *
 * Flow-level onError acts as a default and is overridden by step-level onError.
 */
public class OnErrorDefinition {
    public enum Action {
        STOP,
        CONTINUE,
        RETRY
    }

    private final Action action;
    private final long delayMillis;
    private final Map<String, Object> output;

    public OnErrorDefinition(
            Action action,
            long delayMillis,
            Map<String, Object> output) {

        this.action = action;
        this.delayMillis = delayMillis;
        this.output = output == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(output);
    }

    public Action getAction() {
        return action;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public Map<String, Object> getOutput() {
        return output;
    }
}