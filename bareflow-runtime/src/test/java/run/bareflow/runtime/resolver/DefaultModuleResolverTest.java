package run.bareflow.runtime.resolver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import run.bareflow.core.exception.SystemException;

public class DefaultModuleResolverTest {
    // ------------------------------------------------------------
    // テスト用のダミーモジュールクラス
    // ------------------------------------------------------------
    public static class TestModule {
        public TestModule() {
        } // public no-arg constructor
    }

    // ------------------------------------------------------------
    // 1. 正常系：クラスが正しく解決される
    // ------------------------------------------------------------
    @Test
    public void testResolveValidClass() {
        String basePackage = this.getClass().getPackageName(); // run.bareflow.runtime.resolver
        DefaultModuleResolver resolver = new DefaultModuleResolver(basePackage);

        Class<?> clazz = resolver.resolve("DefaultModuleResolverTest$TestModule");

        assertNotNull(clazz);
        assertEquals(TestModule.class, clazz);
    }

    // ------------------------------------------------------------
    // 2. 異常系：クラスが見つからない場合 SystemException がスローされる
    // ------------------------------------------------------------
    @Test
    public void testResolveUnknownClassThrows() {
        String basePackage = "run.bareflow.runtime.resolver";
        DefaultModuleResolver resolver = new DefaultModuleResolver(basePackage);

        SystemException ex = assertThrows(SystemException.class,
                () -> resolver.resolve("NoSuchModule"));

        assertTrue(ex.getMessage().contains("Module class not found"));
        assertTrue(ex.getMessage().contains("run.bareflow.runtime.resolver.NoSuchModule"));
    }
}