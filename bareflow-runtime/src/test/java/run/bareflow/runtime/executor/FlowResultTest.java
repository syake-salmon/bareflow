package run.bareflow.runtime.executor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.trace.StepTrace;

public class FlowResultTest {
    @Test
    public void testConstructorAndGetters() {
        final ExecutionContext ctx = new ExecutionContext();
        ctx.put("x", 123);

        final StepTrace trace = new StepTrace();

        final FlowResult result = new FlowResult(ctx, trace);

        // 参照がそのまま保持されていること
        assertSame(ctx, result.getContext());
        assertSame(trace, result.getTrace());

        // DTO が context を変更しないこと（context は mutable だが FlowResult は触らない）
        assertEquals(123, result.getContext().get("x"));
    }

    @Test
    public void testNullValuesAllowed() {
        final FlowResult result = new FlowResult(null, null);

        assertNull(result.getContext());
        assertNull(result.getTrace());
    }
}