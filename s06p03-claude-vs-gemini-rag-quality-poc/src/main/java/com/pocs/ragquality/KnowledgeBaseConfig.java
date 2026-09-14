package com.pocs.ragquality;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The fixed, small knowledge base both providers retrieve from: a handful of short, non-overlapping facts about a fictional note-taking app, "Aurora Notes". */
@Configuration
public class KnowledgeBaseConfig {

    @Bean
    public List<Document> knowledgeBaseDocuments() {
        return List.of(
                new Document(
                        "Aurora Notes was founded in 2019 by Priya Malhotra as a small note-taking app "
                                + "for students.",
                        Map.of("topic", "history")),
                new Document(
                        "Aurora Notes is available on Windows, macOS, Linux, iOS, and Android. There is "
                                + "no web version.",
                        Map.of("topic", "platforms")),
                new Document(
                        "The Aurora Notes free plan allows up to 200 notes and 500 MB of storage. The Pro "
                                + "plan costs $6 per month and removes both limits.",
                        Map.of("topic", "pricing")),
                new Document(
                        "Aurora Notes supports end-to-end encryption for Pro subscribers only. Free-plan "
                                + "notes are stored unencrypted on Aurora's servers.",
                        Map.of("topic", "security")),
                new Document(
                        "Aurora Notes added real-time collaborative editing in version 4.0, released in "
                                + "March 2023. Up to 10 people can edit the same note at once.",
                        Map.of("topic", "collaboration")),
                new Document(
                        "Aurora Notes does not offer a public API. Third-party integrations are limited "
                                + "to a Zapier connector released in 2022.",
                        Map.of("topic", "integrations")),
                new Document(
                        "Aurora Notes' offline mode caches the last 30 days of edits locally and syncs "
                                + "automatically once the device reconnects to the internet.",
                        Map.of("topic", "offline")),
                new Document(
                        "Aurora Notes is headquartered in Lisbon, Portugal, and has 22 employees as of "
                                + "2025.",
                        Map.of("topic", "company")));
    }
}
