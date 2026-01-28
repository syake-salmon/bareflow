package io.github.ss.bareflow.core.engine;

import java.util.Map;

import io.github.ss.bareflow.core.context.ExecutionContext;

/**
 * Evaluates input and output maps for a step.
 *
 * Implementations may:
 * - Resolve template expressions (e.g., ${key})
 * - Read values from ExecutionContext
 * - Perform type conversions
 * - Apply custom resolution logic
 *
 * The core provides only this abstraction.
 * Actual resolution strategies belong to runtime or application layers.
 */
public interface StepEvaluator {
    /**
     * Evaluate the input map before invoking a step.
     *
     * @param input raw input map from StepDefinition
     * @param ctx   execution context
     * @return evaluated input map
     */
    Map<String, Object> evaluateInput(Map<String, Object> input, ExecutionContext ctx);

    /**
     * Evaluate the output map after a step is executed.
     *
     * @param output raw output map from StepDefinition
     * @param ctx    execution context
     * @return evaluated output map
     */
    Map<String, Object> evaluateOutput(Map<String, Object> output, ExecutionContext ctx);
}
