package com.publicissapient.kpidashboard.common.model.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JiraIssueMap {

    private String issueNumber;

    private String issueDesc;

    private String issueURL;
}
