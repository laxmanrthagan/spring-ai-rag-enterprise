package com.ragagentic.ai.service;

import com.ragagentic.ai.dto.RetrievalResult;

public interface RetrievalPipeline {
    RetrievalResult retrieve(String question, String departmentFilter);
}