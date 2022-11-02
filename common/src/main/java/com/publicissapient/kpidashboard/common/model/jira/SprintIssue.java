package com.publicissapient.kpidashboard.common.model.jira;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SprintIssue {
    private String number;
    private String priority;
    private String status;
    private String typeName;
    private Double storyPoints;
    private Double originalEstimate;
    private Double remainingEstimate;
}
