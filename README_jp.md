# BareFlow — Minimal Core. Maximum Clarity.

[English](README.md) | [日本語](README_jp.md)

# Table of Contents

1. [Introduction](#1-introduction)
2. [Core Principles](#2-core-principlesコア原則)
3. [Architecture Overview](#3-architecture-overviewアーキテクチャ概要)
4. [Core Concepts](#4-core-conceptsコアコンセプト)
5. [Execution Model](#5-execution-model実行モデル)
6. [Runtime Components](#6-runtime-componentsランタイムコンポーネント)
7. [YAML Flow Definitions](#7-yaml-flow-definitionsyamlフロー定義)
8. [Error Handling](#8-error-handlingエラーハンドリング)
9. [Tracing & Observability](#9-tracing--observabilityトレーシングと可観測性)
10. [Extending BareFlow](#10-extending-bareflowbareflowの拡張)
11. [License](#11-licenseライセンス)

# 1. Introduction

BareFlow は **ミニマルで決定論的なフロー実行エンジン**です。  
隠れた挙動、複雑な式言語、フレームワーク特有の“魔法”を排除し、  
フローを「書いたとおりに」「予測可能に」実行することに特化しています。

BareFlow が重視する価値は次のとおりです：

- **Minimalism（最小主義）**  
  必要最小限の概念だけを提供し、余計な抽象化や暗黙のルールを持ちません。

- **Determinism（決定論）**  
  同じ入力は常に同じ実行結果を生みます。  
  グローバル状態や非決定的な評価は排除されています。

- **Transparency（透明性）**  
  各ステップの入力・出力・リトライ・エラー・タイムスタンプが  
  `StepTrace` とイベントとして完全に記録されます。

- **No Hidden Magic（隠れた魔法なし）**  
  暗黙のデフォルト、推測的な動作、反射ベースの自動推論などは行いません。

- **Strict Separation of Core and Runtime（コアとランタイムの厳密な分離）**  
  コアは純粋なモデルと実行ルールのみを提供し、  
  YAML パース・モジュール解決・ロギングなどは runtime に委ねられます。

BareFlow は次のようなシステムに適しています：

- ステップ単位の明確で予測可能な実行が求められる場面  
- エラー処理の意味論を厳密に制御したい場面  
- モジュール呼び出しをシンプルに保ちたいアプリケーション  
- 実行の完全なトレースが必要な監査・検証用途  
- テスト容易性とデバッグ容易性を重視するプロジェクト  
- コアの安定性を保ちながら、ランタイムを自由に差し替えたい環境

BareFlow の結果は、**理解しやすく、テストしやすく、拡張しやすい**フロー実行モデルです。

# 2. Core Principles（コア原則）

BareFlow は、フロー実行を「予測可能」「透明」「シンプル」に保つため、  
少数の明確な原則に基づいて設計されています。  
これらの原則は、フローの規模や複雑さに関わらず、一貫した動作を保証します。

---

## 2.1 Minimalism（最小主義）

BareFlow は、フロー実行に本当に必要な概念だけを採用します。  
暗黙のデフォルト、複雑な抽象化、隠れた振る舞いは排除され、  
すべてのモデルとコンポーネントは「正しさのために必要だから存在する」ものだけです。

---

## 2.2 Determinism（決定論）

同じ入力は常に同じ実行結果を生みます。  
BareFlow は以下を持ちません：

- グローバル状態  
- 非決定的な評価ルール  
- タイミング依存の副作用  

これにより、フローはテストしやすく、デバッグしやすく、信頼性が高くなります。

---

## 2.3 Transparency（透明性）

BareFlow は実行のすべてを観測可能にします。  
各ステップについて、以下が完全に記録されます：

- 評価済み入力  
- 生の出力  
- マッピング後の出力  
- リトライ履歴  
- エラー情報  
- タイムスタンプ  

隠された処理は一切ありません。

---

## 2.4 No Hidden Magic（隠れた魔法なし）

BareFlow は、動作を曖昧にする動的・自動的な仕組みを避けます。  
具体的には：

- 複雑な式言語は採用しない  
- ネストしたプレースホルダはサポートしない  
- 自動バリデーションや暗黙の変換は行わない  
- 反射ベースの推測や自動解決は行わない  
- 暗黙のリトライやエラーハンドリングは存在しない  

ユーザーは常に「エンジンが何をするか」を正確に把握できます。

---

## 2.5 Strict Separation of Core and Runtime（コアとランタイムの厳密な分離）

BareFlow は 2 つのモジュールに分かれています：

### **Core（bareflow-core）**
- 不変モデル  
- 決定論的な実行ルール  
- プレースホルダ評価ルール  
- イベントモデル  
- トレースモデル  
- 例外モデル  

**I/O・ロギング・YAML パース・モジュール解決は一切含まない。**

### **Runtime（bareflow-runtime）**
- モジュール解決  
- ステップ呼び出し  
- プレースホルダ評価の実装  
- YAML パース  
- ロギング統合  
- イベントリスナーのファンアウト  

Runtime は差し替え可能で、Core の意味論を汚さない。

---

## 2.6 Composability and Extensibility（構成性と拡張性）

BareFlow はミニマルでありながら、必要に応じて拡張できます。  
ユーザーは以下を自由に差し替え可能です：

- StepEvaluator  
- StepInvoker  
- ModuleResolver  
- LoggingAdapter  
- FlowEngineEventListener  

拡張は明示的であり、暗黙の魔法はありません。

---

これらの原則が BareFlow の一貫性と信頼性を支えています。

# 3. Architecture Overview（アーキテクチャ概要）

BareFlow は **Core（純粋な実行モデル）** と **Runtime（実行環境・統合レイヤ）** の  
2 つの明確に分離されたモジュールで構成されています。

この分離により、実行意味論は常に安定し、  
YAML パース・モジュール解決・ロギングなどの周辺機能は  
自由に差し替え可能な形で提供されます。

---

## 3.1 Module Structure（モジュール構造）

BareFlow のプロジェクトは次の 2 モジュールで構成されます。

---

### **Core Module（`bareflow-core`）**

Core は BareFlow の「純粋な心臓部」です。  
以下を提供します：

- **不変のフローモデル**  
  - FlowDefinition  
  - StepDefinition  
  - RetryPolicy  
  - OnErrorDefinition  

- **決定論的な実行エンジン**  
  - FlowEngine  
  - StepEvaluator  
  - StepInvoker（インターフェース）  
  - エラー分類（Business / System / StepExecution）  

- **イベントモデル**  
  - FlowEngineEvent  
  - FlowEngineEventListener  

- **トレースモデル**  
  - StepTrace  
  - StepTraceEntry  

Core は以下を一切含みません：

- I/O  
- ロギング  
- YAML パース  
- モジュール解決  
- 外部依存  
- 反射ベースの自動処理  

**完全に純粋で、決定論的なロジックのみが存在します。**

---

### **Runtime Module（`bareflow-runtime`）**

Runtime は Core の意味論を汚さずに、  
実際のアプリケーション環境で BareFlow を動かすための機能を提供します。

含まれるもの：

- **FlowExecutor**  
  - FlowDefinitionResolver でフローを取得  
  - ExecutionContext を初期化  
  - FlowEngine を構築して実行  

- **YAML パーサ（FlowYamlParser）**  
  - SnakeYAML Engine v2 を使用  
  - YAML → FlowDefinition に直接マッピング  
  - 暗黙のデフォルトなし  

- **StepInvoker のデフォルト実装（DefaultStepInvoker）**  
  - ModuleResolver でクラスを解決  
  - リフレクションで `Map → Map` メソッドを呼び出す  

- **ModuleResolver**  
  - モジュール名 → Java クラスの解決  

- **LoggingAdapter / LogFormatter**  
  - SLF4J ベースのログ統合  

- **CompositeFlowEngineEventListener**  
  - 複数リスナーへのイベント fan-out  

Runtime は差し替え可能で、  
Core の決定論的な意味論を一切変更しません。

---

## 3.2 Flow Lifecycle Overview（フロー実行ライフサイクル）

BareFlow のフロー実行は、以下のフェーズを順番に進みます。

1. **Input Evaluation**  
   - StepEvaluator が input mapping を評価  
   - `${name}` プレースホルダを ExecutionContext から解決  

2. **Invocation**  
   - StepInvoker がモジュールの operation を呼び出す  
   - 結果は rawOutput として返される  

3. **Output Evaluation**  
   - output mapping を評価  
   - プレースホルダは rawOutput → ExecutionContext の順で解決  

4. **Context Merge**  
   - mapped output を ExecutionContext にマージ  

5. **Retry Handling**  
   - SystemException / StepExecutionException → RetryPolicy  
   - BusinessException → RetryPolicy の対象外  

6. **OnError Handling**  
   - RetryPolicy が尽きた後に適用  
   - STOP / CONTINUE / RETRY（1回だけ）  

7. **Event Emission**  
   - 各フェーズで FlowEngineEvent を発行  

8. **Trace Recording**  
   - StepTraceEntry を作成し StepTrace に追加  

このライフサイクルは **すべてのステップで完全に同一**です。

---

## 3.3 Event Model（イベントモデル）

BareFlow は実行のあらゆる瞬間をイベントとして発行します。

- FlowStart / FlowEnd  
- StepStart / StepEnd  
- InputEvaluationStart / End  
- InvokeStart / End  
- OutputEvaluationStart / End  
- RetryPolicyRetry  
- OnErrorRetry  
- BusinessError  
- SystemError  
- StepExecutionError  
- UnhandledError  

イベントはすべて **immutable record** であり、  
FlowEngineEventListener によって観測されます。

イベントは **実行に影響を与えません**。

---

## 3.4 Trace Model（トレースモデル）

BareFlow は実行の完全な履歴を保持します。

### StepTraceEntry（1試行の完全記録）
- beforeContext  
- evaluatedInput  
- rawOutput  
- mappedOutput  
- error  
- timestamps  
- attempt  

### StepTrace（全試行の集合）
- isAllSuccessful()  
- isFinallySuccessful()  
- wasRetried()  
- getTotalAttempts()  

トレースは **監査レベルの完全性**を持ち、  
ログ・デバッグ・検証に利用できます。

---

## 3.5 Separation of Responsibilities（責務の分離）

| Concern | Core | Runtime |
|------------------|------|---------|
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

この分離により：

- **Core は永続的に安定**  
- **Runtime は自由に差し替え可能**  

という理想的な構造が実現されています。

# 4. Core Concepts（コアコンセプト）

BareFlow は、フロー定義・実行・エラー処理・トレースを構成する  
少数の明確で予測可能な概念によって成り立っています。  
これらの概念は、BareFlow の決定論的な実行モデルの基盤となります。

---

## 4.1 FlowDefinition

`FlowDefinition` はフロー全体を表す **不変の構造モデル**です。

含まれるもの：

- **name** — フローの論理名  
- **steps** — `StepDefinition` の順序付きリスト  
- **onError** — フロー全体のデフォルトエラーハンドリング（任意）  
- **metadata** — 任意のメタデータ（実行には影響しない）

特徴：

- ロジックは一切持たない  
- バリデーションやデフォルト補完は runtime 側の責務  
- FlowEngine は FlowDefinition をそのまま逐次実行する

---

## 4.2 StepDefinition

`StepDefinition` はフロー内の 1 ステップを表す構造モデルです。

含まれるもの：

- **name** — ステップ名  
- **module** — 呼び出すモジュール名  
- **operation** — モジュール内の操作名  
- **input** — 生の入力マッピング（実行前に評価される）  
- **output** — 生の出力マッピング（実行後に評価される）  
- **retryPolicy** — システムレベルのリトライ設定（任意）  
- **onError** — ステップ固有のエラーハンドリング（任意）

特徴：

- 完全に構造的  
- 評価は StepEvaluator が担当  
- step-level の onError は flow-level を上書きする

---

## 4.3 RetryPolicy

`RetryPolicy` は **システムレベルのリトライ**を定義します。

対象となる例外：

- `SystemException`  
- `StepExecutionException`

対象外：

- `BusinessException`

フィールド：

- **maxAttempts** — 初回を含む総試行回数  
- **delayMillis** — リトライ間の待機時間（ミリ秒）

意味論：

- attempts は 1 から開始  
- `attempt < maxAttempts` の間リトライ  
- BusinessException は絶対にリトライされない

---

## 4.4 OnErrorDefinition

`OnErrorDefinition` は **ビジネスレベルのエラー処理**を定義します。

アクション：

- **STOP** — エラーを伝播してフローを停止  
- **CONTINUE** — エラーを無視して次のステップへ  
- **RETRY** — **1回だけ**ビジネスレベルのリトライを行う  

オプション：

- **output** — エラー時のみ評価される出力マッピング  
- **delayMillis** — RETRY の前に待機する時間

特徴：

- RetryPolicy が尽きた後に適用  
- step-level が flow-level を上書き  
- RETRY は exactly once（FlowEngine 内で厳密に制御）

---

## 4.5 ExecutionContext

`ExecutionContext` はフロー全体で共有される **フラットな key-value ストア**です。

特徴：

- 階層構造なし  
- 値は任意のオブジェクト  
- merge() は既存キーを上書き  
- snapshot() は防御的コピー（トレース用）  
- view() は live view（不変）

ExecutionContext は BareFlow のデータフローの中心です。

---

## 4.6 StepTrace と StepTraceEntry

BareFlow は実行の完全な履歴を保持します。

### StepTraceEntry（1試行の記録）

含まれるもの：

- beforeContext  
- evaluatedInput  
- rawOutput  
- mappedOutput  
- error  
- startTime / endTime  
- attempt（1 = 初回、2+ = リトライ）

特徴：

- 完全なスナップショット  
- 不変  
- 成功/失敗を明確に判定可能

### StepTrace（全試行の集合）

提供される派生情報：

- **isAllSuccessful()** — フローがリトライなく成功したか  
- **isFinallySuccessful()** — フローが成功したか  
- **wasRetried()** — リトライが発生したか  
- **getTotalAttempts()** — 総試行回数  

StepTrace は監査・デバッグ・可観測性の中心となる。

---

これらのコアコンセプトが、BareFlow の決定論的で透明な実行モデルを支えています。

# 5. Execution Model（実行モデル）

BareFlow の実行モデルは **決定論的・透明・一貫性のある**動作を保証するために設計されています。  
すべてのステップは同じライフサイクルに従い、評価・呼び出し・リトライ・エラー処理・トレースが  
明確に定義された順序で行われます。

---

## 5.1 Step Lifecycle（ステップのライフサイクル）

各ステップは以下のフェーズを順番に実行します。

1. **Input Evaluation（入力評価）**  
   - StepEvaluator が input mapping を評価  
   - `${name}` プレースホルダを ExecutionContext から解決  

2. **Invocation（呼び出し）**  
   - StepInvoker がモジュールの operation を呼び出す  
   - 結果は rawOutput として返される  

3. **Output Evaluation（出力評価）**  
   - output mapping を評価  
   - プレースホルダは rawOutput → ExecutionContext の順で解決  

4. **Context Merge（コンテキスト統合）**  
   - mapped output を ExecutionContext にマージ  
   - 既存キーは上書きされる  

5. **Retry Handling（リトライ処理）**  
   - SystemException / StepExecutionException → RetryPolicy に従ってリトライ  
   - BusinessException → RetryPolicy の対象外  

6. **OnError Handling（エラー処理）**  
   - RetryPolicy が尽きた後に適用  
   - STOP / CONTINUE / RETRY（1回だけ）  

7. **Event Emission（イベント発行）**  
   - 各フェーズで FlowEngineEvent を発行  

8. **Trace Recording（トレース記録）**  
   - StepTraceEntry を作成し StepTrace に追加  

このライフサイクルは **すべてのステップで完全に同一**であり、  
隠れたショートカットや暗黙の処理は存在しません。

---

## 5.2 Input Evaluation（入力評価）

入力評価は StepEvaluator によって行われます。

ルール：

- `${name}` のような **フラットなプレースホルダのみ**サポート  
- ネスト `${a.b}` は非サポート（未解決なら null）  
- リテラル値はそのまま返される  
- 未解決プレースホルダは null  
- 評価は ExecutionContext の値に基づく  

入力評価は純粋で副作用がなく、決定論的です。

---

## 5.3 Invocation（呼び出し）

StepInvoker がモジュールの operation を呼び出します。

責務：

- モジュール名からクラスを解決（ModuleResolver）  
- `Map<String,Object> → Map<String,Object>` のメソッドを呼び出す  
- BusinessException はそのまま伝播  
- その他の例外は SystemException にラップ  

呼び出しは純粋な関数呼び出しとして扱われ、  
ExecutionContext を直接変更することはありません。

---

## 5.4 Output Evaluation（出力評価）

出力評価も StepEvaluator によって行われます。

解決順序：

1. rawOutput  
2. ExecutionContext  
3. 未解決 → null  

特徴：

- output mapping に定義されたキーのみが結果に含まれる  
- リテラル値はそのまま返される  
- rawOutput と ExecutionContext の両方を参照可能  

---

## 5.5 Retry Semantics（リトライ意味論）

RetryPolicy は **システムレベルの失敗**にのみ適用されます。

対象：

- SystemException  
- StepExecutionException  

対象外：

- BusinessException（ビジネスエラーはリトライしない）

ルール：

- attempts は 1 から開始  
- `attempt < maxAttempts` の間リトライ  
- delayMillis だけ待機して再試行  
- RetryPolicy は onError より先に評価される  

RetryPolicy は **決定論的で、暗黙のリトライは存在しない**。

---

## 5.6 OnError Semantics（エラー処理の意味論）

RetryPolicy が尽きた後、OnErrorDefinition が適用されます。

アクション：

- **STOP**  
  - エラーを伝播し、フローを停止  
- **CONTINUE**  
  - エラーを無視し、次のステップへ  
  - output mapping があれば評価して ExecutionContext にマージ  
- **RETRY**  
  - **1回だけ**ビジネスレベルのリトライ  
  - delayMillis 待機後に再試行  

特徴：

- step-level が flow-level を上書き  
- RETRY は FlowEngine 内で厳密に「1回だけ」に制御される  

---

## 5.7 Event Emission（イベント発行）

BareFlow は実行のあらゆる瞬間をイベントとして発行します。

例：

- FlowStartEvent / FlowEndEvent  
- StepStartEvent / StepEndEvent  
- InputEvaluationStart/End  
- InvokeStart/End  
- OutputEvaluationStart/End  
- RetryPolicyRetryEvent  
- OnErrorRetryEvent  
- BusinessErrorEvent  
- SystemErrorEvent  
- StepExecutionErrorEvent  

イベントは **観測専用**であり、実行には影響しません。

---

## 5.8 Trace Recording（トレース記録）

各ステップの試行は StepTraceEntry として記録されます。

記録される内容：

- evaluatedInput  
- rawOutput  
- mappedOutput  
- error  
- beforeContext  
- timestamps  
- attempt  

StepTrace はこれらを集約し、以下の派生情報を提供します：

- isAllSuccessful()  
- isFinallySuccessful()  
- wasRetried()  
- getTotalAttempts()  

トレースは **監査・デバッグ・可観測性**の中心となります。

---

BareFlow の実行モデルは、  
「書いたとおりに」「予測可能に」「透明に」動作することを保証するために  
厳密に定義されています。

# 6. Runtime Components（ランタイムコンポーネント）

BareFlow の runtime は、コアの決定論的な実行モデルを汚すことなく、  
実際のアプリケーション環境でフローを動かすための **プラガブルな実装群**を提供します。

Core が「純粋な意味論」を定義するのに対し、  
Runtime は「実際にどう動かすか」を担当します。

Runtime のすべてのコンポーネントは **差し替え可能**であり、  
Core の安定性を損なうことなく拡張できます。

---

## 6.1 FlowExecutor

`FlowExecutor` は BareFlow の高レベル実行エントリポイントです。

責務：

1. **FlowDefinitionResolver** を使ってフロー定義を読み込む  
2. 初期入力から **ExecutionContext** を構築  
3. **FlowEngine** を生成  
4. フローを実行し、**FlowResult** を返す  

特徴：

- ロジックは最小限  
- ロギング・メトリクス・検証などは行わない  
- 実行の本体は FlowEngine に委譲される  

---

## 6.2 DefaultStepInvoker

`DefaultStepInvoker` は StepInvoker のデフォルト実装です。

責務：

- **ModuleResolver** でモジュールクラスを解決  
- 反射で `Map<String,Object> → Map<String,Object>` のメソッドを呼び出す  
- 戻り値が Map であることを保証  

例外処理：

- `BusinessException` はそのまま再スロー  
- その他の例外は `SystemException` にラップ  

特徴：

- キャッシュなし  
- ライフサイクル管理なし  
- シンプルで予測可能な反射呼び出し  

---

## 6.3 DefaultModuleResolver

`DefaultModuleResolver` はモジュール名を Java クラスに解決します。

動作：

- basePackage + moduleName で完全修飾名を構築  
- `Class.forName` でロード  
- 見つからなければ `SystemException`  

特徴：

- DI やスキャンは行わない  
- 非常にシンプルで明示的  

---

## 6.4 DefaultStepEvaluator

`DefaultStepEvaluator` は BareFlow のプレースホルダ評価ルールを実装します。

特徴：

- `${name}` のみサポート  
- ネスト `${a.b}` は非サポート  
- input は ExecutionContext から解決  
- output は rawOutput → ExecutionContext の順で解決  
- 未解決は null  

BareFlow の「透明で決定論的な評価モデル」を実現する中心的コンポーネントです。

---

## 6.5 FlowYamlParser

`FlowYamlParser` は YAML を FlowDefinition に変換します。

特徴：

- SnakeYAML Engine v2 を使用  
- YAML → FlowDefinition に **構造的に直接マッピング**  
- 暗黙のデフォルトなし  
- バリデーションは最小限（必須項目のみ）  
- 変換・補完・推論は行わない  

BareFlow の YAML DSL は **ミニマルで透明**であることを保証します。

---

## 6.6 LoggingAdapter と LogFormatter

BareFlow はデフォルトではログを出力しません。  
ログ出力は runtime の責務です。

### LoggingAdapter
- SLF4J の Logger にイベントを出力  
- ログレベルに応じて LogFormatter を呼び出す  

### LogFormatter
- イベントを文字列に整形  
- JSON / key-value / カスタム形式など自由に実装可能  

特徴：

- ログ構造は完全にユーザーが決められる  
- Core はログ依存を持たない  

---

## 6.7 CompositeFlowEngineEventListener

複数の FlowEngineEventListener をまとめて扱うためのコンポーネント。

特徴：

- 受け取ったイベントをすべてのリスナーに fan-out  
- フィルタリング・変換は行わない  
- ロギング + メトリクス + デバッグなどを同時に実現可能  

---

## 6.8 Replaceability and Extensibility（置換性と拡張性）

Runtime のすべてのコンポーネントは差し替え可能です。

| Component | Interface | Default Implementation |
|----------|-----------|------------------------|
| Step invocation | StepInvoker | DefaultStepInvoker |
| Module resolution | ModuleResolver | DefaultModuleResolver |
| Placeholder evaluation | StepEvaluator | DefaultStepEvaluator |
| YAML parsing | — | FlowYamlParser |
| Logging | LoggingAdapter | user-provided |
| Event handling | FlowEngineEventListener | user-provided / composite |

これにより、BareFlow は以下を実現します：

- コアの意味論を変えずに拡張可能  
- DI フレームワークとの統合  
- カスタムロギング・メトリクス・監査  
- 独自のモジュール呼び出し戦略  

---

Runtime は「実行環境の柔軟性」を提供し、  
Core は「実行意味論の安定性」を保証します。

# 7. YAML Flow Definitions（YAMLフロー定義）

BareFlow は、フロー定義のために **最小限で透明な YAML DSL** を提供します。  
この DSL は、BareFlow のコアモデル（FlowDefinition / StepDefinition / RetryPolicy / OnErrorDefinition）に  
**直接マッピングされる構造的な表現**であり、暗黙の変換や推論は一切行われません。

---

## 7.1 Design Philosophy（設計思想）

BareFlow の YAML DSL は次の原則に基づいています：

- **Minimal（最小限）**  
  必要なフィールドだけをサポートし、複雑な式言語や DSL は採用しない。

- **Deterministic（決定論的）**  
  YAML → FlowDefinition → FlowEngine の流れが完全に透明で、  
  どのように実行されるかが明確。

- **Transparent（透明）**  
  YAML の構造がそのままモデルにマッピングされ、  
  暗黙のデフォルトや自動補完は存在しない。

- **No Hidden Magic（隠れた魔法なし）**  
  推測・変換・省略記法・自動エラーハンドリングなどは行わない。

- **Structural Only（構造のみ）**  
  バリデーションや補完は runtime 側の責務であり、  
  YAML パーサは純粋に構造を読み取るだけ。

---

## 7.2 Supported Top-Level Fields（トップレベルでサポートされるフィールド）

YAML フロー定義は次のフィールドを持つことができます：

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | フローの論理名 |
| `steps` | list | StepDefinition のリスト |
| `onError` | object (optional) | フロー全体のデフォルトエラーハンドリング |
| `metadata` | map (optional) | 任意のメタデータ（実行には影響しない） |

---

## 7.3 Step Definition Fields（ステップ定義のフィールド）

各ステップは次のフィールドを持ちます：

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | ステップ名 |
| `module` | string | 呼び出すモジュール名 |
| `operation` | string | モジュール内の操作名 |
| `input` | map (optional) | 入力マッピング（評価前の生データ） |
| `output` | map (optional) | 出力マッピング（評価前の生データ） |
| `retry` | object (optional) | RetryPolicy |
| `onError` | object (optional) | ステップ固有のエラーハンドリング |

---

## 7.4 RetryPolicy Fields（リトライポリシーのフィールド）

`retry` ブロックは次のフィールドを持ちます：

| Field | Type | Description |
|-------|------|-------------|
| `maxAttempts` | integer | 初回を含む総試行回数 |
| `delayMillis` | integer | リトライ間の待機時間（ミリ秒） |

RetryPolicy は **SystemException / StepExecutionException** のみに適用されます。

---

## 7.5 OnErrorDefinition Fields（エラーハンドリングのフィールド）

`onError` ブロックは次のフィールドを持ちます：

| Field | Type | Description |
|-------|------|-------------|
| `action` | string | `STOP` / `CONTINUE` / `RETRY` |
| `delayMillis` | integer (optional) | RETRY 前の待機時間 |
| `output` | map (optional) | エラー時のみ評価される出力マッピング |

特徴：

- step-level が flow-level を上書き  
- RETRY は **1回だけ**  
- output はエラー時のみ評価され ExecutionContext にマージされる  

---

## 7.6 Placeholder Rules（プレースホルダルール）

BareFlow のプレースホルダは **非常にシンプル**です。

サポートされる形式：

```
${name}
```

ルール：

- ネスト `${a.b}` は非サポート（未解決なら null）  
- input は ExecutionContext から解決  
- output は rawOutput → ExecutionContext の順で解決  
- 未解決は null  
- 式言語・スクリプトは存在しない  

---

## 7.7 Example YAML Definition（YAML 定義の例）

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

この YAML は以下に直接マッピングされます：

- FlowDefinition  
- StepDefinition  
- RetryPolicy（なし）  
- OnErrorDefinition（flow-level）  
- metadata  

変換・補完・推論は一切行われません。

---

BareFlow の YAML DSL は、  
**「書いたとおりに実行される」**という哲学を体現した  
ミニマルで透明な定義形式です。

# 8. Error Handling（エラーハンドリング）

BareFlow のエラーハンドリングモデルは、  
**システムレベルの失敗** と **ビジネスレベルの失敗** を明確に区別し、  
リトライとエラー処理の意味論を厳密に定義しています。

隠れたリトライや暗黙のエラー処理は一切存在せず、  
すべての挙動は FlowDefinition / StepDefinition に明示的に記述されます。

---

## 8.1 Error Types（エラーの種類）

BareFlow はエラーを 3 種類に分類します。

### **BusinessException**
- ビジネスロジック上の失敗  
- 例：入力不正、ドメインルール違反  
- **RetryPolicy の対象外**  
- onError による処理は可能  

### **SystemException**
- システムレベルの失敗  
- 例：ネットワーク障害、I/O エラー  
- **RetryPolicy の対象**  
- onError による処理も可能  

### **StepExecutionException**
- ステップ実行時の予期しない失敗  
- 例：反射呼び出しの失敗、モジュール内部の例外  
- **RetryPolicy の対象**  
- onError による処理も可能  

---

## 8.2 System-Level Retry（システムレベルのリトライ）

`RetryPolicy` は **SystemException / StepExecutionException** にのみ適用されます。

ルール：

- attempts は **1 から開始**  
- `attempt < maxAttempts` の間リトライ  
- delayMillis だけ待機して再試行  
- BusinessException は絶対にリトライされない  

RetryPolicy は **決定論的**であり、  
暗黙のリトライは存在しません。

---

## 8.3 Business-Level Error Handling（ビジネスレベルのエラー処理）

RetryPolicy が尽きた後、`OnErrorDefinition` が適用されます。

アクション：

### **STOP**
- エラーを伝播し、フローを即停止

### **CONTINUE**
- エラーを無視して次のステップへ進む  
- output mapping があれば評価して ExecutionContext にマージ

### **RETRY**
- **1回だけ**ビジネスレベルのリトライ  
- delayMillis 待機後に再試行  
- 成功すれば続行、失敗すれば再度 onError が適用される

特徴：

- step-level が flow-level を上書き  
- RETRY は FlowEngine 内で厳密に「1回だけ」に制御される  

---

## 8.4 Interaction Between RetryPolicy and OnErrorDefinition  
（RetryPolicy と OnError の相互作用）

両者は **独立**しており、次の順序で評価されます：

```
1. RetryPolicy（システムレベルのリトライ）
2. OnErrorDefinition（ビジネスレベルのエラー処理）
```

例：

```
RetryPolicy.maxAttempts = 2
onError.action = RETRY
```

タイムライン：

```
attempt 1 → SystemException → RetryPolicy retry
attempt 2 → SystemException → RetryPolicy exhausted → onError.RETRY
attempt 3 → success
```

---

## 8.5 Error Events（エラーイベント）

BareFlow はエラーに関するすべての挙動をイベントとして発行します。

- BusinessErrorEvent  
- SystemErrorEvent  
- StepExecutionErrorEvent  
- RetryPolicyRetryEvent  
- OnErrorRetryEvent  
- UnhandledErrorEvent  

これにより、エラー発生時の挙動が完全に観測可能になります。

---

BareFlow のエラーハンドリングモデルは、  
**明確・決定論的・透明**であることを最優先に設計されています。

# 9. Tracing & Observability（トレーシングと可観測性）

BareFlow は、実行のすべてを「見える化」するために  
**イベントストリーム** と **不変の実行トレース** の 2 つの仕組みを提供します。

これにより、各ステップの入力・出力・リトライ・エラー・タイムスタンプが  
完全に観測可能となり、デバッグ・監査・モニタリングに強いフロー実行が実現されます。

---

## 9.1 Design Goals（設計目標）

BareFlow の可観測性モデルは次の目標に基づいています：

- **Complete（完全）**  
  実行のあらゆる瞬間を記録する。

- **Deterministic（決定論的）**  
  イベントとトレースの構造が常に一定で予測可能。

- **Non-intrusive（非侵入的）**  
  観測は実行に影響を与えない。

- **Extensible（拡張可能）**  
  ロギング・メトリクス・監査などを自由に追加できる。

- **Transparent（透明）**  
  隠れた処理や暗黙のロギングは存在しない。

---

## 9.2 Event Stream（イベントストリーム）

FlowEngine は実行中に以下のイベントを発行します：

- **フロー開始 / 終了**  
  - FlowStartEvent  
  - FlowEndEvent  

- **ステップ開始 / 終了**  
  - StepStartEvent  
  - StepEndEvent  

- **入力評価**  
  - InputEvaluationStartEvent  
  - InputEvaluationEndEvent  

- **呼び出し**  
  - InvokeStartEvent  
  - InvokeEndEvent  

- **出力評価**  
  - OutputEvaluationStartEvent  
  - OutputEvaluationEndEvent  

- **リトライ関連**  
  - RetryPolicyRetryEvent  
  - OnErrorRetryEvent  

- **エラー関連**  
  - BusinessErrorEvent  
  - SystemErrorEvent  
  - StepExecutionErrorEvent  
  - UnhandledErrorEvent  

### 特徴

- すべて **immutable record**  
- FlowEngineEventListener によって受信  
- 実行には一切影響しない（純粋な観測）

イベントストリームはリアルタイムの可観測性を提供します。

---

## 9.3 StepTraceEntry（ステップ試行の記録）

各ステップの試行は `StepTraceEntry` として記録されます。

含まれる情報：

- evaluatedInput（評価済み入力）  
- rawOutput（生の出力）  
- mappedOutput（マッピング後の出力）  
- error（例外）  
- retry attempts（試行番号）  
- timestamps（開始・終了時刻）  
- beforeContext（実行前のコンテキストスナップショット）  

特徴：

- 完全なスナップショット  
- 不変  
- 成功/失敗が明確に判定可能  

---

## 9.4 StepTrace（フロー全体のトレース）

`StepTrace` はすべての StepTraceEntry を保持し、  
フロー全体の実行履歴を表します。

提供される派生情報：

- **isAllSuccessful()**  
  全試行が成功した場合のみ true

- **isFinallySuccessful()**  
  最終試行が成功していれば true

- **wasRetried()**  
  リトライが発生した場合 true

- **getTotalAttempts()**  
  総試行回数

特徴：

- 不変  
- シリアライズ・ログ出力・監査に安全  
- 実行の「唯一の真実の記録（source of truth）」となる  

---

## 9.5 Logging Integration（ログ統合）

BareFlow はデフォルトではログを出力しません。  
ログ出力は runtime の責務です。

- **LoggingAdapter**  
  - SLF4J ロガーにイベントを出力  
  - ログレベルに応じて LogFormatter を呼び出す  

- **LogFormatter**  
  - イベントを文字列に整形  
  - JSON / key-value / カスタム形式など自由に実装可能  

ログの構造と出力方法は完全にユーザーが制御できます。

---

## 9.6 Observability Without Side Effects（副作用のない可観測性）

BareFlow は次を保証します：

- イベントは実行に影響しない  
- トレースは実行結果を変えない  
- ロギングは完全にオプトイン  
- 観測は常に決定論的  

これにより、可観測性を有効にしても  
フローの動作が変わることはありません。

---

BareFlow のトレーシングと可観測性モデルは、  
**完全な透明性・監査性・デバッグ容易性**を提供しながら、  
実行の純粋性と決定論を損なわないよう設計されています。

# 10. Extending BareFlow（BareFlow の拡張）

BareFlow はコアを徹底的にミニマルに保ちながら、  
必要に応じて柔軟に拡張できるよう設計されています。

コアエンジンは **決定論的な実行ルール**のみを提供し、  
実際の動作や統合ポイントは runtime 側で自由に差し替え可能です。

この章では、BareFlow が提供する公式の拡張ポイントと、  
拡張時に守るべき原則を説明します。

---

## 10.1 Extension Philosophy（拡張哲学）

BareFlow の拡張モデルは次の原則に基づいています：

- **Explicit, not implicit（明示的であること）**  
  拡張はユーザーが明示的に選択したときのみ有効になる。

- **Deterministic（決定論的であること）**  
  拡張によって実行結果が非決定的になってはならない。

- **Isolated（隔離されていること）**  
  拡張はコアの意味論を変更してはならない。

- **Composable（合成可能であること）**  
  複数の拡張を組み合わせても動作が破綻しない。

- **Replaceable（置換可能であること）**  
  runtime のすべてのコンポーネントは差し替え可能。

---

## 10.2 Custom StepEvaluator（カスタム評価器）

`StepEvaluator` を差し替えることで、  
入力・出力マッピングの評価ルールを変更できます。

用途：

- 独自のプレースホルダ形式をサポート  
- 式言語の導入  
- バリデーションの追加  
- ドメイン固有のマッピングルール  

要件：

- 評価は **純粋で決定論的**であること  
- ExecutionContext を直接変更しないこと  
- 未解決値の扱いを明確にすること  

デフォルト実装は **フラットな `${name}` のみ**をサポートします。

---

## 10.3 Custom StepInvoker（カスタム呼び出し器）

`StepInvoker` を差し替えることで、  
ステップの実行方法を自由に変更できます。

用途：

- DI コンテナとの統合  
- リモートサービス呼び出し  
- 非同期処理  
- カスタムモジュールシステム  
- リトライやラップ処理の追加  

要件：

- 戻り値は `Map<String,Object>`  
- BusinessException / SystemException の扱いを明確にする  
- ExecutionContext を直接変更しない  

デフォルト実装は反射ベースの単純な呼び出しです。

---

## 10.4 Custom ModuleResolver（カスタムモジュール解決）

`ModuleResolver` を差し替えることで、  
モジュール名 → クラスの解決方法を変更できます。

用途：

- DI コンテナ（Spring, Micronaut など）との統合  
- サービスロケータ  
- プラグインシステム  
- 動的モジュールロード  

要件：

- 解決は決定論的であること  
- 見つからない場合は明確な例外を投げること  

---

## 10.5 Custom LoggingAdapter / LogFormatter（ログ拡張）

BareFlow はログ出力を runtime に委ねています。

### LoggingAdapter
- イベントをログに出力  
- ログレベルに応じて LogFormatter を呼び出す  

### LogFormatter
- イベントを文字列に整形  
- JSON / key-value / カスタム形式など自由に実装可能  

ログ構造は完全にユーザーが制御できます。

---

## 10.6 Custom FlowEngineEventListener（イベントリスナー拡張）

`FlowEngineEventListener` を実装することで、  
実行中のすべてのイベントをフックできます。

用途：

- デバッグ  
- メトリクス収集  
- 分散トレーシング  
- モニタリング  
- 監査ログ  

複数のリスナーは `CompositeFlowEngineEventListener` で合成できます。

---

## 10.7 Custom YAML Parser（カスタム YAML パーサ）

`FlowYamlParser` を差し替えることで、  
BareFlow の YAML DSL を拡張したり、  
まったく別の形式（JSON / XML / 独自 DSL）をサポートできます。

用途：

- YAML スキーマの厳密なバリデーション  
- 拡張フィールドの追加  
- 別形式（JSON / XML / TOML など）からの FlowDefinition 生成  
- ドメイン固有 DSL の導入  
- FlowDefinition のキャッシュやプリプロセス処理  

要件：

- 出力は **正しい FlowDefinition** であること  
- 暗黙のデフォルトや推論を追加する場合は、  
  BareFlow の決定論を損なわないよう注意すること  
- パーサは構造的であるべきで、  
  FlowEngine の意味論を変更してはならない  

---

## 10.8 Guidelines for Safe Extensions（安全な拡張のためのガイドライン）

BareFlow を拡張する際は、次の原則を守る必要があります。

### **1. グローバル状態を持たないこと**
拡張コンポーネントは、外部のグローバル状態に依存してはならない。  
決定論が損なわれるため。

### **2. 非決定的な処理を導入しないこと**
ランダム性・時刻依存・並列処理などは  
FlowEngine の意味論を壊す可能性がある。

### **3. 暗黙のリトライやエラー処理を追加しないこと**
RetryPolicy / OnError の意味論は厳密に定義されているため、  
拡張側で勝手にリトライを追加してはならない。

### **4. ExecutionContext を直接変更しないこと**
ExecutionContext の変更は FlowEngine の責務。  
Invoker や Evaluator が直接変更すると意味論が壊れる。

### **5. 拡張は常に「明示的」であること**
ユーザーが意図しない拡張が自動的に有効になるべきではない。

---

BareFlow の拡張モデルは、  
**コアの純粋性を保ちながら、必要な場所だけを柔軟に拡張できる**  
というバランスを実現しています。

# 11. License（ライセンス）

BareFlow は **MIT License** のもとで公開されています。

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

MIT License は、著作権表示とライセンス文を保持する限り、  
ソフトウェアの利用・複製・改変・再配布を自由に行える、  
非常に寛容なオープンソースライセンスです。