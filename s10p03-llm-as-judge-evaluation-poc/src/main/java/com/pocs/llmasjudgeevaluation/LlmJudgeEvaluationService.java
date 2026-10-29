package com.pocs.llmasjudgeevaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;

/** Wraps Spring AI's {@link RelevancyEvaluator} and {@link FactCheckingEvaluator} with a judge ChatClient. */
public class LlmJudgeEvaluationService {

    private static final PromptTemplate RELEVANCY_PROMPT_TEMPLATE = new PromptTemplate("""
            Your task is to evaluate if the response for the query
            is in line with the context information provided.

            You have two options to answer. Either YES or NO.

            Answer YES, if the response for the query
            is in line with context information otherwise NO.

            Respond with exactly one word, YES or NO, and nothing else - no punctuation,
            no explanation.

            Query:
            {query}

            Response:
            {response}

            Context:
            {context}

            Answer:
            """);

    private static final String FACT_CHECKING_PROMPT = """
            Evaluate whether or not the following claim is supported by the provided document.
            Respond with exactly one word, "yes" or "no", and nothing else - no punctuation,
            no explanation.

            Document:
            {document}

            Claim:
            {claim}
            """;

    private final RelevancyEvaluator relevancyEvaluator;
    private final FactCheckingEvaluator factCheckingEvaluator;

    public LlmJudgeEvaluationService(ChatClient.Builder judgeChatClientBuilder) {
        this.relevancyEvaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(judgeChatClientBuilder)
                .promptTemplate(RELEVANCY_PROMPT_TEMPLATE)
                .build();
        this.factCheckingEvaluator = FactCheckingEvaluator.builder(judgeChatClientBuilder)
                .evaluationPrompt(FACT_CHECKING_PROMPT)
                .build();
    }

    /** Does the answer actually address the question, given the retrieved context? */
    public EvaluationResponse evaluateRelevancy(RagAnswer ragAnswer) {
        return relevancyEvaluator.evaluate(toEvaluationRequest(ragAnswer));
    }

    /** Is the answer factually supported by the retrieved context (no hallucination)? */
    public EvaluationResponse evaluateFactChecking(RagAnswer ragAnswer) {
        return factCheckingEvaluator.evaluate(toEvaluationRequest(ragAnswer));
    }

    private EvaluationRequest toEvaluationRequest(RagAnswer ragAnswer) {
        return new EvaluationRequest(ragAnswer.question(), ragAnswer.retrievedDocuments(), ragAnswer.content());
    }
}
