package io.github.ss.bareflow.core.definition;

import java.util.Collections;
import java.util.Map;

/**
 * Error handling definition for a step or flow.
 * Immutable model representing onError behavior.
 */
public class OnErrorDefinition {
    public enum Action {
        STOP,
        CONTINUE,
        RETRY
    }

    private final Action action;
    private final long delayMillis;
    private final Map<String, Object> output;

    public OnErrorDefinition(String action,
            long delayMillis,
            Map<String, Object> output) {

        this.action = Action.valueOf(action);
        this.delayMillis = delayMillis;
        this.output = output == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(output);
    }

    public Action getAction() {
        return action;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public Map<String, Object> getOutput() {
        return output;
    }
}