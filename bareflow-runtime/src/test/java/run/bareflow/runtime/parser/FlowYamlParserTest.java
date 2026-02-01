package run.bareflow.runtime.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import run.bareflow.core.definition.FlowDefinition;
import run.bareflow.core.definition.OnErrorDefinition;
import run.bareflow.core.definition.RetryPolicy;
import run.bareflow.core.definition.StepDefinition;
import run.bareflow.core.exception.SystemException;

public class FlowYamlParserTest {
  private InputStream yaml(final String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  // ------------------------------------------------------------
  // 1. 最小構成の YAML が正しくパースされる
  // ------------------------------------------------------------
  @Test
  public void testParseMinimalYaml() {
    final String yaml = """
        name: testFlow
        steps:
          - name: s1
            module: M
            operation: op
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    assertEquals("testFlow", def.getName());
    assertEquals(1, def.getSteps().size());

    final StepDefinition s1 = def.getSteps().get(0);
    assertEquals("s1", s1.getName());
    assertEquals("M", s1.getModule());
    assertEquals("op", s1.getOperation());
    assertTrue(s1.getInput().isEmpty());
    assertTrue(s1.getOutput().isEmpty());
    assertNull(s1.getRetryPolicy());
    assertNull(s1.getOnError());
  }

  // ------------------------------------------------------------
  // 2. metadata が正しく読み込まれる
  // ------------------------------------------------------------
  @Test
  public void testParseMetadata() {
    final String yaml = """
        name: flow
        metadata:
          version: "1.0"
          author: "Keisuke"
        steps:
          - name: s1
            module: M
            operation: op
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    assertEquals("1.0", def.getMetadata().get("version"));
    assertEquals("Keisuke", def.getMetadata().get("author"));
  }

  // ------------------------------------------------------------
  // 3. retryPolicy が正しく読み込まれる
  // ------------------------------------------------------------
  @Test
  public void testParseRetryPolicy() {
    final String yaml = """
        name: flow
        steps:
          - name: s1
            module: M
            operation: op
            retry:
              maxAttempts: 3
              delayMillis: 500
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    final RetryPolicy retry = def.getSteps().get(0).getRetryPolicy();
    assertNotNull(retry);

    assertEquals(3, retry.getMaxAttempts());
    assertEquals(500L, retry.getDelayMillis());
  }

  // ------------------------------------------------------------
  // 4. retry ブロックが無い場合 → null
  // ------------------------------------------------------------
  @Test
  public void testRetryPolicyAbsent() {
    final String yaml = """
        name: flow
        steps:
          - name: s1
            module: M
            operation: op
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    assertNull(def.getSteps().get(0).getRetryPolicy());
  }

  // ------------------------------------------------------------
  // 5. onError が正しく読み込まれる
  // ------------------------------------------------------------
  @Test
  public void testParseOnError() {
    final String yaml = """
        name: flow
        steps:
          - name: s1
            module: M
            operation: op
            onError:
              action: CONTINUE
              delayMillis: 150
              output:
                msg: "error"
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    final OnErrorDefinition onError = def.getSteps().get(0).getOnError();
    assertNotNull(onError);
    assertEquals(OnErrorDefinition.Action.CONTINUE, onError.getAction());
    assertEquals(150, onError.getDelayMillis());
    assertEquals("error", onError.getOutput().get("msg"));
  }

  // ------------------------------------------------------------
  // 6. flow-level onError が読み込まれる
  // ------------------------------------------------------------
  @Test
  public void testParseFlowLevelOnError() {
    final String yaml = """
        name: flow
        onError:
          action: STOP
        steps:
          - name: s1
            module: M
            operation: op
        """;

    final FlowYamlParser parser = new FlowYamlParser();
    final FlowDefinition def = parser.parse(yaml(yaml));

    assertNotNull(def.getOnError());
    assertEquals(OnErrorDefinition.Action.STOP, def.getOnError().getAction());
  }

  // ------------------------------------------------------------
  // 7. 無効 YAML（name が無い）
  // ------------------------------------------------------------
  @Test
  public void testInvalidYamlMissingName() {
    final String yaml = """
        steps:
          - name: s1
            module: M
            operation: op
        """;

    final FlowYamlParser parser = new FlowYamlParser();

    assertThrows(SystemException.class, () -> parser.parse(yaml(yaml)));
  }

  // ------------------------------------------------------------
  // 8. 無効 YAML（steps が無い）
  // ------------------------------------------------------------
  @Test
  public void testInvalidYamlMissingSteps() {
    final String yaml = """
        name: flow
        """;

    final FlowYamlParser parser = new FlowYamlParser();

    assertThrows(SystemException.class, () -> parser.parse(yaml(yaml)));
  }

  // ------------------------------------------------------------
  // 9. 無効 YAML（step の必須項目不足）
  // ------------------------------------------------------------
  @Test
  public void testInvalidStepMissingFields() {
    final String yaml = """
        name: flow
        steps:
          - name: s1
            module: M
            # operation missing
        """;

    final FlowYamlParser parser = new FlowYamlParser();

    assertThrows(SystemException.class, () -> parser.parse(yaml(yaml)));
  }

  // ------------------------------------------------------------
  // 10. onError.action が不正
  // ------------------------------------------------------------
  @Test
  public void testInvalidOnErrorAction() {
    final String yaml = """
        name: flow
        steps:
          - name: s1
            module: M
            operation: op
            onError:
              action: INVALID
        """;

    final FlowYamlParser parser = new FlowYamlParser();

    assertThrows(SystemException.class, () -> parser.parse(yaml(yaml)));
  }
}