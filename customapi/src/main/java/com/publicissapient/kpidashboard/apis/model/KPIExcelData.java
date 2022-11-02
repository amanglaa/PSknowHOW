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

package com.publicissapient.kpidashboard.apis.model;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Excel Data for KPIs
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KPIExcelData {

	@JsonProperty("Sprint Name")
	private String sprintName;
	
	@JsonProperty("Story ID")
	private Map<String,String> storyId;
	
	@JsonProperty("Issue Description")
	private String issueDesc;
	
	@JsonProperty("Linked Defects")
	private Map<String,String> linkedDefects;

	@JsonProperty("First Time Pass")
	private String firstTimePass;

	@JsonProperty("Escaped Defect")
	private String escapedDefect;

	@JsonProperty("Defect Removed")
	private String removedDefect;

	@JsonProperty("Defect Rejected")
	private String rejectedDefect;

	@JsonProperty("Priority")
	private String priority;

	@JsonProperty("Root Cause")
	private List<String> rootCause;

	@JsonProperty("Resolved")
	private String resolvedTickets;

	@JsonProperty("Defect ID")
	private Map<String,String> defectId;

	@JsonProperty("Created Defect ID")
	private Map<String,String> createdDefectId;

	@JsonProperty("Test Case ID")
	private String testCaseId;

	@JsonProperty("Automated")
	private String automated;

	@JsonProperty("Project")
	private String project;

	@JsonProperty("Job Name")
	private String jobName;

	@JsonProperty("Unit Coverage")
	private String unitCoverage;

	@JsonProperty("Tech Debt (in days)")
	private String techDebt;

	@JsonProperty("Sonar Violations")
	private String sonarViolation;

	@JsonProperty("Weeks")
	private String weeks;

	@JsonProperty("Linked Story ID")
	private Map<String,String> linkedStory;



}
