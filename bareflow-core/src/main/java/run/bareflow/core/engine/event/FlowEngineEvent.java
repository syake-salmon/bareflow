package run.bareflow.core.engine.event;

import java.time.Instant;
import java.util.Map;

import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.definition.StepDefinition;
import run.bareflow.core.exception.BusinessException;
import run.bareflow.core.exception.StepExecutionException;
import run.bareflow.core.exception.SystemException;
import run.bareflow.core.trace.StepTrace;
import run.bareflow.core.trace.StepTraceEntry;

public sealed interface FlowEngineEvent {
        // ------------------------------------------------------------
        // Flow lifecycle
        // ------------------------------------------------------------
        public record FlowStartEvent(
                        FlowDefinition flow,
                        Instant startTime) implements FlowEngineEvent {
        }

        public record FlowEndEvent(
                        FlowDefinition flow,
                        StepTrace trace,
                        Instant startTime,
                        Instant endTime) implements FlowEngineEvent {
        }

        // ------------------------------------------------------------
        // Step lifecycle
        // ------------------------------------------------------------
        public record StepStartEvent(
                        StepDefinition step,
                        int attempt,
                        Instant startTime) implements FlowEngineEvent {
        }

        public record InputEvaluationStartEvent(
                        StepDefinition step,
                        int attempt,
                        Instant startTime) implements FlowEngineEvent {
        }

        public record InputEvaluationEndEvent(
                        StepDefinition step,
                        int attempt,
                        Map<String, Object> evaluatedInput,
                        Instant startTime,
                        Instant endTime) implements FlowEngineEvent {
        }

        public record InvokeStartEvent(
                        StepDefinition step,
                        int attempt,
                        Map<String, Object> evaluatedInput,
                        Instant startTime) implements FlowEngineEvent {
        }

        public record InvokeEndEvent(
                        StepDefinition step,
                        int attempt,
                        Map<String, Object> rawOutput,
                        Instant startTime,
                        Instant endTime) implements FlowEngineEvent {
        }

        public record OutputEvaluationStartEvent(
                        StepDefinition step,
                        int attempt,
                        Map<String, Object> rawOutput,
                        Instant startTime) implements FlowEngineEvent {
        }

        public record OutputEvaluationEndEvent(
                        StepDefinition step,
                        int attempt,
                        Map<String, Object> mappedOutput,
                        Instant startTime,
                        Instant endTime) implements FlowEngineEvent {
        }

        public record StepEndEvent(StepDefinition step,
                        int attempt,
                        Instant startTime,
                        Instant endTime,
                        StepTraceEntry entry) implements FlowEngineEvent {
        }

        // ------------------------------------------------------------
        // Retry lifecycle
        // ------------------------------------------------------------
        public record RetryPolicyRetryEvent(
                        StepDefinition step,
                        int attempt,
                        long delayMillis) implements FlowEngineEvent {
        }

        public record OnErrorRetryEvent(
                        StepDefinition step,
                        int attempt,
                        long delayMillis) implements FlowEngineEvent {
        }

        // ------------------------------------------------------------
        // Error events
        // ------------------------------------------------------------
        public record BusinessErrorEvent(
                        StepDefinition step,
                        int attempt,
                        BusinessException error) implements FlowEngineEvent {
        }

        public record SystemErrorEvent(
                        StepDefinition step,
                        int attempt,
                        SystemException error) implements FlowEngineEvent {
        }

        public record StepExecutionErrorEvent(
                        StepDefinition step,
                        int attempt,
                        StepExecutionException error) implements FlowEngineEvent {
        }

        public record UnhandledErrorEvent(
                        StepDefinition step,
                        int attempt,
                        Throwable error) implements FlowEngineEvent {
        }
}