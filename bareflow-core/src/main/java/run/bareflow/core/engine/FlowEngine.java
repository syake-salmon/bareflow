package run.bareflow.core.engine;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.definition.OnErrorDefinition;
import run.bareflow.core.definition.RetryPolicy;
import run.bareflow.core.definition.StepDefinition;
import run.bareflow.core.engine.evaluator.StepEvaluator;
import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.BusinessException;
import run.bareflow.core.exception.StepExecutionException;
import run.bareflow.core.exception.SystemException;
import run.bareflow.core.trace.StepTrace;
import run.bareflow.core.trace.StepTraceEntry;

import java.time.Instant;
import java.util.Map;

/**
 * Formal implementation of BareFlow's execution engine.
 * Executes steps sequentially with retry and onError handling.
 *
 * This engine relies on StepEvaluator for resolving input/output mappings.
 * BareFlow's evaluation model is intentionally simple:
 *
 * - Only flat placeholders of the form "${name}" are supported.
 * - Nested or hierarchical expressions such as "${a.b}" are not supported.
 * - For input evaluation: placeholders are resolved from the ExecutionContext.
 * - For output evaluation: placeholders are resolved first from the raw output
 * returned by the StepInvoker, then from the ExecutionContext.
 *
 * These rules are defined by StepEvaluator and enforced by the default
 * implementation provided in the core module.
 *
 * Core dependencies:
 * - StepEvaluator
 * - StepInvoker
 * - Definition models (FlowDefinition, StepDefinition, RetryPolicy,
 * OnErrorDefinition)
 * - ExecutionContext
 * - StepTrace
 */

public class FlowEngine {
    private final StepEvaluator evaluator;
    private final StepInvoker invoker;

    public FlowEngine(StepEvaluator evaluator, StepInvoker invoker) {
        this.evaluator = evaluator;
        this.invoker = invoker;
    }

    /**
     * Execute a flow using the given context.
     * Returns a StepTrace representing the full execution history.
     * 
     * @param flow flow definition
     * @param ctx  execution context
     */
    public StepTrace execute(FlowDefinition flow, ExecutionContext ctx) {
        StepTrace trace = new StepTrace();

        for (StepDefinition step : flow.getSteps()) {
            executeStepWithControl(flow, step, ctx, trace);
        }

        return trace;
    }

    /**
     * Execute a single step with retry and onError handling.
     *
     * RetryPolicy:
     * - attempts start at 1
     * - retry is allowed while attempts <= maxAttempts
     * - this means "maxAttempts = maximum number of total attempts"
     *
     * Output mapping:
     * - if the output mapping is empty, nothing is written to the context
     * - if the mapping is present, only mapped keys are evaluated and merged
     * - evaluator handles simple "${name}" placeholders (no nested paths)
     */
    private void executeStepWithControl(FlowDefinition flow,
            StepDefinition step,
            ExecutionContext ctx,
            StepTrace trace) {

        RetryPolicy retryPolicy = step.getRetryPolicy();
        int attempts = 0;

        while (true) {
            attempts++;

            Instant start = Instant.now();
            Map<String, Object> before = ctx.snapshot();

            try {
                // 1. Evaluate input
                Map<String, Object> evaluatedInput = evaluator.evaluateInput(step.getInput(), ctx);

                // 2. Invoke module operation
                Map<String, Object> output = invoker.invoke(step.getModule(), step.getOperation(), evaluatedInput);

                // 3. Apply output mapping
                if (!step.getOutput().isEmpty()) {
                    Map<String, Object> mapped = evaluator.evaluateOutput(step.getOutput(), output, ctx);
                    ctx.merge(mapped);
                }

                // 4. Record success trace
                trace.record(new StepTraceEntry(
                        step.getName(),
                        before,
                        evaluatedInput,
                        output,
                        null,
                        start,
                        Instant.now()));

                return; // success

            } catch (BusinessException be) {
                // Business errors are not retried
                recordError(trace, step, before, be, start);
                handleOnError(flow, step, be);
                return;

            } catch (SystemException se) {
                // System errors may be retried
                if (retryPolicy != null && attempts <= retryPolicy.getMaxAttempts()) {
                    sleep(retryPolicy.getBackoffMillis());
                    continue;
                }

                recordError(trace, step, before, se, start);
                handleOnError(flow, step, se);
                return;

            } catch (StepExecutionException se) {
                // General execution errors → treat like SystemException
                if (retryPolicy != null && attempts <= retryPolicy.getMaxAttempts()) {
                    sleep(retryPolicy.getBackoffMillis());
                    continue;
                }

                recordError(trace, step, before, se, start);
                handleOnError(flow, step, se);
                return;
            }
        }
    }

    /**
     * Handle onError behavior (STOP / CONTINUE / RETRY).
     *
     * RETRY:
     * - onError.RETRY performs exactly one retry
     * - this is independent from RetryPolicy
     */
    private void handleOnError(FlowDefinition flow,
            StepDefinition step,
            Throwable error) {

        OnErrorDefinition onError = step.getOnError() != null
                ? step.getOnError()
                : flow.getOnError();

        if (onError == null) {
            throw new StepExecutionException("Unhandled error: " + error.getMessage(), error);
        }

        switch (onError.getAction()) {
            case STOP:
                throw new StepExecutionException("Flow stopped due to error", error);

            case CONTINUE:
                return;

            case RETRY:
                sleep(onError.getDelayMillis());
                return;

            default:
                throw new StepExecutionException("Unknown onError action", error);
        }
    }

    /**
     * Record error trace entry.
     */
    private void recordError(StepTrace trace,
            StepDefinition step,
            Map<String, Object> before,
            Throwable error,
            Instant start) {

        trace.record(new StepTraceEntry(
                step.getName(),
                before,
                null,
                null,
                error,
                start,
                Instant.now()));
    }

    /**
     * Sleep helper for retry delays.
     */
    private void sleep(long millis) {
        if (millis <= 0)
            return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // NOP
        }
    }
}