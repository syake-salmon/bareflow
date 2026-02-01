package run.bareflow.runtime.executor;

import run.bareflow.core.context.ExecutionContext;
import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.engine.FlowEngine;
import run.bareflow.core.engine.evaluator.StepEvaluator;
import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.SystemException;
import run.bareflow.core.resolver.FlowDefinitionResolver;
import run.bareflow.core.trace.StepTrace;

import java.util.Map;
import java.util.function.Function;

/**
 * Executes a flow by resolving its definition and delegating to FlowEngine.
 * This executor is intentionally minimal and transparent.
 *
 * Responsibilities:
 * - Resolve FlowDefinition using FlowDefinitionResolver
 * - Initialize ExecutionContext with initial input
 * - Construct FlowEngine with provided evaluator and invoker
 * - Execute the flow and return FlowResult
 *
 * No additional behavior (logging, metrics, validation) is performed here.
 * Such concerns belong to higher-level runtime layers.
 */
public class FlowExecutor {
    private final FlowDefinitionResolver definitionResolver;
    private final StepEvaluator evaluator;
    private final StepInvoker invoker;
    private final Function<FlowExecutor, FlowEngine> engineFactory;

    public FlowExecutor(
            FlowDefinitionResolver definitionResolver,
            StepEvaluator evaluator,
            StepInvoker invoker,
            Function<FlowExecutor, FlowEngine> engineFactory) {

        this.definitionResolver = definitionResolver;
        this.evaluator = evaluator;
        this.invoker = invoker;
        this.engineFactory = engineFactory;
    }

    /**
     * Execute a flow by its logical name.
     *
     * @param flowName logical flow name
     * @param input    initial input context (may be empty)
     * @return FlowResult containing final context and execution trace
     */
    public FlowResult execute(String flowName, Map<String, Object> input) {
        try {
            // 1. Resolve FlowDefinition
            FlowDefinition definition = definitionResolver.resolve(flowName);

            // 2. Create ExecutionContext
            ExecutionContext context = new ExecutionContext();
            if (input != null) {
                context.merge(input);
            }

            // 3. Execute via FlowEngine
            FlowEngine engine = engineFactory.apply(this);
            StepTrace trace = engine.execute(definition, context);

            // 4. Return result
            return new FlowResult(context, trace);

        } catch (Exception e) {
            throw new SystemException("Failed to execute flow: " + flowName, e);
        }
    }
}