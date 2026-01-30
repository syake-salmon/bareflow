package run.bareflow.runtime.executor;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.trace.StepTrace;

/**
 * Represents the result of a flow execution.
 *
 * Contains:
 * - The final ExecutionContext after all steps have completed.
 * - The full StepTrace capturing the execution history.
 *
 * FlowResult is a simple immutable DTO returned by FlowExecutor.
 * It performs no additional processing or transformation.
 */
public class FlowResult {
    private final ExecutionContext context;
    private final StepTrace trace;

    public FlowResult(ExecutionContext context, StepTrace trace) {
        this.context = context;
        this.trace = trace;
    }

    /**
     * Returns the final execution context.
     * The context is mutable, but FlowResult does not modify it.
     */
    public ExecutionContext getContext() {
        return context;
    }

    /**
     * Returns the full execution trace.
     */
    public StepTrace getTrace() {
        return trace;
    }
}