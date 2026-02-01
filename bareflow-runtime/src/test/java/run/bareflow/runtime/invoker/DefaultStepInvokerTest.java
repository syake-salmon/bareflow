package run.bareflow.runtime.invoker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import run.bareflow.core.exception.BusinessException;
import run.bareflow.core.exception.SystemException;
import run.bareflow.runtime.resolver.ModuleResolver;

public class DefaultStepInvokerTest {
    /**
     * テスト用のモジュールクラス（正常系）
     */
    public static class TestModule {
        public Map<String, Object> op(final Map<String, Object> input) {
            return Map.of("result", 123);
        }
    }

    /**
     * BusinessException を投げるモジュール
     */
    public static class BusinessErrorModule {
        public Map<String, Object> op(final Map<String, Object> input) {
            throw new BusinessException("biz");
        }
    }

    /**
     * Map を返さないモジュール
     */
    public static class InvalidReturnModule {
        public String op(final Map<String, Object> input) {
            return "not a map";
        }
    }

    /**
     * operation メソッドが存在しないモジュール
     */
    public static class NoSuchMethodModule {
        public Map<String, Object> other(final Map<String, Object> input) {
            return Map.of();
        }
    }

    /**
     * RuntimeException を投げるモジュール
     */
    public static class RuntimeErrorModule {
        public Map<String, Object> op(final Map<String, Object> input) {
            throw new RuntimeException("boom");
        }
    }

    // ------------------------------------------------------------
    // 1. 正常系
    // ------------------------------------------------------------
    @Test
    public void testInvokeSuccess() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenAnswer(inv -> TestModule.class);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        final Map<String, Object> result = invoker.invoke("M", "op", Map.of());

        assertEquals(123, result.get("result"));
    }

    // ------------------------------------------------------------
    // 2. BusinessException はそのまま再スロー
    // ------------------------------------------------------------
    @Test
    public void testInvokeBusinessException() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenAnswer(inv -> BusinessErrorModule.class);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        assertThrows(BusinessException.class, () -> invoker.invoke("M", "op", Map.of()));
    }

    // ------------------------------------------------------------
    // 3. Map を返さない → SystemException
    // ------------------------------------------------------------
    @Test
    public void testInvalidReturnType() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenAnswer(inv -> InvalidReturnModule.class);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        assertThrows(SystemException.class, () -> invoker.invoke("M", "op", Map.of()));
    }

    // ------------------------------------------------------------
    // 4. operation メソッドが存在しない → SystemException
    // ------------------------------------------------------------
    @Test
    public void testNoSuchMethod() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenAnswer(inv -> NoSuchMethodModule.class);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        assertThrows(SystemException.class, () -> invoker.invoke("M", "op", Map.of()));
    }

    // ------------------------------------------------------------
    // 5. RuntimeException → SystemException にラップ
    // ------------------------------------------------------------
    @Test
    public void testRuntimeExceptionWrappedAsSystemException() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenAnswer(inv -> RuntimeErrorModule.class);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        final SystemException ex = assertThrows(SystemException.class, () -> invoker.invoke("M", "op", Map.of()));

        assertTrue(ex.getMessage().contains("Error during step invocation"));
    }

    // ------------------------------------------------------------
    // 6. moduleResolver が null を返す → SystemException
    // ------------------------------------------------------------
    @Test
    public void testModuleNotFound() {
        final ModuleResolver resolver = mock(ModuleResolver.class);
        when(resolver.resolve("M")).thenReturn(null);

        final DefaultStepInvoker invoker = new DefaultStepInvoker(resolver);

        assertThrows(SystemException.class, () -> invoker.invoke("M", "op", Map.of()));
    }
}