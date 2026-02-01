package run.bareflow.runtime.resolver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ModuleResolver.
 *
 * Since ModuleResolver is an interface, we test it using a simple
 * fake implementation that mimics expected behavior.
 */
public class ModuleResolverTest {
    /**
     * A simple fake resolver for testing.
     * - "valid" → returns ValidModule.class
     * - anything else → throws RuntimeException
     */
    static class FakeModuleResolver implements ModuleResolver {
        @Override
        public Class<?> resolve(String moduleName) {
            if ("valid".equals(moduleName)) {
                return ValidModule.class;
            }
            throw new RuntimeException("Module not found: " + moduleName);
        }
    }

    /** Dummy module class for testing. */
    public static class ValidModule {
        public ValidModule() {
        } // public no-arg constructor
    }

    // ------------------------------------------------------------
    // 1. 正常系：既知のモジュール名を解決できる
    // ------------------------------------------------------------
    @Test
    public void testResolveValidModule() {
        ModuleResolver resolver = new FakeModuleResolver();

        Class<?> clazz = resolver.resolve("valid");

        assertNotNull(clazz);
        assertEquals(ValidModule.class, clazz);
    }

    // ------------------------------------------------------------
    // 2. 異常系：未知のモジュール名は例外を投げる
    // ------------------------------------------------------------
    @Test
    public void testResolveUnknownModuleThrows() {
        ModuleResolver resolver = new FakeModuleResolver();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> resolver.resolve("unknown"));

        assertTrue(ex.getMessage().contains("Module not found"));
    }
}