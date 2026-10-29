package com.pocs.llmasjudgeevaluation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Runs the golden dataset through the LLM-as-judge evaluators and prints each provider's gate verdict. */
@Component
public class EvaluationDemoRunner implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final ChatModel anthropicChatModel;
    private final ChatModel geminiChatModel;

    public EvaluationDemoRunner(VectorStore vectorStore,
            @Qualifier("anthropicChatModel") ChatModel anthropicChatModel,
            @Qualifier("googleGenAiChatModel") ChatModel geminiChatModel) {
        this.vectorStore = vectorStore;
        this.anthropicChatModel = anthropicChatModel;
        this.geminiChatModel = geminiChatModel;
    }

    @Override
    public void run(String... args) {
        if (isSet("ANTHROPIC_API_KEY")) {
            runGateFor("Claude (Anthropic)", anthropicChatModel);
        } else {
            System.out.println("[s10p03] --- Claude (Anthropic) --- skipped, ANTHROPIC_API_KEY not set");
        }

        if (isSet("GEMINI_API_KEY")) {
            runGateFor("Gemini", geminiChatModel);
        } else {
            System.out.println("[s10p03] --- Gemini --- skipped, GEMINI_API_KEY not set");
        }
    }

    private void runGateFor(String label, ChatModel judgeModel) {
        System.out.println("[s10p03] --- " + label + " as judge ---");
        LlmJudgeEvaluationService evaluationService = new LlmJudgeEvaluationService(ChatClient.builder(judgeModel));
        ConcisenessEvaluator concisenessEvaluator = new ConcisenessEvaluator(60);

        List<EvaluationResponse> relevancyResponses = new ArrayList<>();
        List<EvaluationResponse> factCheckingResponses = new ArrayList<>();
        List<EvaluationResponse> concisenessResponses = new ArrayList<>();

        for (GoldenCase goldenCase : GoldenDataset.CASES) {
            RagAnswerService ragAnswerService =
                    new RagAnswerService(new CannedChatModel(goldenCase.groundedAnswer()), vectorStore);
            RagAnswer ragAnswer = ragAnswerService.answer(goldenCase.question());

            relevancyResponses.add(evaluationService.evaluateRelevancy(ragAnswer));
            factCheckingResponses.add(evaluationService.evaluateFactChecking(ragAnswer));
            concisenessResponses.add(concisenessEvaluator.evaluate(
                    new EvaluationRequest(ragAnswer.question(), ragAnswer.retrievedDocuments(), ragAnswer.content())));
        }

        System.out.println("[s10p03] relevancy gate:      " + QualityGate.evaluate(relevancyResponses));
        System.out.println("[s10p03] fact-checking gate:   " + QualityGate.evaluate(factCheckingResponses));
        System.out.println("[s10p03] conciseness gate:     " + QualityGate.evaluate(concisenessResponses));
    }

    private boolean isSet(String envVar) {
        String value = System.getenv(envVar);
        return value != null && !value.isBlank();
    }
}
