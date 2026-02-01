package run.bareflow.core.engine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.nullable;

import java.util.*;

import org.junit.jupiter.api.Test;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.definition.*;
import run.bareflow.core.engine.evaluator.StepEvaluator;
import run.bareflow.core.engine.event.FlowEngineEvent;
import run.bareflow.core.engine.event.FlowEngineEventListener;
import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.*;
import run.bareflow.core.trace.StepTrace;

public class FlowEngineTest {
    // ------------------------------------------------------------
    // Utility classes
    // ------------------------------------------------------------
    private static class RecordingListener implements FlowEngineEventListener {
        final List<FlowEngineEvent> events = new ArrayList<>();

        @Override
        public void onEvent(FlowEngineEvent event) {
            events.add(event);
        }
    }

    private static class PassthroughEvaluator implements StepEvaluator {
        @Override
        public Map<String, Object> evaluateInput(Map<String, Object> input, ExecutionContext ctx) {
            return input;
        }

        @Override
        public Map<String, Object> evaluateOutput(Map<String, Object> output, Map<String, Object> raw,
                ExecutionContext ctx) {
            return raw;
        }
    }

    private static class FixedInvoker implements StepInvoker {
        private final Map<String, Object> output;

        FixedInvoker(Map<String, Object> output) {
            this.output = output;
        }

        @Override
        public Map<String, Object> invoke(String module, String operation, Map<String, Object> input) {
            return output;
        }
    }

    // ------------------------------------------------------------
    // 1. 正常系
    // ------------------------------------------------------------
    @Test
    public void test_successful_step_executes_all_events() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();
        StepInvoker invoker = new FixedInvoker(Map.of("result", 123));

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "test-step",
                "mod",
                "op",
                Map.of("x", 1),
                Map.of("y", "${result}"),
                null,
                null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        ExecutionContext ctx = new ExecutionContext();

        StepTrace trace = engine.execute(flow, ctx);

        assertEquals(1, trace.getEntries().size());
        assertEquals(123, ctx.get("result"));

