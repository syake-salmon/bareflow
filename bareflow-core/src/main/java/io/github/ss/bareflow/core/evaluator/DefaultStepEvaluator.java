package io.github.ss.bareflow.core.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.ss.bareflow.core.context.ExecutionContext;
import io.github.ss.bareflow.core.engine.StepEvaluator;
import io.github.ss.bareflow.core.exception.StepExecutionException;

public class DefaultStepEvaluator implements StepEvaluator {
    @Override
    public Map<String, Object> evaluate(Map<String, Object> rawInput, ExecutionContext ctx) {
        if (rawInput == null) {
            return Collections.emptyMap();
        }
        return evaluateMap(rawInput, ctx);
    }

    private Object evaluateValue(Object value, ExecutionContext ctx) {
        if (value instanceof String) {
            return resolveString((String) value, ctx);
        }
        if (value instanceof Map) {
            return evaluateMap((Map<?, ?>) value, ctx);
        }
        if (value instanceof List) {
            return evaluateList((List<?>) value, ctx);
        }
        return value; // primitive or unknown type
    }

    private Map<String, Object> evaluateMap(Map<?, ?> map, ExecutionContext ctx) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(entry.getKey().toString(), evaluateValue(entry.getValue(), ctx));
        }
        return result;
    }

    private List<Object> evaluateList(List<?> list, ExecutionContext ctx) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            result.add(evaluateValue(item, ctx));
        }
        return result;
    }

    private Object resolveString(String value, ExecutionContext ctx) {
        if (!value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }

        String key = value.substring(2, value.length() - 1);

        if (key.isEmpty()) {
            throw new StepExecutionException("Empty context reference: ${}");
        }

        if (!ctx.contains(key)) {
            throw new StepExecutionException("Context key not found: " + key);
        }
        return ctx.get(key);
    }
}
