package io.github.ss.bareflow.core.definition;

/**
 * Resolves a FlowDefinition by its logical name.
 *
 * Implementations may load the definition from any source:
 * - filesystem
 * - classpath
 * - database
 * - remote HTTP endpoint
 * - in-memory registry
 *
 * This interface abstracts away the source of the flow definition,
 * allowing FlowExecutor to remain independent of storage concerns.
 */
public interface FlowDefinitionResolver {
    /**
     * Resolve a FlowDefinition for the given flow name.
     *
     * @param flowName logical name of the flow
     * @return parsed FlowDefinition
     * @throws RuntimeException if the flow cannot be found or parsed
     */
    FlowDefinition resolve(String flowName);
}
