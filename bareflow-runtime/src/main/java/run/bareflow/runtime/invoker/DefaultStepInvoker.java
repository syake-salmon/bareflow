package run.bareflow.runtime.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import run.bareflow.core.engine.invoker.StepInvoker;
import run.bareflow.core.exception.BusinessException;
import run.bareflow.core.exception.SystemException;
import run.bareflow.runtime.resolver.ModuleResolver;

/**
 * Default StepInvoker implementation using a ModuleResolver.
 *
 * Responsibilities:
 * - Resolve a module class using ModuleResolver
 * - Instantiate the module via its no-arg constructor
 * - Locate the operation method with signature: Map<String,Object> →
 * Map<String,Object>
 * - Invoke the method and return its result
 *
 * Error handling:
 * - BusinessException thrown by the target method is propagated as-is
 * - Any other exception is wrapped in SystemException
 *
 * This class performs no caching, validation, or lifecycle management.
 * Higher-level runtime layers may extend or wrap this behavior.
 */
public class DefaultStepInvoker implements StepInvoker {
    private final ModuleResolver moduleResolver;

    public DefaultStepInvoker(ModuleResolver moduleResolver) {
        this.moduleResolver = moduleResolver;
    }

    @Override
    public Map<String, Object> invoke(
            String module,
            String operation,
            Map<String, Object> input) {

        try {
            // 1. Resolve module class
            Class<?> clazz = moduleResolver.resolve(module);

            // 2. Instantiate module
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 3. Resolve operation method
            Method method = resolveMethod(clazz, operation);

            // 4. Invoke method
            Object result = method.invoke(instance, input);

            // 5. Validate return type
            if (!(result instanceof Map)) {
                throw new SystemException(
                        "StepInvoker: method must return Map<String,Object>. " +
                                "module=" + module + ", operation=" + operation);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> output = (Map<String, Object>) result;
            return output;

        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();

            // BusinessException is propagated as-is
            if (target instanceof BusinessException) {
                throw (BusinessException) target;
            }

            // All other exceptions are system-level
            throw new SystemException("Error during step invocation", target);

        } catch (Exception e) {
            throw new SystemException("Failed to invoke step", e);
        }
    }

    private Method resolveMethod(Class<?> clazz, String operation) {
        try {
            return clazz.getMethod(operation, Map.class);
        } catch (NoSuchMethodException e) {
            throw new SystemException(
                    "Operation method not found: " +
                            clazz.getName() + "#" + operation + "(Map)",
                    e);
        }
    }
}