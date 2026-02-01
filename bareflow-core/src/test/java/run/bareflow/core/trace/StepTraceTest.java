package run.bareflow.core.trace;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class StepTraceTest {
    private StepTraceEntry successEntry(int attempt) {
        return new StepTraceEntry(
                "step",
                Map.of("a", 1),
                Map.of("x", 10),
                Map.of("y", 20),
                null,
                Instant.now(),
                Instant.now(),
                attempt);
    }

    private StepTraceEntry failureEntry(int attempt) {
        return new StepTraceEntry(
                "step",
                Map.of("a", 1),
                Map.of("x", 10),
                null,
                new RuntimeException("fail"),
                Instant.now(),
                Instant.now(),
                attempt);
    }

    // ------------------------------------------------------------
    // 1. record() でエントリが追加される
    // ------------------------------------------------------------
    @Test
    public void testRecordAddsEntry() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));

        assertEquals(1, trace.getEntries().size());
    }

    // ------------------------------------------------------------
    // 2. getEntries() が不変リストを返す
    // ------------------------------------------------------------
    @Test
    public void testGetEntriesIsImmutable() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));

        List<StepTraceEntry> entries = trace.getEntries();
        assertThrows(UnsupportedOperationException.class, () -> entries.add(successEntry(2)));
    }

    // ------------------------------------------------------------
    // 3. isAllSuccessful()
    // ------------------------------------------------------------
    @Test
    public void testIsAllSuccessful_AllSuccess() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));
        trace.record(successEntry(2));

        assertTrue(trace.isAllSuccessful());
    }

    @Test
    public void testIsAllSuccessful_HasFailure() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));
        trace.record(failureEntry(2));

        assertFalse(trace.isAllSuccessful());
    }

    // ------------------------------------------------------------
    // 4. isFinallySuccessful()
    // ------------------------------------------------------------
    @Test
    public void testIsFinallySuccessful_Empty() {
        StepTrace trace = new StepTrace();
        assertFalse(trace.isFinallySuccessful());
    }

    @Test
    public void testIsFinallySuccessful_LastSuccess() {
        StepTrace trace = new StepTrace();
        trace.record(failureEntry(1));
        trace.record(successEntry(2));

        assertTrue(trace.isFinallySuccessful());
    }

    @Test
    public void testIsFinallySuccessful_LastFailure() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));
        trace.record(failureEntry(2));

        assertFalse(trace.isFinallySuccessful());
    }

    // ------------------------------------------------------------
    // 5. wasRetried()
    // ------------------------------------------------------------
    @Test
    public void testWasRetried_False() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));

        assertFalse(trace.wasRetried());
    }

    @Test
    public void testWasRetried_True() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));
        trace.record(successEntry(2));

        assertTrue(trace.wasRetried());
    }

    // ------------------------------------------------------------
    // 6. getTotalAttempts()
    // ------------------------------------------------------------
    @Test
    public void testGetTotalAttempts() {
        StepTrace trace = new StepTrace();
        trace.record(successEntry(1));
        trace.record(failureEntry(2));
        trace.record(successEntry(3));

        assertEquals(3, trace.getTotalAttempts());
    }
}