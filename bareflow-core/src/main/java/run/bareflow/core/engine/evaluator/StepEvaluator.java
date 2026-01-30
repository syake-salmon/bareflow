package run.bareflow.core.engine.evaluator;

import java.util.Map;

import run.bareflow.core.context.ExecutionContext;

/**
 * Evaluates input and output mappings for a step.
 *
 * BareFlow uses a simple placeholder model:
 * - Only flat placeholders of the form "${name}" are supported.
 * - Nested or hierarchical expressions such as "${a.b}" are not supported.
 *
 * Resolution rules:
 * - For input evaluation: values are resolved from the ExecutionContext.
 * - For output evaluation: values are resolved first from the raw output
 * returned by the StepInvoker, then from the ExecutionContext.
 *
 * Implementations must be pure and deterministic.
 * The core provides a default implementation that follows BareFlow's rules.
 * Custom evaluators may be provided, but doing so changes the semantics of
 * BareFlow definitions and is generally discouraged.
 */
public interface StepEvaluator {
    /**
     * Evaluate the input mapping before invoking a step.
     * Literal values are returned as-is.
     * Placeholders "${name}" are resolved from the ExecutionContext.
     *
     * @param input raw input mapping from StepDefinition
     * @param ctx   execution context
     * @return evaluated input map
     */
    Map<String, Object> evaluateInput(Map<String, Object> input, ExecutionContext ctx);

    /**
     * Evaluate the output mapping of a step.
     * Only keys defined in the output mapping are included in the result.
     * Literal values are returned as-is.
     * Placeholders "${name}" are resolved first from rawOutput, then from ctx.
     *
     * @param outputMapping mapping defined in StepDefinition
     * @param rawOutput     raw output returned by the StepInvoker
     * @param ctx           execution context
     * @return evaluated and mapped output values
     */
    Map<String, Object> evaluateOutput(
            Map<String, Object> outputMapping,
            Map<String, Object> rawOutput,
            ExecutionContext ctx);
}