package com.pocs.schemavalidationretry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class LeadExtractionServiceLiveTest {

    @Autowired
    private LeadExtractionService leadExtractionService;

    @Test
    void extractsAValidatedLeadFromARealInquiryAgainstClaude() {
        String rawInquiry = "Hey there, this is Alex Martin. Best way to reach me is "
                + "alex.martin@example.com or +1-415-555-0199. We run a 40-person marketing team "
                + "and want a demo of the enterprise plan before end of quarter.";

        ExtractionResult result = leadExtractionService.extractLead(rawInquiry);

        assertThat(result.attemptsUsed()).isGreaterThan(0);
        assertThat(result.lead().fullName()).isNotBlank();
        assertThat(result.lead().email()).contains("@");
        assertThat(result.lead().confidenceScore()).isBetween(0.0, 1.0);
    }
}
