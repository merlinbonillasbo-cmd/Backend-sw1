package com.workflow.modules.analytics.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        // TODO: aggregate metrics from repositories
        summary.put("totalInstances", 0);
        summary.put("pendingInstances", 0);
        summary.put("completedInstances", 0);
        summary.put("rejectedInstances", 0);
        return summary;
    }

}
