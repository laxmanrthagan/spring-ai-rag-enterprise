package com.ragagentic.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticEvaluation {
    private boolean valid;
    private double groundingScore;
    private List<String> identifiedHallucinations;
    private String feedbackForReasoner;
}