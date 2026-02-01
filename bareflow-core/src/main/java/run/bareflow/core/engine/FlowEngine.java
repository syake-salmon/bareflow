package run.bareflow.core.engine;

import java.time.Instant;
import java.util.Map;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.definition.OnErrorDefinition;
import run.bareflow.core.definition.RetryPolicy;
import run.bareflow.core.definition.StepDefinition;
import run.bareflow.core.engine.evaluator.StepEvaluator;
import run.bareflow.core.engine.event.FlowEngineEvent.*;
import run.bareflow.core.engine.event.FlowEngineEventListener;
import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.BusinessException;
import run.bareflow.core.exception.StepExecutionException;
import run.bareflow.core.exception.SystemException;
import run.bareflow.core.trace.StepTrace;
import run.bareflow.core.trace.StepTraceEntry;

/**
 * Formal implementation of BareFlow's execution engine.
 * Executes steps sequentially with retry and onError handling.
 *
 * <p>
 * This engine relies on StepEvaluator for resolving input/output mappings.
 * BareFlow's evaluation model is intentionally simple:
 * </p>
 *
 * <ul>
 * <li>Only flat placeholders of the form "${name}" are supported.</li>
 * <li>No nested expressions such as "${a.b}".</li>
 * <li>Input placeholders are resolved from the ExecutionContext.</li>
 * <li>Output placeholders are resolved first from the raw output,
 * then from the ExecutionContext.</li>
 * </ul>
 *
 * <p>
 * Retry semantics:
 * </p>
 * <ul>
 * <li>attempts start at 1</li>
 * <li>RetryPolicy.maxAttempts = total number of attempts</li>
 * <li>RetryPolicy applies only to SystemException / StepExecutionException</li>
 * <li>BusinessException is never retried by RetryPolicy</li>
 * <li>onError.RETRY performs exactly one retry, independent of RetryPolicy</li>
 * </ul>
 */
public class FlowEngine {
    private final StepEvaluator evaluator;
    private final StepInvoker invoker;
    private final FlowEngineEventListener listener;
    private boolean onErrorRetryUsed = false;

    public FlowEngine(final StepEvaluator evaluator,
            final StepInvoker invoker,
            final FlowEngineEventListener listener) {
        this.evaluator = evaluator;
        this.invoker = invoker;
        this.listener = listener;
    }

    /**
     * Execute a flow using the given context.
     * Returns a StepTrace representing the full execution history.
     */
    public StepTrace execute(final FlowDefinition flow, final ExecutionContext ctx) {
        Instant flowStartTime = Instant.now();
        listener.onEvent(new FlowStartEvent(flow, flowStartTime));

        final StepTrace trace = new StepTrace();

        for (final StepDefinition step : flow.getSteps()) {
            this.executeStepWithControl(flow, step, ctx, trace);
        }

        listener.onEvent(new FlowEndEvent(flow, trace, flowStartTime, Instant.now()));
        return trace;
    }

