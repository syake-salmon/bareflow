package run.bareflow.core.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the mutable execution state of a flow.
 *
 * ExecutionContext is a flat key-value store used to pass data between steps.
 * It provides simple read/write operations and supports snapshotting for trace
 * recording. No hierarchical or nested resolution is performed.
 *
 * Characteristics:
 * - Keys are simple strings.
 * - Values are arbitrary objects.
 * - Merging overwrites existing keys.
 * - Snapshots are immutable copies.
 *
 * This class is intentionally minimal and deterministic.
 * Higher-level semantics belong to FlowEngine and StepEvaluator.
 */
public class ExecutionContext {
    private final Map<String, Object> data = new HashMap<>();

    /**
     * Retrieve a value by key.
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * Store or overwrite a value.
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Check if a key exists.
     */
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    /**
     * Merge another map into this context.
     * Existing keys are overwritten.
     */
    public void merge(Map<String, Object> values) {
        if (values != null) {
            data.putAll(values);
        }
    }

    /**
     * Return an immutable snapshot of the current state.
     * Used by StepTrace to capture pre-step context.
     */
    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(data));
    }

    /**
     * Return an immutable view of the current context.
     * Useful for debugging or external inspection.
     */
    public Map<String, Object> view() {
        return Collections.unmodifiableMap(data);
    }
}