package run.bareflow.runtime.resolver;

import run.bareflow.core.exception.SystemException;

/**
 * Default implementation of ModuleResolver.
 *
 * Resolves module names by concatenating a base package with the logical
 * module name from the flow definition. For example:
 *
 * basePackage = "com.example.modules"
 * moduleName = "UserModule"
 * → resolves to class "com.example.modules.UserModule"
 *
 * Responsibilities:
 * - Construct the fully qualified class name
 * - Load the class via Class.forName
 * - Throw SystemException if the class cannot be found
 *
 * Requirements for resolved classes:
 * - Must be instantiable via a public no-arg constructor
 * - Must contain operation methods with signature:
 * Map<String,Object> → Map<String,Object>
 * (validated by DefaultStepInvoker)
 *
 * This resolver performs no caching, scanning, or DI integration.
 * Higher-level runtime layers may wrap or replace this implementation.
 */
public class DefaultModuleResolver implements ModuleResolver {
    private final String basePackage;

    /**
     * @param basePackage base package where module classes are located
     */
    public DefaultModuleResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public Class<?> resolve(String moduleName) {
        String className = basePackage + "." + moduleName;

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SystemException("Module class not found: " + className, e);
        }
    }
}