        assertTrue(listener.events.get(0) instanceof FlowEngineEvent.FlowStartEvent);
        assertTrue(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.StepStartEvent));
        assertTrue(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.InvokeStartEvent));
        assertTrue(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.StepEndEvent));
        assertTrue(listener.events.get(listener.events.size() - 1) instanceof FlowEngineEvent.FlowEndEvent);
    }

    // ------------------------------------------------------------
    // 2. RetryPolicy
    // ------------------------------------------------------------
    @Test
    public void test_retry_policy_retries_once() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        StepInvoker invoker = new StepInvoker() {
            int count = 0;

            @Override
            public Map<String, Object> invoke(String module, String operation, Map<String, Object> input) {
                count++;
                if (count == 1) {
                    throw new SystemException("fail once");
                }
                return Map.of("ok", true);
            }
        };

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "retry-step",
                "mod",
                "op",
                Map.of(),
                Map.of(),
                new RetryPolicy(2, 0),
                null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        engine.execute(flow, new ExecutionContext());

        long retryEvents = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.RetryPolicyRetryEvent)
                .count();

        assertEquals(1, retryEvents);
    }

    // ------------------------------------------------------------
    // 3. onError.RETRY
    // ------------------------------------------------------------
    @Test
    public void test_onError_retry_fires_event() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        StepInvoker invoker = new StepInvoker() {
            int count = 0;

            @Override
            public Map<String, Object> invoke(String module, String operation, Map<String, Object> input) {
                count++;
                if (count == 1) {
                    throw new BusinessException("fail once");
                }
                return Map.of("ok", true);
            }
        };

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "onerror-retry",
                "mod",
                "op",
                Map.of(),
                Map.of(),
                null,
                new OnErrorDefinition(OnErrorDefinition.Action.RETRY, 0, null));

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        engine.execute(flow, new ExecutionContext());

        long retryEvents = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.OnErrorRetryEvent)
                .count();

        assertEquals(1, retryEvents);
    }

    // ------------------------------------------------------------
    // 4. onError.STOP
    // ------------------------------------------------------------
    @Test
    public void test_onError_stop_throws_exception() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        StepInvoker invoker = (m, o, i) -> {
            throw new BusinessException("fail");
        };

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "stop-step",
                "mod",
                "op",
                Map.of(),
                Map.of(),
                null,
                new OnErrorDefinition(OnErrorDefinition.Action.STOP, 0, null));

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        assertThrows(StepExecutionException.class, () -> engine.execute(flow, new ExecutionContext()));
    }

    // ------------------------------------------------------------
    // 5. onError.CONTINUE
    // ------------------------------------------------------------
    @Test
    public void test_onError_continue_moves_to_next_step() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        FlowEngine engine = new FlowEngine(evaluator, (m, o, i) -> {
            if (o.equals("op1"))
                throw new BusinessException("fail");
            if (o.equals("op2"))
                return Map.of("ok", true);
            return null;
        }, listener);

        StepDefinition step1 = new StepDefinition(
                "step1",
                "mod",
                "op1",
                Map.of(),
                Map.of(),
                null,
                new OnErrorDefinition(OnErrorDefinition.Action.CONTINUE, 0, null));

        StepDefinition step2 = new StepDefinition(
                "step2",
                "mod",
                "op2",
                Map.of(),
                Map.of(),
                null,
                null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(step1, step2), null, null);

        engine.execute(flow, new ExecutionContext());

        assertTrue(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.BusinessErrorEvent));
        assertTrue(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.StepEndEvent));
    }

    // ------------------------------------------------------------
    // 6. 複数ステップの順序
    // ------------------------------------------------------------
    @Test
    public void test_multiple_steps_execute_in_order() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();
        StepInvoker invoker = new FixedInvoker(Map.of("v", 1));

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition s1 = new StepDefinition("s1", "m", "o", Map.of(), Map.of(), null, null);
        StepDefinition s2 = new StepDefinition("s2", "m", "o", Map.of(), Map.of(), null, null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(s1, s2), null, null);

        engine.execute(flow, new ExecutionContext());

        List<String> stepStarts = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.StepStartEvent)
                .map(e -> ((FlowEngineEvent.StepStartEvent) e).step().getName())
                .toList();

        assertEquals(List.of("s1", "s2"), stepStarts);
    }

    // ------------------------------------------------------------
    // 7. OutputMapping が空
    // ------------------------------------------------------------
    @Test
    public void test_empty_output_mapping_skips_output_events() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();
        StepInvoker invoker = new FixedInvoker(Map.of("x", 1));

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "no-output",
                "m",
                "o",
                Map.of(),
                Map.of(), // empty output mapping
                null,
                null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        engine.execute(flow, new ExecutionContext());

        assertFalse(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.OutputEvaluationStartEvent));
        assertFalse(listener.events.stream().anyMatch(e -> e instanceof FlowEngineEvent.OutputEvaluationEndEvent));
    }

    // ------------------------------------------------------------
    // 8. RetryPolicy と onError.RETRY の優先順位
    // ------------------------------------------------------------
    @Test
    public void test_retry_policy_takes_precedence_over_onError_retry() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        SystemException exceptedException = new SystemException("fail");
        StepInvoker invoker = (m, o, i) -> {
            throw exceptedException;
        };

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "step",
                "m",
                "o",
                Map.of(),
                Map.of(),
                new RetryPolicy(2, 0),
                new OnErrorDefinition(OnErrorDefinition.Action.RETRY, 0, null));

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        // assertThrows(StepExecutionException.class, () -> engine.execute(flow, new
        // ExecutionContext()));
        StepTrace trace = engine.execute(flow, new ExecutionContext());

        assertEquals(exceptedException, trace.getEntries().get(0).getError());
        assertEquals(exceptedException, trace.getEntries().get(1).getError());

        long retryPolicyEvents = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.RetryPolicyRetryEvent)
                .count();

        long onErrorRetryEvents = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.OnErrorRetryEvent)
                .count();

        assertEquals(1, retryPolicyEvents); // RetryPolicy は 1 回
        assertEquals(1, onErrorRetryEvents); // RetryPolicy が尽きた後に onError.RETRY が 1 回
    }

    // ------------------------------------------------------------
    // 9. StepExecutionException の retry 意味論
    // ------------------------------------------------------------
    @Test
    public void test_step_execution_exception_retries_like_system_exception() {
        RecordingListener listener = new RecordingListener();
        StepEvaluator evaluator = new PassthroughEvaluator();

        StepInvoker invoker = new StepInvoker() {
            int count = 0;

            @Override
            public Map<String, Object> invoke(String module, String operation, Map<String, Object> input) {
                count++;
                if (count == 1) {
                    throw new StepExecutionException("fail");
                }
                return Map.of("ok", true);
            }
        };

        FlowEngine engine = new FlowEngine(evaluator, invoker, listener);

        StepDefinition step = new StepDefinition(
                "step",
                "m",
                "o",
                Map.of(),
                Map.of(),
                new RetryPolicy(2, 0),
                null);

        FlowDefinition flow = new FlowDefinition("flow", List.of(step), null, null);

        engine.execute(flow, new ExecutionContext());

        long retryEvents = listener.events.stream()
                .filter(e -> e instanceof FlowEngineEvent.RetryPolicyRetryEvent)
                .count();

        assertEquals(1, retryEvents);
    }
}