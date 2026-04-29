package com.workflow.modules.aiassistant.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    public List<String> recommendNextNode(String workflowDefinitionId, String currentNodeId) {
        // TODO: implement AI-based node recommendation logic
        return List.of();
    }

    public List<String> recommendAssignee(String workflowDefinitionId, String nodeId) {
        // TODO: implement AI-based assignee recommendation logic
        return List.of();
    }

}
