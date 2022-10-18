package com.publicissapient.kpidashboard.common.model.jira;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class IssueHistoryMappedData {

	@Id
	private IssueGroupFields id;
	// for scrum
	private List<JiraIssueSprint> storySprintDetails = new ArrayList<>();
	// for kanban
	private List<KanbanIssueHistory> historyDetails = new ArrayList<>();
}
