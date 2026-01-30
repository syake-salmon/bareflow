package run.bareflow.core.resolver;

import run.bareflow.core.definition.FlowDefinition;

/**
 * Resolves a FlowDefinition by its logical name.
 *
 * This interface abstracts the source and loading mechanism of flow
 * definitions.
 * Implementations may retrieve and parse definitions from any location, such
 * as:
 * - filesystem
 * - classpath resources
 * - databases
 * - remote HTTP endpoints
 * - in-memory registries
 *
 * FlowDefinitionResolver is the boundary between BareFlow's pure execution
 * model
 * and external storage concerns. FlowExecutor relies on this interface to
 * obtain
 * a fully parsed FlowDefinition before delegating to FlowEngine.
 *
 * Implementations must return a valid FlowDefinition or throw an exception.
 */
public interface FlowDefinitionResolver {
    /**
     * Resolve a FlowDefinition for the given logical flow name.
     *
     * @param flowName logical name of the flow
     * @return a fully parsed FlowDefinition
     * @throws RuntimeException if the flow cannot be found, loaded, or parsed
     */
    FlowDefinition resolve(String flowName);
}