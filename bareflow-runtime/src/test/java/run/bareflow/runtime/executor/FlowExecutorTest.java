package run.bareflow.runtime.executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.engine.FlowEngine;
import run.bareflow.core.engine.evaluator.StepEvaluator;
import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.SystemException;
import run.bareflow.core.resolver.FlowDefinitionResolver;
import run.bareflow.core.trace.StepTrace;

public class FlowExecutorTest {
    // ------------------------------------------------------------
    // 1. 正常系：Flow が成功する
    // ------------------------------------------------------------
    @Test
    public void testExecuteSuccess() throws Exception {
        final FlowDefinitionResolver resolver = mock(FlowDefinitionResolver.class);
        final StepEvaluator evaluator = mock(StepEvaluator.class);
        final StepInvoker invoker = mock(StepInvoker.class);

        final FlowDefinition definition = mock(FlowDefinition.class);
        final StepTrace trace = mock(StepTrace.class);
        final FlowEngine engine = mock(FlowEngine.class);

        when(resolver.resolve("flow1")).thenReturn(definition);
        when(engine.execute(eq(definition), any(ExecutionContext.class))).thenReturn(trace);

        final Function<FlowExecutor, FlowEngine> factory = exec -> engine;

        final FlowExecutor executor = new FlowExecutor(resolver, evaluator, invoker, factory);

        final FlowResult result = executor.execute("flow1", Map.of("x", 1));

        assertSame(trace, result.getTrace());
        assertEquals(1, result.getContext().get("x"));
    }

    // ------------------------------------------------------------
    // 2. input が null の場合
    // ------------------------------------------------------------
    @Test
    public void testExecuteWithNullInput() throws Exception {
        final FlowDefinitionResolver resolver = mock(FlowDefinitionResolver.class);
        final StepEvaluator evaluator = mock(StepEvaluator.class);
        final StepInvoker invoker = mock(StepInvoker.class);

        final FlowDefinition definition = mock(FlowDefinition.class);
        final StepTrace trace = mock(StepTrace.class);
        final FlowEngine engine = mock(FlowEngine.class);

        when(resolver.resolve("flow1")).thenReturn(definition);
        when(engine.execute(eq(definition), any(ExecutionContext.class))).thenReturn(trace);

        final Function<FlowExecutor, FlowEngine> factory = exec -> engine;

        final FlowExecutor executor = new FlowExecutor(resolver, evaluator, invoker, factory);

        final FlowResult result = executor.execute("flow1", null);

        assertTrue(result.getContext().snapshot().isEmpty());
        assertSame(trace, result.getTrace());
    }

    // ------------------------------------------------------------
    // 3. FlowDefinitionResolver が例外 → SystemException にラップされる
    // ------------------------------------------------------------
    @Test
    public void testResolverThrows() throws Exception {
        final FlowDefinitionResolver resolver = mock(FlowDefinitionResolver.class);
        final StepEvaluator evaluator = mock(StepEvaluator.class);
        final StepInvoker invoker = mock(StepInvoker.class);

        when(resolver.resolve("bad")).thenThrow(new RuntimeException("resolver failed"));

        final FlowExecutor executor = new FlowExecutor(resolver, evaluator, invoker, exec -> mock(FlowEngine.class));

        assertThrows(SystemException.class, () -> executor.execute("bad", Map.of()));
    }

    // ------------------------------------------------------------
    // 4. FlowEngine が例外 → SystemException にラップされる
    // ------------------------------------------------------------
    @Test
    public void testFlowEngineThrows() throws Exception {
        final FlowDefinitionResolver resolver = mock(FlowDefinitionResolver.class);
        final StepEvaluator evaluator = mock(StepEvaluator.class);
        final StepInvoker invoker = mock(StepInvoker.class);

        final FlowDefinition definition = mock(FlowDefinition.class);
        when(resolver.resolve("flow1")).thenReturn(definition);

        final FlowEngine engine = mock(FlowEngine.class);
        when(engine.execute(eq(definition), any(ExecutionContext.class)))
                .thenThrow(new RuntimeException("engine failed"));

        final FlowExecutor executor = new FlowExecutor(resolver, evaluator, invoker, exec -> engine);

        assertThrows(SystemException.class, () -> executor.execute("flow1", Map.of("x", 1)));
    }
}