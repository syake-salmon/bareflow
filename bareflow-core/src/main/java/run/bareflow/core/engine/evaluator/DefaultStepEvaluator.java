package run.bareflow.core.engine.evaluator;

import java.util.HashMap;
import java.util.Map;

import run.bareflow.core.context.ExecutionContext;

/**
 * Default implementation of StepEvaluator.
 *
 * BareFlow uses a simple placeholder model:
 * - Only flat placeholders of the form "${name}" are supported.
 * - Nested or hierarchical expressions such as "${a.b}" are not supported.
 *
 * Resolution rules:
 * - For input evaluation: placeholders are resolved from the ExecutionContext.
 * - For output evaluation: placeholders are resolved first from rawOutput,
 * then from the ExecutionContext.
 *
 * Literal values are returned as-is.
 * Unresolved placeholders evaluate to null.
 */
public class DefaultStepEvaluator implements StepEvaluator {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    @Override
    public Map<String, Object> evaluateInput(Map<String, Object> input, ExecutionContext ctx) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> e : input.entrySet()) {
            Object value = evaluateValue(e.getValue(), null, ctx);
            result.put(e.getKey(), value);
        }

        return result;
    }

    @Override
    public Map<String, Object> evaluateOutput(
            Map<String, Object> outputMapping,
            Map<String, Object> rawOutput,
            ExecutionContext ctx) {

        if (outputMapping == null || outputMapping.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> e : outputMapping.entrySet()) {
            Object value = evaluateValue(e.getValue(), rawOutput, ctx);
            result.put(e.getKey(), value);
        }

        return result;
    }

    /**
     * Evaluate a single value.
     * Supports:
     * - literal values
     * - simple placeholders "${name}"
     *
     * No nested expressions such as "${a.b}" are supported.
     * Unresolved placeholders evaluate to null.
     */
    private Object evaluateValue(Object expr, Map<String, Object> rawOutput, ExecutionContext ctx) {
        if (!(expr instanceof String)) {
            return expr; // literal
        }

        String s = (String) expr;

        if (!isPlaceholder(s)) {
            return s; // literal string
        }

        String key = extractKey(s);

        // 1. raw output
        if (rawOutput != null && rawOutput.containsKey(key)) {
            return rawOutput.get(key);
        }

        // 2. context
        if (ctx.contains(key)) {
            return ctx.get(key);
        }

        // 3. unresolved placeholder → null
        return null;
    }

    private boolean isPlaceholder(String s) {
        return s.startsWith(PREFIX) && s.endsWith(SUFFIX) && s.length() > 3;
    }

    private String extractKey(String s) {
        return s.substring(PREFIX.length(), s.length() - SUFFIX.length());
    }
}