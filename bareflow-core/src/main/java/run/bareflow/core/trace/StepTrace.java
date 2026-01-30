package run.bareflow.core.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the full execution trace of a flow.
 * Stores an ordered list of StepTraceEntry objects.
 *
 * StepTrace is mutable only through the record() method.
 * Consumers receive an immutable view of the entries.
 */
public class StepTrace {
    private final List<StepTraceEntry> entries = new ArrayList<>();

    /**
     * Record a new step execution entry.
     */
    public void record(StepTraceEntry entry) {
        entries.add(entry);
    }

    /**
     * Returns an immutable list of all recorded entries.
     */
    public List<StepTraceEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns true if all steps succeeded.
     */
    public boolean isSuccess() {
        return entries.stream().allMatch(StepTraceEntry::isSuccess);
    }
}