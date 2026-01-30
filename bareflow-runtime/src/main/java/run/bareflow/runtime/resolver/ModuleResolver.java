package run.bareflow.runtime.resolver;

/**
 * Resolves a logical module name to a Java Class.
 *
 * ModuleResolver abstracts how BareFlow locates and loads module classes.
 * Implementations may use any strategy, such as:
 * - classpath scanning
 * - filesystem-based loading
 * - dependency injection containers
 * - custom application registries
 *
 * Responsibilities:
 * - Map a logical module name (from YAML) to a concrete Class
 * - Throw an exception if the module cannot be resolved
 *
 * Requirements for resolved classes:
 * - Must be instantiable via a public no-arg constructor
 * - Must contain operation methods with signature:
 * Map<String,Object> → Map<String,Object>
 * (validated by StepInvoker, not by this resolver)
 *
 * ModuleResolver does not perform caching, lifecycle management,
 * or method resolution. These concerns belong to higher-level runtime layers.
 */
public interface ModuleResolver {
    /**
     * Resolve a module name to a Java Class.
     *
     * @param moduleName logical module name from YAML
     * @return resolved Class
     * @throws RuntimeException if the module cannot be found or loaded
     */
    Class<?> resolve(String moduleName);
}