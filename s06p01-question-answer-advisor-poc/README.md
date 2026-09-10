# s06p01 - QuestionAnswerAdvisor

First POC of Level 6 (Retrieval-Augmented Generation). QuestionAnswerAdvisor is Spring AI's
built-in ChatClient advisor that turns any VectorStore into a RAG pipeline: it runs a similarity
search against the store, injects the results into the prompt, and lets the chat model answer
grounded in that retrieved context instead of (only) its training data.

What's demonstrated:

- QuestionAnswerAdvisor.builder(VectorStore).build() - building the advisor from any VectorStore
  (here, SimpleVectorStore backed by the Gemini embedding model, same as s05p03).
- ChatClient.prompt().advisors(advisor).user(question).call().content() - wiring the advisor into
  a normal ChatClient call; the advisor transparently rewrites the prompt before it reaches the
  model.
- Per-query SearchRequest customization - .searchRequest(SearchRequest.builder().topK().similarityThreshold())
  passed into the advisor builder, changing how many/which documents get retrieved for a given call.
- .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "topic == 'x'")) - scoping
  retrieval to a metadata subset at call time, without building a new advisor.

## Requirements

- Java 25, Maven
- A Gemini API key (embeddings) and an Anthropic API key (chat) for the live demo path - optional;
  mvn test is fully green without either, since the advisor's actual retrieval/filter/prompt-
  augmentation mechanics are proven directly against QuestionAnswerAdvisor.before() with a
  deterministic fake embedding model.

## Setup

```sh
export GEMINI_API_KEY=AQ...
export ANTHROPIC_API_KEY=sk-ant-...
```

No POC-local .gitignore needed - the repo-root one already covers target/.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with JAVA_HOME/PATH as in the repo's root CLAUDE.md.

- No API keys set: context-load test + all 4 QuestionAnswerAdvisorMechanicsTests run (5 tests) -
  these call QuestionAnswerAdvisor.before() directly against a real SimpleVectorStore backed by a
  small deterministic hash-based fake EmbeddingModel, proving retrieval, per-query topK limiting,
  and FILTER_EXPRESSION scoping all work with zero keys. The 2-test live class skips.
- With both GEMINI_API_KEY and ANTHROPIC_API_KEY set: all 7 run, including a live end-to-end
  QuestionAnswerAdvisor-grounded Claude answer.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. With both keys set, loads the knowledge base, asks a question with the default
advisor, again with a narrowed per-query SearchRequest, and once more scoped to an unrelated
topic (geography) to show the model correctly refuses to answer once its retrieved context no
longer contains the relevant documents. Without either key, explains and exits cleanly.

## Key API Reference

| API | Purpose |
|---|---|
| QuestionAnswerAdvisor.builder(VectorStore) | Build a RAG advisor from any VectorStore |
| ChatClient.prompt().advisors(advisor) | Attach the advisor to a single chat call |
| QuestionAnswerAdvisor.Builder.searchRequest(SearchRequest) | Customize the advisor's own retrieval (topK/similarityThreshold) |
| .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, expr)) | Scope retrieval to matching metadata for one call |
| QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS | Context/metadata key holding the documents the advisor actually retrieved |

## What this proves

- QuestionAnswerAdvisor genuinely rewrites the prompt before it reaches the model - the augmented
  user message contains the retrieved documents' text verbatim, not just a summary or reference.
- Per-query SearchRequest customization (topK, similarityThreshold) actually changes how many
  documents get retrieved for a given advisor instance, independent of the underlying VectorStore.
- .advisors(a -> a.param(FILTER_EXPRESSION, ...)) genuinely restricts the retrieval candidate set
  per call - scoping to an unrelated topic leaves the model with no grounding for the original
  question, and (in the live test) it correctly says so rather than answering from prior knowledge.

Next on the roadmap: s06p02-retrieval-augmentation-advisor-poc - the more composable
RetrievalAugmentationAdvisor, VectorStoreDocumentRetriever, query rewriting via
ContextualQueryAugmentor, and multi-retriever merging via DocumentJoiner.
