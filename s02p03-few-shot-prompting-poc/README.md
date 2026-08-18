# s02p03 — Few-Shot Prompting

Constructs few-shot `Prompt` objects two ways — alternating `UserMessage`/`AssistantMessage` pairs, and a `PromptTemplate` that loops over example data to render them into one templated string — and compares the resulting classification quality (zero-shot vs. few-shot) on Claude vs. Gemini for a customer-support-message classification task.

## Requirements

- Java 25
- Maven
- An Anthropic API key and/or a Gemini API key

## Setup

```sh
export ANTHROPIC_API_KEY=sk-ant-...
export GEMINI_API_KEY=...
```

Without either variable, the app still starts and the wiring/unit tests still pass — the live test for whichever provider's key is missing skips gracefully.

## Run the tests

```sh
mvn test
```

`FewShotPromptConstructionTests` is ungated and needs no API key. `AnthropicFewShotServiceLiveTest`/`GeminiFewShotServiceLiveTest` are gated per provider and skip gracefully with no key.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — on startup it classifies the same support message zero-shot and few-shot against whichever provider(s) have a key set, and prints the comparison when both are available.

## Test

```sh
mvn test
```
