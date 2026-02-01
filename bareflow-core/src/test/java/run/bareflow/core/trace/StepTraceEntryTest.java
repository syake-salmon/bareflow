package run.bareflow.core.trace;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class StepTraceEntryTest {
    @Test
    public void testSuccessEntry() {
        String stepName = "step1";
        Map<String, Object> before = Map.of("a", 1);
        Map<String, Object> input = Map.of("x", 10);
        Map<String, Object> output = Map.of("y", 20);
        Throwable error = null;
        Instant start = Instant.now();
        Instant end = start.plusMillis(50);
        int attempt = 1;

        StepTraceEntry entry = new StepTraceEntry(
                stepName,
                before,
                input,
                output,
                error,
                start,
                end,
                attempt);

        assertEquals(stepName, entry.getStepName());
        assertEquals(before, entry.getBeforeContext());
        assertEquals(input, entry.getEvaluatedInput());
        assertEquals(output, entry.getRawOutput());
        assertNull(entry.getError());
        assertEquals(start, entry.getStartTime());
        assertEquals(end, entry.getEndTime());
        assertEquals(attempt, entry.getAttempt());
        assertTrue(entry.isSuccess());
    }

    @Test
    public void testFailureEntry() {
        String stepName = "step2";
        Map<String, Object> before = Map.of("b", 2);
        Map<String, Object> input = Map.of("i", 99);
        Map<String, Object> output = null;
        Throwable error = new RuntimeException("boom");
        Instant start = Instant.now();
        Instant end = start.plusMillis(30);
        int attempt = 2;

        StepTraceEntry entry = new StepTraceEntry(
                stepName,
                before,
                input,
                output,
                error,
                start,
                end,
                attempt);

        assertEquals(stepName, entry.getStepName());
        assertEquals(before, entry.getBeforeContext());
        assertEquals(input, entry.getEvaluatedInput());
        assertNull(entry.getRawOutput());
        assertEquals(error, entry.getError());
        assertEquals(start, entry.getStartTime());
        assertEquals(end, entry.getEndTime());
        assertEquals(attempt, entry.getAttempt());
        assertFalse(entry.isSuccess());
    }
}