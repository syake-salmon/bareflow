package run.bareflow.runtime.parser;

import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.definition.OnErrorDefinition;
import run.bareflow.core.definition.RetryPolicy;
import run.bareflow.core.definition.StepDefinition;
import run.bareflow.core.exception.SystemException;

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
                throw new SystemException("Invalid YAML: root must be a map");
            }

            Map<String, Object> root = (Map<String, Object>) loaded;

            String name = (String) root.get("name");
            if (name == null) {
                throw new SystemException("Invalid YAML: Flow 'name' is required");
            }

            // --- metadata ---
            Map<String, Object> metadata = root.containsKey("metadata")
                    ? (Map<String, Object>) root.get("metadata")
                    : Map.of();

            // --- steps ---
            List<Map<String, Object>> stepMaps = (List<Map<String, Object>>) root.get("steps");
            if (stepMaps == null || stepMaps.isEmpty()) {
                throw new SystemException("Invalid YAML: Flow must contain at least one step");
            }

            List<StepDefinition> steps = stepMaps.stream()
                    .map(this::parseStep)
                    .toList();

            // --- flow-level onError ---
            OnErrorDefinition onError = null;
            if (root.containsKey("onError")) {
                onError = parseOnError((Map<String, Object>) root.get("onError"));
            }

            return new FlowDefinition(name, steps, onError, metadata);

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

        if (name == null)
            throw new SystemException("Invalid YAML: Step 'name' is required");
        if (module == null)
            throw new SystemException("Invalid YAML: Step 'module' is required");
        if (operation == null)
            throw new SystemException("Invalid YAML: Step 'operation' is required");

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

        String actionStr = (String) raw.get("action");
        if (actionStr == null) {
            throw new SystemException("Invalid YAML: onError 'action' is required");
        }

        OnErrorDefinition.Action action;
        try {
            action = OnErrorDefinition.Action.valueOf(actionStr);
        } catch (IllegalArgumentException e) {
            throw new SystemException("Invalid YAML: onError 'action' must be one of STOP, CONTINUE, RETRY");
        }

        Integer delayMillis = (Integer) raw.getOrDefault("delayMillis", 0);
        Map<String, Object> output = (Map<String, Object>) raw.getOrDefault("output", Map.of());

        return new OnErrorDefinition(action, delayMillis, output);
    }
}