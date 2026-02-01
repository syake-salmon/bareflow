package run.bareflow.core.definition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RetryPolicyTest {
    @Test
    void testConstructorAndGetters() {
        RetryPolicy policy = new RetryPolicy(3, 1500L);

        assertEquals(3, policy.getMaxAttempts());
        assertEquals(1500L, policy.getDelayMillis());
    }

    @Test
    void testZeroAttemptsAllowed() {
        RetryPolicy policy = new RetryPolicy(0, 0L);

        assertEquals(0, policy.getMaxAttempts());
        assertEquals(0L, policy.getDelayMillis());
    }

    @Test
    void testNegativeValuesAreStoredAsIs() {
        RetryPolicy policy = new RetryPolicy(-1, -100L);

        assertEquals(-1, policy.getMaxAttempts());
        assertEquals(-100L, policy.getDelayMillis());
    }
}