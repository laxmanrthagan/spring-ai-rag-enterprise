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
public class Plan {
    private List<String> steps;
    private boolean useGraphRAG;
    private String optimizedQuery;
}