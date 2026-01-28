package io.github.ss.bareflow.core.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ordered collection of step execution records.
 * Mutable only internally; exposed as an unmodifiable list.
 */
public class StepTrace {
    private final List<StepTraceEntry> entries = new ArrayList<>();

    /**
     * Record a new trace entry.
     */
    public void record(StepTraceEntry entry) {
        entries.add(entry);
    }

    /**
     * Get all trace entries in execution order.
     */
    public List<StepTraceEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}