    /**
     * Execute a single step with retry and onError handling.
     *
     * <p>
     * RetryPolicy:
     * </p>
     * <ul>
     * <li>attempts start at 1</li>
     * <li>retry is allowed while attempts &lt; maxAttempts</li>
     * <li>maxAttempts = total number of attempts</li>
     * </ul>
     */
    private void executeStepWithControl(
            final FlowDefinition flow,
            final StepDefinition step,
            final ExecutionContext ctx,
            final StepTrace trace) {

        final RetryPolicy retryPolicy = step.getRetryPolicy();
        int attempts = 0;

        while (true) {
            attempts++;
            Instant stepStartTime = Instant.now();
            listener.onEvent(new StepStartEvent(step, attempts, stepStartTime));

            final Instant start = Instant.now();
            final Map<String, Object> before = ctx.snapshot();

            try {
                // 1. Evaluate input
                Instant inputEvalStartTime = Instant.now();
                listener.onEvent(new InputEvaluationStartEvent(step, attempts, inputEvalStartTime));

                final Map<String, Object> evaluatedInput = this.evaluator.evaluateInput(step.getInput(), ctx);

                listener.onEvent(
                        new InputEvaluationEndEvent(step, attempts, evaluatedInput, inputEvalStartTime, Instant.now()));

                // 2. Invoke module operation
                Instant invokeStartTime = Instant.now();
                listener.onEvent(new InvokeStartEvent(step, attempts, evaluatedInput, invokeStartTime));

                final Map<String, Object> rawOutput = this.invoker.invoke(step.getModule(), step.getOperation(),
                        evaluatedInput);

                listener.onEvent(new InvokeEndEvent(step, attempts, rawOutput, invokeStartTime, Instant.now()));

                // 3. Apply output mapping
                if (!step.getOutput().isEmpty()) {
                    Instant outputEvalStartTime = Instant.now();
                    listener.onEvent(new OutputEvaluationStartEvent(step, attempts, rawOutput, outputEvalStartTime));

                    final Map<String, Object> mappedOutput = this.evaluator.evaluateOutput(step.getOutput(), rawOutput,
                            ctx);
                    ctx.merge(mappedOutput);

                    listener.onEvent(new OutputEvaluationEndEvent(step, attempts, mappedOutput, outputEvalStartTime,
                            Instant.now()));
                }

                // 4. Record success
                final StepTraceEntry entry = new StepTraceEntry(
                        step.getName(),
                        before,
                        evaluatedInput,
                        rawOutput,
                        null,
                        start,
                        Instant.now(),
                        attempts);
                trace.record(entry);

                listener.onEvent(new StepEndEvent(entry, stepStartTime, Instant.now()));
                return; // success

            } catch (final BusinessException e) {
                listener.onEvent(new BusinessErrorEvent(step, attempts, e));
                this.recordError(trace, step, before, e, start, attempts);

                // Business errors are not retried by RetryPolicy
                final boolean retry = this.handleOnError(flow, step, attempts, e);
                if (retry) {
                    continue; // onError.RETRY → exactly one retry
                }
                return;

            } catch (final SystemException e) {
                listener.onEvent(new SystemErrorEvent(step, attempts, e));
                this.recordError(trace, step, before, e, start, attempts);

                // System errors may be retried by RetryPolicy
                if (retryPolicy != null && attempts < retryPolicy.getMaxAttempts()) {
                    long delayMillis = retryPolicy.getDelayMillis();

                    listener.onEvent(new RetryPolicyRetryEvent(step, attempts, delayMillis));

                    this.sleep(delayMillis);
                    continue;
                }

                final boolean retry = this.handleOnError(flow, step, attempts, e);
                if (retry) {
                    continue;
                }
                return;

            } catch (final StepExecutionException e) {
                listener.onEvent(new StepExecutionErrorEvent(step, attempts, e));
                this.recordError(trace, step, before, e, start, attempts);

                // Treated the same as SystemException
                if (retryPolicy != null && attempts < retryPolicy.getMaxAttempts()) {
                    long delayMillis = retryPolicy.getDelayMillis();

                    listener.onEvent(new RetryPolicyRetryEvent(step, attempts, delayMillis));

                    this.sleep(delayMillis);
                    continue;
                }

                final boolean retry = this.handleOnError(flow, step, attempts, e);
                if (retry) {
                    continue;
                }
                return;
            }
        }
    }

    /**
     * Handle onError behavior (STOP / CONTINUE / RETRY).
     *
     * <p>
     * RETRY performs exactly one retry, independent of RetryPolicy.
     * </p>
     *
     * @return true if the step should be retried once, false otherwise
     */
    private boolean handleOnError(
            final FlowDefinition flow,
            final StepDefinition step,
            final int attempts,
            final Throwable error) {

        final OnErrorDefinition onError = step.getOnError() != null ? step.getOnError() : flow.getOnError();

        if (onError == null) {
            throw new StepExecutionException("Unhandled error: " + error.getMessage(), error);
        }

        switch (onError.getAction()) {
            case STOP:
                throw new StepExecutionException("Flow stopped due to error", error);

            case CONTINUE:
                return false;

            case RETRY:
                if (!onErrorRetryUsed) {
                    final long delayMillis = onError.getDelayMillis();

                    listener.onEvent(new OnErrorRetryEvent(step, attempts, delayMillis));

                    sleep(delayMillis);
                    onErrorRetryUsed = true;
                    return true;
                }
                return false;

            default:
                throw new StepExecutionException("Unknown onError action", error);
        }
    }

    /**
     * Record an error attempt into the trace.
     */
    private void recordError(
            final StepTrace trace,
            final StepDefinition step,
            final Map<String, Object> before,
            final Throwable error,
            final Instant start,
            final int attempts) {

        trace.record(new StepTraceEntry(
                step.getName(),
                before,
                null,
                null,
                error,
                start,
                Instant.now(),
                attempts));
    }

    /**
     * Sleep helper for retry delays.
     */
    private void sleep(final long millis) {
        if (millis <= 0L) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ignored) {
            // NOP
        }
    }
}