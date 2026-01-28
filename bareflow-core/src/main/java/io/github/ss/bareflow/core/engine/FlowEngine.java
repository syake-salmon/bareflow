package io.github.ss.bareflow.core.engine;

import io.github.ss.bareflow.core.context.ExecutionContext;
import io.github.ss.bareflow.core.definition.FlowDefinition;
import io.github.ss.bareflow.core.definition.OnErrorDefinition;
import io.github.ss.bareflow.core.definition.RetryPolicy;
import io.github.ss.bareflow.core.definition.StepDefinition;
import io.github.ss.bareflow.core.exception.BusinessException;
import io.github.ss.bareflow.core.exception.StepExecutionException;
import io.github.ss.bareflow.core.exception.SystemException;
import io.github.ss.bareflow.core.trace.StepTrace;
import io.github.ss.bareflow.core.trace.StepTraceEntry;

import java.time.Instant;
import java.util.Map;

/**
 * Formal implementation of BareFlow's execution engine.
 * Executes steps sequentially with retry and onError handling.
 *
 * This engine is pure and depends only on core abstractions:
 * - StepEvaluator
 * - StepInvoker
 * - Definition models
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
     * Execute a flow using the given context and trace.
     *
     * @param flow  flow definition
     * @param ctx   execution context
     * @param trace step trace collector
     */
    public void execute(FlowDefinition flow, ExecutionContext ctx, StepTrace trace) {
        for (StepDefinition step : flow.getSteps()) {
            executeStepWithControl(flow, step, ctx, trace);
        }
    }

    /**
     * Execute a single step with retry and onError handling.
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

                // 3. Merge output into context
                if (!step.getOutput().isEmpty()) {
                    ctx.merge(output);
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