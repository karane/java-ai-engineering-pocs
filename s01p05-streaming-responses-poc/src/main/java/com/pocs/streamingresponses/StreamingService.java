package com.pocs.streamingresponses;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.Subscription;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@Service
public class StreamingService {

    private final ChatClient anthropicChatClient;
    private final ChatClient geminiChatClient;

    public StreamingService(@Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
                             @Qualifier("geminiChatClient") ChatClient geminiChatClient) {
        this.anthropicChatClient = anthropicChatClient;
        this.geminiChatClient = geminiChatClient;
    }

    public Flux<String> anthropicStreamContent(String topic) {
        return anthropicChatClient.prompt()
                .user(promptFor(topic))
                .stream()
                .content();
    }

    public Flux<String> geminiStreamContent(String topic) {
        return geminiChatClient.prompt()
                .user(promptFor(topic))
                .stream()
                .content();
    }

    public Flux<ChatResponse> anthropicStreamChatResponse(String topic) {
        return anthropicChatClient.prompt()
                .user(promptFor(topic))
                .stream()
                .chatResponse();
    }

    public Flux<ChatResponse> geminiStreamChatResponse(String topic) {
        return geminiChatClient.prompt()
                .user(promptFor(topic))
                .stream()
                .chatResponse();
    }

    public StreamingChunkStats collectStats(Flux<String> contentStream) {
        AtomicInteger chunkCount = new AtomicInteger();
        String fullText = contentStream
                .doOnNext(chunk -> chunkCount.incrementAndGet())
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .block(Duration.ofSeconds(60));
        return new StreamingChunkStats(fullText, chunkCount.get());
    }

    public StreamingChunkStats collectWithBackpressure(Flux<String> contentStream) {
        StringBuilder textSoFar = new StringBuilder();
        AtomicInteger chunkCount = new AtomicInteger();
        CompletableFuture<Void> done = new CompletableFuture<>();

        contentStream.subscribe(new BaseSubscriber<String>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            protected void hookOnNext(String chunk) {
                textSoFar.append(chunk);
                chunkCount.incrementAndGet();
                request(1);
            }

            @Override
            protected void hookOnComplete() {
                done.complete(null);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });

        done.join();
        return new StreamingChunkStats(textSoFar.toString(), chunkCount.get());
    }

    private String promptFor(String topic) {
        return "Write a short (3-4 sentence) paragraph about: " + topic;
    }
}
