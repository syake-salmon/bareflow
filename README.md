# BareFlow - Minimal Core. Maximum Clarity.

[English](README.md) | [日本語](README_jp.md)

# Table of Contents

1. [Introduction](#1-introduction)  
2. [Core Principles](#2-core-principles)  
3. [Architecture Overview](#3-architecture-overview)  
4. [Core Concepts](#4-core-concepts)  
5. [Execution Model](#5-execution-model)  
6. [Runtime Components](#6-runtime-components)  
7. [YAML Flow Definitions](#7-yaml-flow-definitions)  
8. [Error Handling](#8-error-handling)  
9. [Tracing & Observability](#9-tracing--observability)  
10. [Extending BareFlow](#10-extending-bareflow)  
11. [License](#11-license)

# 1. Introduction

BareFlow is a minimal, deterministic flow execution engine for Java.  
It provides a small set of well-defined primitives for describing and executing flows without introducing hidden behavior, complex expression languages, or framework-level magic.

BareFlow focuses on clarity and predictability:

- **Minimalism** — only the essential concepts are included.  
- **Determinism** — the same inputs always produce the same execution behavior.  
- **Transparency** — every step, retry, and error is observable.  
- **No hidden magic** — no implicit defaults, no global state, no reflection-based guessing.  
- **Strict separation of core and runtime** — the core defines pure models and execution rules; the runtime provides pluggable implementations.

BareFlow is designed for systems that require:

- explicit and predictable step-by-step execution  
- clear error-handling semantics  
- simple module invocation  
- full traceability of execution  
- easy testing and debugging  
- the ability to replace or extend runtime components without modifying the core

The result is a flow engine that is easy to understand, easy to test, and easy to extend—while remaining fully transparent in its behavior.

# 2. Core Principles

BareFlow is built on a small set of foundational principles that guide every design decision in the framework.  
These principles ensure that flows remain predictable, transparent, and easy to reason about, regardless of scale or complexity.

## 2.1 Minimalism
BareFlow includes only the essential concepts required to define and execute flows.  
No implicit defaults, no hidden behaviors, and no unnecessary abstractions are introduced.  
Every model and component exists because it is required for correctness, not convenience.

## 2.2 Determinism
Given the same inputs, BareFlow always produces the same execution behavior.  
There are no timing‑dependent side effects, no global state, and no non-deterministic evaluation rules.  
Determinism ensures that flows are testable, debuggable, and reliable.

## 2.3 Transparency
All execution details are observable.  
BareFlow emits structured events for every phase of execution and records complete step traces, including:

- evaluated input  
- raw output  
- mapped output  
- retry attempts  
- errors  
- timestamps  

Nothing is hidden from the user.

## 2.4 No Hidden Magic
BareFlow avoids dynamic behavior that obscures control flow or execution semantics.  
Specifically:

- no expression languages beyond simple placeholders  
- no nested placeholder resolution  
- no automatic validation or transformation  
- no reflection-based guessing  
- no implicit retry or error-handling behavior  

Users always know exactly what the engine will do.

## 2.5 Strict Separation of Core and Runtime
BareFlow is divided into two modules with clearly defined responsibilities:

- **Core** — pure models, execution rules, and deterministic engine behavior  
- **Runtime** — pluggable implementations for evaluation, invocation, resolution, logging, and parsing  

This separation ensures that the core remains stable and predictable, while the runtime can evolve or be replaced without affecting the engine’s semantics.

## 2.6 Composability and Extensibility
Although minimal, BareFlow is designed to be extended.  
Users may provide custom implementations for:

- StepEvaluator  
- StepInvoker  
- ModuleResolver  
- LoggingAdapter  
- FlowEngineEventListener  

Extensibility is explicit and controlled, never implicit or magical.

---

These principles define BareFlow’s identity and ensure that the framework remains simple, predictable, and trustworthy.

# 3. Architecture Overview

BareFlow is organized into two clearly separated modules—**Core** and **Runtime**—each with distinct responsibilities.  
This separation ensures that the execution semantics remain stable and deterministic, while runtime behavior can be extended or replaced without modifying the core engine.

## 3.1 Module Structure

### **Core Module (`bareflow-core`)**
The core module defines the fundamental building blocks of BareFlow:

- immutable flow models  
- deterministic execution engine  
- placeholder evaluation rules  
- event model  
- trace model  
- exception types  

The core contains **no reflection**, **no I/O**, **no logging**, and **no external dependencies**.  
It is pure logic.

### **Runtime Module (`bareflow-runtime`)**
The runtime module provides pluggable implementations:

- module resolution  
- step invocation  
- placeholder evaluation  
- YAML parsing  
- logging integration  
- event fan-out  

Runtime components can be replaced or extended without altering the core.

---

## 3.2 Flow Lifecycle Overview

A flow execution proceeds through the following phases:

1. **Input Evaluation**  
   Placeholders in the step’s input mapping are resolved from the `ExecutionContext`.

2. **Invocation**  
   The `StepInvoker` calls the target module and operation with the evaluated input.

3. **Output Evaluation**  
   Placeholders in the output mapping are resolved first from the raw output, then from the `ExecutionContext`.

4. **Context Merge**  
   Mapped output is merged into the `ExecutionContext`.

5. **Retry Handling**  
   System-level errors may trigger retries based on `RetryPolicy`.

6. **OnError Handling**  
   Business-level error handling is applied after retries are exhausted.

7. **Event Emission**  
   Each phase emits structured events for observability.

8. **Trace Recording**  
   A `StepTraceEntry` is created for every step.

This lifecycle is deterministic and identical for every step.

---

## 3.3 Event Model

BareFlow emits structured events for every significant moment in the flow:

- flow start / end  
- step start / end  
- input evaluation start / end  
- invocation start / end  
- output evaluation start / end  
- retry events  
- error events  

Events are delivered to a `FlowEngineEventListener`, allowing:

- logging  
- monitoring  
- debugging  
- custom instrumentation  

The event model is purely observational and does not affect execution.

---

## 3.4 Trace Model

BareFlow records a complete execution trace:

- evaluated input  
- raw output  
- mapped output  
- errors  
- retry attempts  
- timestamps  
- step-level metadata  

`StepTrace` aggregates all `StepTraceEntry` objects and represents the full execution history.

Traces are immutable snapshots and can be safely logged, serialized, or inspected.

---

## 3.5 Separation of Responsibilities

| Concern | Core | Runtime |
|--------|------|---------|
| Flow model | ✔ | |
| Execution engine | ✔ | |
| Placeholder evaluation rules | ✔ | |
| Event model | ✔ | |
| Trace model | ✔ | |
| Module resolution | | ✔ |
| Step invocation | | ✔ |
| YAML parsing | | ✔ |
| Logging integration | | ✔ |
| Event fan-out | | ✔ |

This separation ensures that:

- the **core remains stable and deterministic**  
- the **runtime remains flexible and replaceable**  

BareFlow’s architecture is intentionally simple, explicit, and predictable.

# 4. Core Concepts

BareFlow defines a small set of core concepts that describe flows, steps, execution state, error-handling behavior, and traceability.  
These concepts form the foundation of the engine’s deterministic execution model.

## 4.1 FlowDefinition
`FlowDefinition` is an immutable structural representation of an entire flow.  
It contains:

- **name** — logical identifier of the flow  
- **steps** — ordered list of `StepDefinition` objects  
- **onError** — optional flow-level default error-handling policy  
- **metadata** — optional user-defined metadata with no effect on execution  

`FlowDefinition` contains no behavior.  
Validation, parsing, and defaulting are responsibilities of higher-level components such as `FlowDefinitionResolver` or the runtime parser.

## 4.2 StepDefinition
`StepDefinition` describes a single step in a flow.  
It includes:

- **name** — logical step identifier  
- **module** — target module name  
- **operation** — operation within the module  
- **input** — raw input mapping (evaluated before invocation)  
- **output** — raw output mapping (evaluated after invocation)  
- **retryPolicy** — optional system-level retry configuration  
- **onError** — optional step-level error-handling policy  

`StepDefinition` is purely structural.  
The engine interprets it deterministically without implicit defaults.

## 4.3 RetryPolicy
`RetryPolicy` defines system-level retry behavior for a step.  
It applies only to:

- `SystemException`  
- `StepExecutionException`  

It never applies to `BusinessException`.

Fields:

- **maxAttempts** — total number of attempts (including the first)  
- **delayMillis** — delay between retry attempts  

Retry behavior is deterministic and does not overlap with `OnErrorDefinition`.

## 4.4 OnErrorDefinition
`OnErrorDefinition` defines how the engine reacts when a step fails after system-level retries are exhausted.

Actions:

- **STOP** — propagate the error and stop the flow  
- **CONTINUE** — ignore the error and proceed to the next step  
- **RETRY** — perform exactly one business-level retry  

Optional:

- **output** — mapping evaluated only when an error occurs  
- **delayMillis** — delay before a business-level retry  

Step-level `onError` overrides the flow-level default.

## 4.5 ExecutionContext
`ExecutionContext` is a mutable, flat key-value store used to pass data between steps.

Characteristics:

- keys are simple strings  
- values are arbitrary objects  
- merging overwrites existing keys  
- snapshots are immutable copies  
- no hierarchical resolution is performed  

The context is the primary mechanism for data flow within a BareFlow execution.

## 4.6 StepTrace and StepTraceEntry
BareFlow records a complete execution history.

### StepTraceEntry
Represents the result of a single step:

- evaluated input  
- raw output  
- mapped output  
- errors  
- retry attempts  
- timestamps  
- step metadata  

Entries are immutable snapshots.

### StepTrace
Aggregates all `StepTraceEntry` objects for the entire flow.  
It is returned by the engine and can be logged, serialized, or inspected.

---

These core concepts define the structure and semantics of BareFlow.  
They are intentionally minimal, explicit, and free of hidden behavior, ensuring that flows remain predictable and easy to reason about.

# 5. Execution Model

BareFlow’s execution model is deterministic, transparent, and free of hidden behavior.  
Every step follows the same lifecycle, and all evaluation, invocation, retry, and error-handling rules are explicit.

## 5.1 Step Lifecycle

Each step in a flow is executed through the following phases:

1. **Input Evaluation**  
   Raw input mapping from `StepDefinition` is evaluated using the `ExecutionContext`.  
   Only flat placeholders of the form `${name}` are supported.

2. **Invocation**  
   The `StepInvoker` calls the target module and operation with the evaluated input.  
   The result is returned as a raw output map.

3. **Output Evaluation**  
   Raw output is combined with the output mapping from `StepDefinition`.  
   Placeholders are resolved first from raw output, then from the `ExecutionContext`.

4. **Context Merge**  
   Mapped output is merged into the `ExecutionContext`.  
   Existing keys are overwritten.

5. **Retry Handling**  
   System-level errors may trigger retries based on `RetryPolicy`.

6. **OnError Handling**  
   After system-level retries are exhausted, `OnErrorDefinition` determines the next action.

7. **Event Emission**  
   Each phase emits structured events for observability.

8. **Trace Recording**  
   A `StepTraceEntry` is created to capture the full result of the step.

This lifecycle is identical for every step and contains no implicit shortcuts or hidden logic.

---

## 5.2 Input Evaluation

Input evaluation is performed by a `StepEvaluator`.  
Rules:

- Only flat placeholders `${name}` are supported.  
- Literal values are returned as-is.  
- Unresolved placeholders evaluate to `null`.  
- No nested expressions (`${a.b}`) or scripting languages are supported.

Input evaluation is pure and deterministic.

---

## 5.3 Invocation

Invocation is performed by a `StepInvoker`.  
Responsibilities:

- locate the target module  
- call the specified operation  
- return a raw output map  

The core engine does not define how modules are implemented.  
The runtime provides a default reflection-based invoker, but users may replace it.

---

## 5.4 Output Evaluation

Output evaluation follows the same placeholder rules as input evaluation, with one difference:

Resolution order:

1. raw output  
2. execution context  
3. unresolved → `null`

Only keys defined in the output mapping are included in the mapped output.

---

## 5.5 Retry Semantics

`RetryPolicy` applies only to:

- `SystemException`  
- `StepExecutionException`  

It never applies to `BusinessException`.

Rules:

- attempts start at 1  
- `maxAttempts` includes the first attempt  
- retry is allowed while `attempt < maxAttempts`  
- `delayMillis` defines the wait time between attempts  

Retry behavior is deterministic and does not overlap with `OnErrorDefinition`.

---

## 5.6 OnError Semantics

After system-level retries are exhausted, `OnErrorDefinition` determines the next action:

- **STOP** — propagate the error and stop the flow  
- **CONTINUE** — ignore the error and proceed to the next step  
- **RETRY** — perform exactly one business-level retry  

Optional:

- `delayMillis` — wait before retrying  
- `output` — mapping evaluated only when an error occurs  

Step-level `onError` overrides the flow-level default.

---

## 5.7 Event Emission

BareFlow emits structured events for every significant moment:

- flow start / end  
- step start / end  
- input evaluation start / end  
- invocation start / end  
- output evaluation start / end  
- retry events  
- error events  

Events are delivered to a `FlowEngineEventListener`.  
They do not affect execution.

---

## 5.8 Trace Recording

For each step, a `StepTraceEntry` is created containing:

- evaluated input  
- raw output  
- mapped output  
- errors  
- retry attempts  
- timestamps  

`StepTrace` aggregates all entries and represents the full execution history.

Traces are immutable and safe to log, serialize, or inspect.

---

BareFlow’s execution model is intentionally simple, explicit, and deterministic.  
It ensures that flows behave exactly as defined, with no hidden behavior or implicit logic.

# 6. Runtime Components

The runtime module provides concrete, pluggable implementations that complement the deterministic core engine.  
While the core defines execution rules and data models, the runtime supplies the operational behavior required to run flows in real applications.

Runtime components can be replaced or extended without modifying the core, ensuring flexibility while preserving deterministic semantics.

---

## 6.1 FlowExecutor

`FlowExecutor` is the high-level entry point for executing flows.  
It coordinates:

- YAML parsing  
- flow resolution  
- evaluator selection  
- invoker selection  
- event listener configuration  

Responsibilities:

- load a `FlowDefinition` (typically from YAML)  
- prepare runtime components  
- execute the flow using `FlowEngine`  
- return a `FlowResult` containing the final context and trace  

`FlowExecutor` is the recommended way to run flows in production.

---

## 6.2 DefaultStepInvoker

`DefaultStepInvoker` is the runtime’s default implementation of `StepInvoker`.  
It performs reflection-based invocation of module operations.

Responsibilities:

- locate the target module via `ModuleResolver`  
- call the specified operation method  
- return a raw output map  

Characteristics:

- deterministic reflection behavior  
- no dynamic guessing  
- no hidden conventions  

Users may replace this component to integrate with custom module systems.

---

## 6.3 DefaultModuleResolver

`DefaultModuleResolver` resolves module names to module instances.  
It provides a simple registry-based lookup mechanism.

Responsibilities:

- maintain a mapping of module names to module objects  
- return the module instance for a given name  
- throw an error if the module is not found  

Users may replace this resolver to integrate with dependency injection frameworks or service locators.

---

## 6.4 DefaultStepEvaluator

`DefaultStepEvaluator` implements BareFlow’s placeholder evaluation rules:

- only flat placeholders `${name}`  
- no nested expressions  
- no scripting languages  
- unresolved placeholders → `null`  

Responsibilities:

- evaluate input mappings  
- evaluate output mappings  
- resolve placeholders from raw output and `ExecutionContext`  

This evaluator is deterministic and side-effect–free.

---

## 6.5 FlowYamlParser

`FlowYamlParser` converts YAML definitions into:

- `FlowDefinition`  
- `StepDefinition`  
- `RetryPolicy`  
- `OnErrorDefinition`  

Characteristics:

- minimal structural mapping  
- no validation  
- no transformation  
- no implicit defaults  

The parser ensures that YAML definitions map directly to core models without hidden behavior.

---

## 6.6 LoggingAdapter and LogFormatter

BareFlow does not perform logging by default.  
Instead, it provides two pluggable interfaces:

### LoggingAdapter
Defines how log messages are emitted.  
Users may integrate with:

- SLF4J  
- Log4j  
- System.out  
- custom logging backends  

### LogFormatter
Controls how log messages are formatted.  
Formatting is fully customizable.

This design keeps the core free of logging dependencies.

---

## 6.7 CompositeFlowEngineEventListener

`CompositeFlowEngineEventListener` allows multiple event listeners to be combined.  
It fans out events to all registered listeners.

Use cases:

- logging  
- metrics  
- debugging  
- monitoring  
- custom instrumentation  

Event listeners observe execution but do not influence it.

---

## 6.8 Replaceability and Extensibility

All runtime components are replaceable:

| Component | Interface | Default Implementation |
|----------|-----------|------------------------|
| Step invocation | `StepInvoker` | `DefaultStepInvoker` |
| Module resolution | `ModuleResolver` | `DefaultModuleResolver` |
| Placeholder evaluation | `StepEvaluator` | `DefaultStepEvaluator` |
| YAML parsing | — | `FlowYamlParser` |
| Logging | `LoggingAdapter` | user-provided |
| Event handling | `FlowEngineEventListener` | user-provided / composite |

This ensures that BareFlow can adapt to different environments, architectures, and integration requirements without modifying the core engine.

---

The runtime module provides practical, pluggable behavior while preserving the core’s deterministic execution model.  
Users may adopt the defaults or replace any component to suit their application architecture.

# 7. YAML Flow Definitions

BareFlow provides a minimal YAML-based format for defining flows.  
The YAML DSL maps directly to core model classes without validation, transformation, or implicit defaults.  
This ensures that YAML definitions remain transparent and predictable.

## 7.1 Design Philosophy

The YAML format follows BareFlow’s core principles:

- **Minimal** — only essential fields are supported  
- **Deterministic** — no dynamic expressions or scripting languages  
- **Transparent** — YAML maps directly to core models  
- **No hidden magic** — no automatic defaults or inference  
- **Structural only** — validation and enrichment are not performed  

The YAML parser (`FlowYamlParser`) performs a simple structural mapping and nothing more.

---

## 7.2 Supported Top-Level Fields

A YAML flow definition may contain:

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Logical identifier of the flow |
| `steps` | list | Ordered list of step definitions |
| `onError` | object (optional) | Flow-level default error-handling policy |
| `metadata` | map (optional) | Arbitrary user-defined metadata |

All fields map directly to `FlowDefinition`.

---

## 7.3 Step Definition Fields

Each step in `steps` supports:

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Logical step identifier |
| `module` | string | Target module name |
| `operation` | string | Operation within the module |
| `input` | map (optional) | Raw input mapping |
| `output` | map (optional) | Raw output mapping |
| `retryPolicy` | object (optional) | System-level retry configuration |
| `onError` | object (optional) | Step-level error-handling policy |

These fields map directly to `StepDefinition`.

---

## 7.4 RetryPolicy Fields

`retryPolicy` supports:

| Field | Type | Description |
|-------|------|-------------|
| `maxAttempts` | integer | Total number of attempts (including the first) |
| `delayMillis` | integer | Delay between retry attempts |

Maps directly to `RetryPolicy`.

---

## 7.5 OnErrorDefinition Fields

`onError` supports:

| Field | Type | Description |
|-------|------|-------------|
| `action` | string | One of: `STOP`, `CONTINUE`, `RETRY` |
| `delayMillis` | integer (optional) | Delay before retrying |
| `output` | map (optional) | Mapping evaluated only when an error occurs |

Maps directly to `OnErrorDefinition`.

---

## 7.6 Placeholder Rules

BareFlow supports only **flat placeholders**:

```
${name}
```

Rules:

- no nested expressions (`${a.b}`)  
- no scripting languages  
- unresolved placeholders → `null`  
- input placeholders resolve from `ExecutionContext`  
- output placeholders resolve from raw output, then from `ExecutionContext`  

These rules are identical to the core evaluator.

---

## 7.7 Example YAML Definition

```
name: sampleFlow

steps:
  - name: hello
    module: sampleModule
    operation: hello
    input:
      name: "${userName}"
    output:
      message: "${result}"

  - name: finalize
    module: sampleModule
    operation: finalize
    input:
      message: "${message}"

onError:
  action: STOP

metadata:
  version: "1.0"
  author: "Keisuke"
```

This YAML maps directly to:

- `FlowDefinition`  
- `StepDefinition`  
- `RetryPolicy` (none in this example)  
- `OnErrorDefinition`  
- metadata map  

No transformation or validation is performed.

---

BareFlow’s YAML DSL is intentionally minimal and structural.  
It provides a clear, predictable way to define flows without introducing hidden behavior or complex expression languages.

# 8. Error Handling

BareFlow provides a clear and deterministic error-handling model.  
System-level failures and business-level failures are treated separately, and all retry and onError behavior is explicit.  
There is no hidden fallback logic, no automatic recovery, and no implicit defaults.

## 8.1 Error Types

BareFlow distinguishes between three categories of errors:

### **BusinessException**
Represents a business-level failure.  
Examples:

- invalid input  
- domain rule violation  
- expected business error  

Characteristics:

- never retried by `RetryPolicy`  
- may trigger `OnErrorDefinition`  
- treated as a controlled failure  

### **SystemException**
Represents a system-level failure.  
Examples:

- network failure  
- I/O error  
- unexpected infrastructure issue  

Characteristics:

- eligible for system-level retries via `RetryPolicy`  
- may trigger `OnErrorDefinition` after retries are exhausted  

### **StepExecutionException**
Represents an unexpected failure during step invocation.  
Examples:

- reflection error  
- module invocation failure  
- unexpected runtime exception  

Characteristics:

- eligible for system-level retries  
- may trigger `OnErrorDefinition` after retries are exhausted  

---

## 8.2 System-Level Retry (RetryPolicy)

`RetryPolicy` defines how the engine handles system-level failures.

Applicable to:

- `SystemException`  
- `StepExecutionException`  

Never applied to:

- `BusinessException`

Rules:

- attempts start at 1  
- `maxAttempts` includes the first attempt  
- retry is allowed while `attempt < maxAttempts`  
- `delayMillis` defines the wait time between attempts  

System-level retries are deterministic and do not overlap with business-level retries.

---

## 8.3 Business-Level Error Handling (OnErrorDefinition)

After system-level retries are exhausted, `OnErrorDefinition` determines the next action.

Supported actions:

### **STOP**
Propagate the error and stop the flow immediately.

### **CONTINUE**
Ignore the error and proceed to the next step.  
If an `output` mapping is provided, it is evaluated and merged into the `ExecutionContext`.

### **RETRY**
Perform exactly one business-level retry.  
This retry is independent of `RetryPolicy`.

Optional fields:

- `delayMillis` — wait before retrying  
- `output` — mapping evaluated only when an error occurs  

Step-level `onError` overrides the flow-level default.

---

## 8.4 Interaction Between RetryPolicy and OnErrorDefinition

The interaction is strictly ordered:

1. **System-level retries (RetryPolicy)**  
   Applied only to system-level errors.

2. **Business-level error handling (OnErrorDefinition)**  
   Applied after system-level retries are exhausted.

3. **Final action**  
   STOP, CONTINUE, or RETRY (exactly one retry).

There is no overlap or ambiguity between the two mechanisms.

---

## 8.5 Error Events

BareFlow emits structured events for all error-related behavior:

- `BusinessErrorEvent`  
- `SystemErrorEvent`  
- `StepExecutionErrorEvent`  
- `RetryPolicyRetryEvent`  
- `OnErrorRetryEvent`  
- `UnhandledErrorEvent`  

These events provide full observability without affecting execution.

---

BareFlow’s error-handling model is explicit, deterministic, and free of hidden behavior.  
System-level retries and business-level error handling are clearly separated, ensuring predictable and debuggable flow execution.

# 9. Tracing & Observability

BareFlow provides full observability into flow execution through two complementary mechanisms:  
**structured events** and **immutable execution traces**.  
These mechanisms ensure that every step, retry, and error is visible and debuggable without introducing hidden behavior.

## 9.1 Design Goals

BareFlow’s observability model is designed to be:

- **complete** — every significant moment is captured  
- **deterministic** — events and traces follow a predictable structure  
- **non-intrusive** — observability never affects execution  
- **extensible** — users may attach custom listeners or logging adapters  
- **transparent** — no hidden or implicit instrumentation  

Observability is a first-class concern in BareFlow.

---

## 9.2 Event Stream

During execution, the engine emits structured events for:

- flow start / end  
- step start / end  
- input evaluation start / end  
- invocation start / end  
- output evaluation start / end  
- retry attempts  
- business-level retries  
- system-level errors  
- business-level errors  
- unhandled errors  

Events are delivered to a `FlowEngineEventListener`.

### Characteristics

- events are immutable  
- events do not influence execution  
- multiple listeners may be combined using `CompositeFlowEngineEventListener`  
- listeners may perform logging, metrics collection, debugging, or custom instrumentation  

The event stream provides real-time observability.

---

## 9.3 StepTraceEntry

For each step, BareFlow records a `StepTraceEntry` containing:

- evaluated input  
- raw output  
- mapped output  
- errors  
- retry attempts  
- timestamps  
- step metadata  

Each entry is an immutable snapshot of the step’s execution.

### Purpose

- debugging  
- auditing  
- logging  
- test verification  
- external inspection  

`StepTraceEntry` captures the complete state of a step at the moment it finishes.

---

## 9.4 StepTrace

`StepTrace` aggregates all `StepTraceEntry` objects for the entire flow.  
It represents the full execution history and is returned by the engine.

Characteristics:

- immutable  
- ordered by execution  
- safe to serialize or log  
- independent of runtime components  

`StepTrace` is the authoritative record of what happened during execution.

---

## 9.5 Logging Integration

BareFlow does not perform logging by default.  
Instead, it provides two pluggable interfaces:

- **LoggingAdapter** — defines how log messages are emitted  
- **LogFormatter** — defines how messages are formatted  

Users may integrate with:

- SLF4J  
- Log4j  
- System.out  
- custom logging backends  

Logging is optional and fully customizable.

---

## 9.6 Observability Without Side Effects

BareFlow guarantees that:

- events never modify execution state  
- traces never influence control flow  
- logging is opt-in  
- observability is deterministic and reproducible  

This ensures that flows behave identically whether observability is enabled or not.

---

BareFlow’s tracing and observability model provides complete visibility into execution while preserving determinism and purity.  
It enables debugging, monitoring, and auditing without introducing hidden behavior or runtime dependencies.

# 10. Extending BareFlow

BareFlow is designed to be minimal at its core while remaining highly extensible.  
The core engine defines deterministic execution rules, and the runtime provides replaceable components that users may customize to fit their application architecture.

This chapter describes all official extension points and the responsibilities of each component.

---

## 10.1 Extension Philosophy

BareFlow’s extensibility model follows these principles:

- **Explicit, not implicit** — extensions are opt‑in and never activated automatically  
- **Deterministic** — custom components must not introduce hidden behavior  
- **Isolated** — extensions do not modify core semantics  
- **Composable** — multiple extensions can be combined  
- **Replaceable** — any runtime component can be swapped out  

The core remains pure and stable, while the runtime adapts to user needs.

---

## 10.2 Custom StepEvaluator

`StepEvaluator` controls how input and output mappings are evaluated.

Users may provide a custom evaluator to:

- support additional placeholder formats  
- integrate with expression languages  
- perform validation  
- implement domain-specific mapping rules  

Requirements:

- evaluation must be deterministic  
- evaluation must not modify the `ExecutionContext` directly  
- unresolved values should be handled explicitly  

The default implementation (`DefaultStepEvaluator`) supports only flat placeholders.

---

## 10.3 Custom StepInvoker

`StepInvoker` defines how steps are executed.

Users may replace it to:

- integrate with dependency injection frameworks  
- call remote services  
- invoke asynchronous operations  
- wrap module calls with custom logic  
- support non-reflection invocation models  

Requirements:

- must return a raw output map  
- must throw `BusinessException`, `SystemException`, or `StepExecutionException` appropriately  
- must not modify the `ExecutionContext`  

The default implementation (`DefaultStepInvoker`) uses reflection.

---

## 10.4 Custom ModuleResolver

`ModuleResolver` maps module names to module instances.

Users may replace it to:

- integrate with DI containers  
- support dynamic module loading  
- implement service discovery  
- provide custom module registries  

Requirements:

- resolution must be deterministic  
- missing modules must result in a clear error  

The default implementation (`DefaultModuleResolver`) uses a simple registry.

---

## 10.5 Custom LoggingAdapter and LogFormatter

BareFlow does not log anything by default.  
Users may provide:

### LoggingAdapter
Defines how log messages are emitted.  
Integrations may include:

- SLF4J  
- Log4j  
- System.out  
- cloud logging services  
- custom logging backends  

### LogFormatter
Controls how messages are formatted.  
Formatting is fully customizable.

Logging is optional and does not affect execution.

---

## 10.6 Custom FlowEngineEventListener

`FlowEngineEventListener` receives all execution events.

Users may implement listeners for:

- debugging  
- metrics  
- distributed tracing  
- auditing  
- monitoring dashboards  
- custom instrumentation  

Multiple listeners can be combined using `CompositeFlowEngineEventListener`.

Listeners must not modify execution behavior.

---

## 10.7 Custom YAML Parser

Although BareFlow provides `FlowYamlParser`, users may replace it to:

- support additional YAML fields  
- perform validation  
- implement schema enforcement  
- support alternative configuration formats (JSON, XML, etc.)  

Requirements:

- parser must produce valid core model objects  
- parser must not introduce implicit defaults  

---

## 10.8 Guidelines for Safe Extensions

When extending BareFlow:

- avoid global state  
- avoid non-deterministic behavior  
- avoid hidden retries or implicit error handling  
- avoid modifying the `ExecutionContext` outside the engine  
- ensure all custom components are pure and predictable  

Extensions should enhance functionality without altering core semantics.

---

BareFlow’s extension model provides flexibility without sacrificing determinism or transparency.  
Users can adapt the runtime to their environment while relying on the core engine’s stable and predictable behavior.

# 11. License

BareFlow is distributed under the **MIT License**.

```
MIT License

Copyright (c) 2024 syake-salmon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in  
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING  
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER  
DEALINGS IN THE SOFTWARE.
```

The MIT License allows unrestricted use, modification, and distribution of the software,  
as long as the copyright notice and license text are preserved.