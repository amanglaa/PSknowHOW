/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.model.application;//NOPMD

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Field mapping. Represents Jira field mapping values
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "field_mapping")
public class FieldMapping extends BasicModel {

	private ObjectId projectToolConfigId;
	private ObjectId basicProjectConfigId;
	private String sprintName;
	private String epicName;
	private List<String> jiradefecttype;

	// defectPriority
	private List<String> defectPriority;
	private String[] jiraIssueTypeNames;
	private List<String> jiraIssueEpicType;
	private String storyFirstStatus;
	private String envImpacted;
	private String rootCause;
	private List<String> jiraStatusForDevelopment;
	@Builder.Default
	private List<String> jiraStatusForQa = Arrays.asList("Ready For Testing", "In Testing");
	private List<String> testRegressionValue; // TODO delete this field
	// type of test cases
	private List<String> jiraDefectInjectionIssueType;
	private List<String> jiraDod;
	private String jiraDefectCreatedStatus;
	private String jiraTechDebtIdentification;
	private String jiraTechDebtCustomField;
	private List<String> jiraTechDebtValue;
	private String jiraDefectRejectionStatus;
	private String jiraBugRaisedByIdentification;
	private List<String> jiraBugRaisedByValue;
	private List<String> jiraDefectSeepageIssueType;
	private String jiraBugRaisedByCustomField;
	private List<String> jiraDefectRemovalStatus;
	private List<String> jiraDefectRemovalIssueType;
	private List<String> regressionAutomationFolderPath;// TODO delete this field
	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String jiraStoryPointsCustomField;
	// parent issue type for the test
	private List<String> jiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<String> jiraCanNotAutomatedTestValue;

	private List<String> jiraSprintVelocityIssueType;

	private List<String> jiraSprintCapacityIssueType;

	private List<String> jiraDefectRejectionlIssueType;
	private List<String> jiraDefectCountlIssueType;

	private List<String> jiraIssueDeliverdStatus;

	private String jiraDor;

	private List<String> jiraIntakeToDorIssueType;

	private List<String> jiraStoryIdentification;

	private String jiraLiveStatus;

	private List<String> ticketCountIssueType;

	private List<String> kanbanRCACountIssueType;

	private List<String> jiraTicketVelocityIssueType;

	private List<String> ticketDeliverdStatus;
	private List<String> jiraTicketClosedStatus;
	private List<String> kanbanCycleTimeIssueType;
	private List<String> jiraTicketTriagedStatus;
	private List<String> jiraTicketRejectedStatus;

	private String jiraStatusMappingCustomField;
	private List<String> excludeRCAFromFTPR;

	private List<String> resolutionTypeForRejection;
	private List<String> jiraQADefectDensityIssueType;
	private String jiraBugRaisedByQACustomField;
	private String jiraBugRaisedByQAIdentification;
	private List<String> jiraBugRaisedByQAValue;
	private List<String> jiraDefectDroppedStatus;

	// Epic custom Field mapping
	private String epicRiskReduction;
	private String epicUserBusinessValue;
	private String epicWsjf;
	private String epicTimeCriticality;
	private String epicJobSize;

	// Production Defect Mapping
	private String productionDefectCustomField;
	private String productionDefectIdentifier;
	private List<String> productionDefectValue;
	private String productionDefectComponentValue;

	// testCaseMapping
	private String[] jiraTestCaseType;
	private String testAutomatedIdentification;
	private String testAutomationCompletedIdentification;
	private String testAutomated;
	private String testAutomationCompletedByCustomField;
	private List<String> jiraAutomatedTestValue;
	private List<String> jiraRegressionTestValue;
	private List<String> jiraCanBeAutomatedTestValue;
	private List<String> testCaseStatus;
	@Builder.Default
	private String estimationCriteria = "Story Point";

	@Builder.Default
	private Double workingHoursDayCPT = 6D;

	// additional filter config fields
	private List<AdditionalFilterConfig> additionalFilterConfig;

	// issue status to exclude missing worklogs
	private List<String> issueStatusExcluMissingWork;

	/**
	 * Get jira issue type names string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getJiraIssueTypeNames() {
		return jiraIssueTypeNames == null ? null : jiraIssueTypeNames.clone();
	}

	/**
	 * Sets jira issue type names.
	 *
	 * @param jiraIssueTypeNames
	 *            the jira issue type names
	 */
	public void setJiraIssueTypeNames(String[] jiraIssueTypeNames) {
		this.jiraIssueTypeNames = jiraIssueTypeNames == null ? null : jiraIssueTypeNames.clone();
	}

	/**
	 * Get jira test case type string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getJiraTestCaseType() {
		return jiraTestCaseType == null ? null : jiraTestCaseType.clone();
	}

	/**
	 * Sets jira test case type.
	 *
	 * @param jiraTestCaseType
	 *            the jira test case type
	 */
	public void setJiraTestCaseType(String[] jiraTestCaseType) {
		this.jiraTestCaseType = jiraTestCaseType == null ? null : jiraTestCaseType.clone();
	}

}