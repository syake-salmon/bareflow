package io.github.ss.bareflow.runtime.parser;

import io.github.ss.bareflow.core.definition.FlowDefinition;
import io.github.ss.bareflow.core.definition.StepDefinition;
import io.github.ss.bareflow.core.definition.RetryPolicy;
import io.github.ss.bareflow.core.definition.OnErrorDefinition;
import io.github.ss.bareflow.core.exception.SystemException;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Parses a YAML file into a FlowDefinition.
 * This parser is intentionally minimal and transparent,
 * converting YAML structures directly into BareFlow definition objects.
 */
public class FlowYamlParser {
    private final Load yamlLoader;

    public FlowYamlParser() {
        LoadSettings settings = LoadSettings.builder().build();
        this.yamlLoader = new Load(settings);
    }

    /**
     * Parse a YAML InputStream into a FlowDefinition.
     *
     * @param inputStream YAML file input
     * @return FlowDefinition
     */
    @SuppressWarnings("unchecked")
    public FlowDefinition parse(InputStream inputStream) {
        try {
            Object loaded = yamlLoader.loadFromInputStream(inputStream);

            if (!(loaded instanceof Map)) {
                throw new SystemException("YAML root must be a map");
            }

            Map<String, Object> root = (Map<String, Object>) loaded;

            String name = (String) root.get("name");
            List<Map<String, Object>> stepsRaw = (List<Map<String, Object>>) root.get("steps");
            Map<String, Object> onErrorRaw = (Map<String, Object>) root.get("onError");

            if (name == null || stepsRaw == null) {
                throw new SystemException("Flow YAML must contain 'name' and 'steps'");
            }

            List<StepDefinition> steps = stepsRaw.stream()
                    .map(this::parseStep)
                    .toList();

            OnErrorDefinition flowOnError = parseOnError(onErrorRaw);

            return new FlowDefinition(name, steps, flowOnError);

        } catch (Exception e) {
            throw new SystemException("Failed to parse YAML flow definition", e);
        }
    }

    /**
     * Parse a single step block into a StepDefinition.
     */
    @SuppressWarnings("unchecked")
    private StepDefinition parseStep(Map<String, Object> raw) {

        String name = (String) raw.get("name");
        String module = (String) raw.get("module");
        String operation = (String) raw.get("operation");

        Map<String, Object> input = (Map<String, Object>) raw.getOrDefault("input", Map.of());
        Map<String, Object> output = (Map<String, Object>) raw.getOrDefault("output", Map.of());

        RetryPolicy retryPolicy = parseRetryPolicy((Map<String, Object>) raw.get("retry"));
        OnErrorDefinition onError = parseOnError((Map<String, Object>) raw.get("onError"));

        return new StepDefinition(
                name,
                module,
                operation,
                input,
                output,
                retryPolicy,
                onError);
    }

    /**
     * Parse retry policy block.
     */
    private RetryPolicy parseRetryPolicy(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }

        Integer maxAttempts = (Integer) raw.getOrDefault("maxAttempts", 1);
        Integer delayMillis = (Integer) raw.getOrDefault("delayMillis", 0);

        return new RetryPolicy(maxAttempts, delayMillis);
    }

    /**
     * Parse onError block.
     */
    @SuppressWarnings("unchecked")
    private OnErrorDefinition parseOnError(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }

        String action = (String) raw.get("action");
        Integer delayMillis = (Integer) raw.getOrDefault("delayMillis", 0);
        Map<String, Object> output = (Map<String, Object>) raw.getOrDefault("output", Map.of());

        return new OnErrorDefinition(action, delayMillis, output);
    }
}