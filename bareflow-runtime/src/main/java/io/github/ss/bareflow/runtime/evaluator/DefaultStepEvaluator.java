package io.github.ss.bareflow.runtime.evaluator;

import io.github.ss.bareflow.core.context.ExecutionContext;
import io.github.ss.bareflow.core.engine.StepEvaluator;
import io.github.ss.bareflow.core.exception.SystemException;

import java.util.HashMap;
import java.util.Map;

/**
 * Default evaluator for BareFlow.
 *
 * Resolves simple variable references of the form:
 *
 * ${key}
 *
 * Rules:
 * - Only top-level keys are allowed.
 * - Nested or hierarchical expressions (e.g., ${a.b}) are NOT supported.
 * - Empty references (${}) are invalid.
 * - Keys must match [A-Za-z0-9_]+.
 *
 * Any invalid reference results in a SystemException.
 *
 * This evaluator intentionally keeps the expression model minimal,
 * reflecting BareFlow's philosophy of simplicity and transparency.
 */
public class DefaultStepEvaluator implements StepEvaluator {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    @Override
    public Map<String, Object> evaluateInput(Map<String, Object> input, ExecutionContext ctx) {
        return resolve(input, ctx);
    }

    @Override
    public Map<String, Object> evaluateOutput(Map<String, Object> output, ExecutionContext ctx) {
        return resolve(output, ctx);
    }

    private Map<String, Object> resolve(Map<String, Object> map, ExecutionContext ctx) {
        Map<String, Object> resolved = new HashMap<>();

        for (var entry : map.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String s && isReference(s)) {
                String key = extractKey(s);
                validateKey(key, s);

                Object resolvedValue = ctx.get(key);
                resolved.put(entry.getKey(), resolvedValue);

            } else {
                resolved.put(entry.getKey(), value);
            }
        }

        return resolved;
    }

    /**
     * Check if a string is a ${...} reference.
     */
    private boolean isReference(String s) {
        return s.startsWith(PREFIX) && s.endsWith(SUFFIX);
    }

    /**
     * Extract the key inside ${key}.
     */
    private String extractKey(String s) {
        return s.substring(2, s.length() - 1);
    }

    /**
     * Validate that the key is a valid top-level reference.
     */
    private void validateKey(String key, String raw) {
        if (key.isEmpty()) {
            throw new SystemException("Empty reference is not allowed: " + raw);
        }

        // Only allow simple top-level keys
        if (!key.matches("[A-Za-z0-9_]+")) {
            throw new SystemException("Invalid reference (only top-level keys allowed): " + raw);
        }
    }
}