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

package com.publicissapient.kpidashboard.apis.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.ChangeFailureRateInfo;
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

	private static final String MONTH_YEAR_FORMAT = "MMM yyyy";
	private static final String DATE_YEAR_MONTH_FORMAT = "dd-MMM-yy";

	private static final String DATE_FORMAT_PRODUCTION_DEFECT_AGEING = "yyyy-MM-dd";
	private static final String LEAD_TIME = "Lead Time";
	private static final String INTAKE_TO_DOR = "Intake - DoR";
	private static final String DOR_TO_DOD = "DoR - DoD";
	private static final String DOD_TO_LIVE = "DoD - Live";

	private KPIExcelUtility() {
	}

	/**
	 * This method populate the excel data for DIR KPI
	 *
	 * @param sprint
	 * @param storyIds
	 * @param defects
	 * @param kpiExcelData
	 * @param issueData
	 */
	public static void populateDirOrDensityExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
		storyIds.forEach(story -> {
			Map<String, String> linkedDefects = new HashMap<>();
			defects.stream().filter(d -> d.getDefectStoryID().contains(story))
					.forEach(defect -> linkedDefects.putIfAbsent(defect.getNumber(), checkEmptyURL(defect)));
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			excelData.setLinkedDefects(linkedDefects);
			if (MapUtils.isNotEmpty(issueData)) {
				JiraIssue jiraIssue = issueData.get(story);
				if (null != jiraIssue) {
					excelData.setIssueDesc(checkEmptyName(jiraIssue));
					Map<String, String> storyId = new HashMap<>();
					storyId.put(story, checkEmptyURL(jiraIssue));
					excelData.setStoryId(storyId);
				}
			}
			kpiExcelData.add(excelData);
		});
	}

	public static void populateFTPRExcelData(String sprint, List<String> storyIds, List<JiraIssue> ftprStories,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
		List<String> collect = ftprStories.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
		storyIds.forEach(story -> {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			if (MapUtils.isNotEmpty(issueData)) {
				JiraIssue jiraIssue = issueData.get(story);
				if (null != jiraIssue) {
					excelData.setIssueDesc(checkEmptyName(jiraIssue));
					Map<String, String> storyId = new HashMap<>();
					storyId.put(story, checkEmptyURL(jiraIssue));
					excelData.setStoryId(storyId);
				}
			}
			excelData.setFirstTimePass(collect.contains(story) ? Constant.EXCEL_YES : Constant.EMPTY_STRING);
			kpiExcelData.add(excelData);
		});
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else "N" kpi specific
	 *
	 * @param sprint
	 * @param totalBugList
	 * @param conditionDefects
	 * @param kpiExcelData
	 * @param kpiId
	 */
	public static void populateDefectRelatedExcelData(String sprint, Map<String, JiraIssue> totalBugList,
			List<JiraIssue> conditionDefects, List<KPIExcelData> kpiExcelData, String kpiId) {

		if (MapUtils.isNotEmpty(totalBugList)) {
			List<String> conditionalList = conditionDefects.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalBugList.forEach((defectId, jiraIssue) -> {
				String present = conditionalList.contains(defectId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> defectIdDetails = new HashMap<>();
				defectIdDetails.put(defectId, checkEmptyURL(jiraIssue));
				excelData.setDefectId(defectIdDetails);
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId())) {
					excelData.setRemovedDefect(present);
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_SEEPAGE_RATE.getKpiId())) {
					excelData.setEscapedDefect(present);
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_REJECTION_RATE.getKpiId())) {
					excelData.setRejectedDefect(present);
				}

				kpiExcelData.add(excelData);
			});
		}
	}

	/**
	 * to get direct related values of a jira issue like priority/RCA from total
	 * list
	 *
	 * @param sprint
	 * @param jiraIssues
	 * @param kpiExcelData
	 * @param kpiId
	 */
	public static void populateDefectRelatedExcelData(String sprint, List<JiraIssue> jiraIssues,
			List<KPIExcelData> kpiExcelData, String kpiId) {
		jiraIssues.stream().forEach(jiraIssue -> {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			excelData.setIssueDesc(checkEmptyName(jiraIssue));
			Map<String, String> defectIdDetails = new HashMap<>();
			defectIdDetails.put(jiraIssue.getNumber(), checkEmptyURL(jiraIssue));
			excelData.setDefectId(defectIdDetails);
			if (kpiId.equalsIgnoreCase(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId())) {
				excelData.setPriority(jiraIssue.getPriority());
			}
			if (kpiId.equalsIgnoreCase(KPICode.DEFECT_COUNT_BY_RCA.getKpiId())) {
				excelData.setRootCause(jiraIssue.getRootCauseList());
			}

			kpiExcelData.add(excelData);
		});
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else
	 * Constant.EMPTY_STRING kpi specific
	 *
	 * @param sprint
	 * @param totalStoriesMap
	 * @param conditionStories
	 * @param kpiExcelData
	 */
	public static void populateCreatedVsResolvedExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<JiraIssue> conditionStories, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setCreatedDefectId(storyDetails);
				excelData.setResolvedTickets(present);

				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateRegressionAutomationExcelData(String sprintProject,
			Map<String, TestCaseDetails> totalStoriesMap, List<TestCaseDetails> conditionStories,
			List<KPIExcelData> kpiExcelData, String kpiId) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> conditionalList = conditionStories.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				if (kpiId.equalsIgnoreCase(KPICode.REGRESSION_AUTOMATION_COVERAGE.getKpiId())) {
					excelData.setSprintName(sprintProject);
				} else {
					excelData.setProject(sprintProject);
				}
				excelData.setTestCaseId(storyId);
				excelData.setAutomated(present);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateSonarKpisExcelData(String projectName, List<String> jobList,
			List<String> kpiSpecificDataList, List<String> versionDate, List<KPIExcelData> kpiExcelData, String kpiId) {
		if (CollectionUtils.isNotEmpty(jobList)) {
			for (int i = 0; i < jobList.size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				excelData.setJobName(jobList.get(i));
				if (kpiId.equalsIgnoreCase(KPICode.UNIT_TEST_COVERAGE.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId())) {
					excelData.setUnitCoverage(kpiSpecificDataList.get(i));
				}
				if (kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId())) {
					excelData.setTechDebt(kpiSpecificDataList.get(i));
				}
				if (kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS.getKpiId())
						|| kpiId.equalsIgnoreCase(KPICode.SONAR_VIOLATIONS_KANBAN.getKpiId())) {
					excelData.setSonarViolation(kpiSpecificDataList.get(i));
				}
				excelData.setWeeks(versionDate.get(i));
				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateInSprintAutomationExcelData(String sprint, List<TestCaseDetails> allTestList,
			List<TestCaseDetails> automatedList, Set<JiraIssue> linkedStories, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(allTestList)) {
			List<String> conditionalList = automatedList.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			allTestList.forEach(testIssue -> {
				String present = conditionalList.contains(testIssue.getNumber()) ? Constant.EXCEL_YES
						: Constant.EMPTY_STRING;
				Map<String, String> linkedStoriesMap = new HashMap<>();
				linkedStories.stream().filter(story -> testIssue.getDefectStoryID().contains(story.getNumber()))
						.forEach(story -> linkedStoriesMap.putIfAbsent(story.getNumber(), checkEmptyURL(story)));

				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setTestCaseId(testIssue.getNumber());
				excelData.setLinkedStory(linkedStoriesMap);
				excelData.setAutomated(present);
				kpiExcelData.add(excelData);
			});
		}

	}

	private static String checkEmptyName(Object object) {
		String description = "";
		if (object instanceof JiraIssue) {
			JiraIssue jiraIssue = (JiraIssue) object;
			description = StringUtils.isEmpty(jiraIssue.getName()) ? Constant.EMPTY_STRING : jiraIssue.getName();
		}
		if (object instanceof KanbanJiraIssue) {
			KanbanJiraIssue jiraIssue = (KanbanJiraIssue) object;
			description = StringUtils.isEmpty(jiraIssue.getName()) ? Constant.EMPTY_STRING : jiraIssue.getName();
		}
		return description;
	}

	private static String checkEmptyURL(Object object) {
		String url = "";
		if (object instanceof JiraIssue) {
			JiraIssue jiraIssue = (JiraIssue) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof KanbanJiraIssue) {
			KanbanJiraIssue jiraIssue = (KanbanJiraIssue) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}
		if (object instanceof KanbanIssueCustomHistory) {
			KanbanIssueCustomHistory jiraIssue = (KanbanIssueCustomHistory) object;
			url = StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
		}

		return url;

	}

	public static void populateChangeFailureRateExcelData(String projectName,
			ChangeFailureRateInfo changeFailureRateInfo, List<KPIExcelData> kpiExcelData) {
		List<String> buildJobNameList = changeFailureRateInfo.getBuildJobNameList();
		if (CollectionUtils.isNotEmpty(buildJobNameList)) {
			for (int i = 0; i < changeFailureRateInfo.getBuildJobNameList().size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				excelData.setJobName(buildJobNameList.get(i));
				excelData.setWeeks(changeFailureRateInfo.getDateList().get(i));
				excelData.setBuildCount(changeFailureRateInfo.getTotalBuildCountList().get(i).toString());
				excelData.setBuildFailureCount(changeFailureRateInfo.getTotalBuildFailureCountList().get(i).toString());
				excelData.setBuildFailurePercentage(
						changeFailureRateInfo.getBuildFailurePercentageList().get(i).toString());
				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateTestExcecutionExcelData(String sprintProjectName, TestExecution testDetail,
			KanbanTestExecution kanbanTestExecution, double executionPercentage, double passPercentage,
			List<KPIExcelData> kpiExcelData) {

		if (testDetail != null) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprintProjectName);
			excelData.setTotalTest(testDetail.getTotalTestCases().toString());
			excelData.setExecutedTest(testDetail.getExecutedTestCase().toString());
			excelData.setExecutionPercentage(String.valueOf(executionPercentage));
			excelData.setPassedTest(testDetail.getPassedTestCase().toString());
			excelData.setPassedPercentage(String.valueOf(passPercentage));
			kpiExcelData.add(excelData);
		}
		if (kanbanTestExecution != null) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProject(sprintProjectName);
			excelData.setTotalTest(kanbanTestExecution.getTotalTestCases().toString());
			excelData.setExecutedTest(kanbanTestExecution.getExecutedTestCase().toString());
			excelData.setExecutionPercentage(String.valueOf(executionPercentage));
			excelData.setPassedTest(kanbanTestExecution.getPassedTestCase().toString());
			excelData.setPassedPercentage(String.valueOf(passPercentage));
			excelData.setExecutionDate(kanbanTestExecution.getExecutionDate());
			kpiExcelData.add(excelData);
		}
	}

	public static void populateSprintVelocity(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<KPIExcelData> kpiExcelData) {

		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			totalStoriesMap.forEach((storyId, jiraIssue) -> {

				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, jiraIssue.getUrl());
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(jiraIssue.getName());
				excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateSprintCapacity(String sprint, List<JiraIssue> totalStoriesList,
			List<String> loggedTimeList, List<KPIExcelData> kpiExcelData, List<String> estimateTimeList) {

		if (CollectionUtils.isNotEmpty(totalStoriesList)) {
			for (int i = 0; i < totalStoriesList.size(); i++) {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(totalStoriesList.get(i).getNumber(), checkEmptyURL(totalStoriesList.get(i)));
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(checkEmptyName(totalStoriesList.get(i)));
				excelData.setOriginalTimeEstimate(estimateTimeList.get(0));
				excelData.setTotalTimeSpent(loggedTimeList.get(i));
				kpiExcelData.add(excelData);
			}
		}
	}

	public static void populateAverageResolutionTime(Map<String, List<ResolutionTimeValidation>> sprintWiseResolution,
			List<KPIExcelData> kpiExcelData) {

		if (MapUtils.isNotEmpty(sprintWiseResolution)) {
			sprintWiseResolution.forEach((sprint, resolutionTimesValidationList) -> {
				resolutionTimesValidationList.stream().forEach(resolutionTimeValidation -> {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setSprintName(sprint);
					Map<String, String> storyDetails = new HashMap<>();
					storyDetails.put(resolutionTimeValidation.getIssueNumber(), resolutionTimeValidation.getUrl());
					excelData.setStoryId(storyDetails);
					excelData.setIssueDesc(resolutionTimeValidation.getIssueDescription());
					excelData.setIssueType(resolutionTimeValidation.getIssueType());
					excelData.setResolutionTime(resolutionTimeValidation.getResolutionTime().toString());
					kpiExcelData.add(excelData);
				});

			});
		}
	}

	public static void populateSprintCountExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<KPIExcelData> kpiExcelData) {

		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			totalStoriesMap.forEach((storyId, jiraIssue) -> {

				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, jiraIssue.getUrl());
				excelData.setStoryId(storyDetails);
				excelData.setIssueDesc(jiraIssue.getName());

				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateLeadTime(List<KPIExcelData> kpiExcelData, String projectName,
			Map<String, Long> cycleMap) {

		if (MapUtils.isNotEmpty(cycleMap)) {

			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setIntakeToDOR(cycleMap.get(INTAKE_TO_DOR).toString());
			excelData.setDorToDod(cycleMap.get(DOR_TO_DOD).toString());
			excelData.setDodToLive(cycleMap.get(DOD_TO_LIVE).toString());
			excelData.setLeadTime(cycleMap.get(LEAD_TIME).toString());
			kpiExcelData.add(excelData);

		}
	}

	/**
	 * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
	 * present in conditional list then Constant.EXCEL_YES else
	 * Constant.EMPTY_STRING kpi specific
	 *
	 * @param sprint
	 * @param totalStoriesMap
	 * @param conditionStories
	 * @param kpiExcelData
	 */

	public static void populateCommittmentReliability(String sprint, Map<String, JiraIssue> totalStoriesMap,
			List<JiraIssue> conditionStories, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalStoriesMap)) {
			List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toList());
			totalStoriesMap.forEach((storyId, jiraIssue) -> {
				String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				excelData.setIssueDesc(checkEmptyName(jiraIssue));
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(storyId, checkEmptyURL(jiraIssue));
				excelData.setStoryId(storyDetails);
				excelData.setStatus(present);

				kpiExcelData.add(excelData);

			});
		}
	}

	public static void populateCODExcelData(String projectName, List<JiraIssue> epicList,
			List<KPIExcelData> kpiExcelData) {

		epicList.forEach(epic -> {
			if (null != epic) {
				Map<String, String> epicLink = new HashMap<>();
				epicLink.put(epic.getNumber(), checkEmptyURL(epic));
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setEpicID(epicLink);
				excelData.setEpicName(checkEmptyName(epic));
				excelData.setCostOfDelay(epic.getCostOfDelay());
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
						.optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
						.optionalEnd().toFormatter();
				LocalDateTime dateTime = LocalDateTime.parse(epic.getChangeDate(), formatter);
				excelData.setMonth(dateTime.format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT)));
				excelData.setEpicEndDate(dateTime.format(DateTimeFormatter.ofPattern(DATE_YEAR_MONTH_FORMAT)));
				kpiExcelData.add(excelData);
			}
		});
	}

	public static void populateKanbanCODExcelData(String projectName, List<KanbanJiraIssue> epicList,
			List<KPIExcelData> kpiExcelData) {

		epicList.forEach(epic -> {
			if (!epic.getProjectName().isEmpty()) {
				Map<String, String> epicLink = new HashMap<>();
				epicLink.put(epic.getNumber(), epic.getUrl());
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setEpicID(epicLink);
				excelData.setEpicName(epic.getName());
				excelData.setCostOfDelay(epic.getCostOfDelay());
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
						.optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
						.optionalEnd().toFormatter();
				LocalDateTime dateTime = LocalDateTime.parse(epic.getChangeDate(), formatter);
				excelData.setMonth(dateTime.format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT)));
				excelData.setEpicEndDate(dateTime.toString());
				kpiExcelData.add(excelData);
			}
		});
	}

	public static void populateReleaseFreqExcelData(List<ProjectVersion> projectVersionList, String projectName,
			List<KPIExcelData> kpiExcelData) {

		projectVersionList.forEach(pv -> {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setReleaseName(pv.getDescription());
			excelData.setReleaseDesc(pv.getName());
			excelData.setReleaseEndDate(pv.getReleaseDate().toString(DATE_YEAR_MONTH_FORMAT));
			excelData.setMonth(pv.getReleaseDate().toString(MONTH_YEAR_FORMAT));
			kpiExcelData.add(excelData);

		});

	}

	public static void populateDeploymentFrequencyExcelData(String projectName, DeploymentFrequencyInfo dfi,
			List<KPIExcelData> kpiExcelData) {

		for (int i = 0; i < dfi.getJobNameList().size(); i++) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setDate(dfi.getDeploymentDateList().get(i));
			excelData.setJobName(dfi.getJobNameList().get(i));
			excelData.setMonth(dfi.getMonthList().get(i));
			excelData.setDeploymentEnvironment(dfi.getEnvironmentList().get(i));
			excelData.setMonth(dfi.getMonthList().get(i));
			kpiExcelData.add(excelData);

		}

	}

	public static void populateDefectWithoutIssueLinkExcelData(List<JiraIssue> defectWithoutStory,
			List<KPIExcelData> kpiExcelData, String sprintName) {

		defectWithoutStory.forEach(defect -> {
			if (null != defect) {
				KPIExcelData excelData = new KPIExcelData();
				Map<String, String> defectLink = new HashMap<>();
				defectLink.put(defect.getNumber(), checkEmptyURL(defect));
				excelData.setSprintName(sprintName);
				excelData.setDefectWithoutStoryLink(defectLink);
				excelData.setPriority(defect.getPriority());
				kpiExcelData.add(excelData);
			}
		});

	}

	public static void populateTestWithoutStoryExcelData(String projectName, Map<String, TestCaseDetails> totalTestMap,
			List<TestCaseDetails> testWithoutStory, List<KPIExcelData> kpiExcelData) {
		if (MapUtils.isNotEmpty(totalTestMap)) {
			List<String> testWithoutStoryIdList = testWithoutStory.stream().map(TestCaseDetails::getNumber)
					.collect(Collectors.toList());
			totalTestMap.forEach((testId, testCaseDetails) -> {
				String isDefectPresent = testWithoutStoryIdList.contains(testId) ? Constant.EMPTY_STRING
						: Constant.EXCEL_YES;
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setTestCaseId(testId);
				excelData.setIsTestLinkedToStory(isDefectPresent);
				kpiExcelData.add(excelData);
			});
		}
	}

	public static void populateProductionDefectAgingExcelData(String projectName, List<JiraIssue> defectList,
			List<KPIExcelData> kpiExcelData) {
		defectList.forEach(defect -> {
			KPIExcelData excelData = new KPIExcelData();
			Map<String, String> defectLink = new HashMap<>();
			defectLink.put(defect.getNumber(), checkEmptyURL(defect));
			excelData.setProjectName(projectName);
			excelData.setDefectId(defectLink);
			excelData.setPriority(defect.getPriority());
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT)
					.optionalStart().appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
					.optionalEnd().toFormatter();
			LocalDateTime dateTime = LocalDateTime.parse(defect.getCreatedDate(), formatter);
			excelData.setDate(dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PRODUCTION_DEFECT_AGEING)));
			excelData.setIssueStatus(defect.getJiraStatus());
			kpiExcelData.add(excelData);
		});
	}

	public static void populateOpenTicketByAgeingExcelData(String projectName, List<KanbanJiraIssue> kanbanJiraIssues,
			List<KPIExcelData> kpiExcelData) {
		kanbanJiraIssues.forEach(kanbanIssues -> {
			KPIExcelData excelData = new KPIExcelData();
			Map<String, String> storyMap = new HashMap<>();
			storyMap.put(kanbanIssues.getNumber(), checkEmptyURL(kanbanIssues));
			excelData.setProject(projectName);
			excelData.setTicketIssue(storyMap);
			excelData.setPriority(kanbanIssues.getPriority());
			excelData.setCreatedDate(LocalDate.parse(kanbanIssues.getCreatedDate().split("T")[0]).toString());
			excelData.setIssueStatus(kanbanIssues.getJiraStatus());
			kpiExcelData.add(excelData);
		});
	}

	/**
	 * prepare data for excel for cumulative kpi of Kanban on the basis of field.
	 * field can be RCA/priority/status field values as per field of jira
	 * 
	 * @param projectName
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldValues
	 * @param kanbanJiraIssues
	 * @param excelDataList
	 * @param kpiId
	 */
	public static void prepareExcelForKanbanCumulativeDataMap(String projectName,
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, Set<String> fieldValues,
			Set<KanbanIssueCustomHistory> kanbanJiraIssues, List<KPIExcelData> excelDataList, String date,
			String kpiId) {

		Map<String, Set<String>> fieldWiseIssuesLatestMap = filterKanbanDataBasedOnFieldLatestCumulativeData(
				jiraHistoryFieldAndDateWiseIssueMap, fieldValues);

		Map<String, Set<String>> fieldWiseIssues = fieldWiseIssuesLatestMap.entrySet().stream()
				.sorted((i1, i2) -> i1.getKey().compareTo(i2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		fieldWiseIssues.entrySet().forEach(dateSet -> {
			String field = dateSet.getKey();
			dateSet.getValue().stream().forEach(values -> {
				KanbanIssueCustomHistory kanbanJiraIssue = kanbanJiraIssues.stream()
						.filter(issue -> issue.getStoryID().equalsIgnoreCase(values)).findFirst().get();
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProject(projectName);
				Map<String, String> ticketMap = new HashMap<>();
				ticketMap.put(kanbanJiraIssue.getStoryID(), checkEmptyURL(kanbanJiraIssue));
				excelData.setTicketIssue(ticketMap);
				if (kpiId.equalsIgnoreCase(KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.getKpiId())) {
					excelData.setIssueStatus(field);
				}
				if (kpiId.equalsIgnoreCase(KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.getKpiId())) {
					excelData.setRootCause(Arrays.asList(field));
				}
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_COUNT_BY_PRIORITY.getKpiId())) {
					excelData.setPriority(field);
				}
				excelData.setCreatedDate(LocalDate.parse(kanbanJiraIssue.getCreatedDate().split("T")[0]).toString());
				excelData.setDayWeekMonth(date);
				excelDataList.add(excelData);
			});
		});

	}

	/**
	 * prepare excel data only Today Cumulative data so that only latest data values
	 * of field(status/rca/priority)
	 *
	 * @param jiraHistoryFieldAndDateWiseIssueMap
	 * @param fieldValues
	 * @return
	 */
	private static Map<String, Set<String>> filterKanbanDataBasedOnFieldLatestCumulativeData(
			Map<String, Map<String, Set<String>>> jiraHistoryFieldAndDateWiseIssueMap, Set<String> fieldValues) {
		String date = LocalDate.now().toString();
		Map<String, Set<String>> fieldWiseIssuesLatestMap = new HashMap<>();
		fieldValues.forEach(field -> {
			Set<String> ids = jiraHistoryFieldAndDateWiseIssueMap.get(field).getOrDefault(date, new HashSet<>())
					.stream().filter(Objects::nonNull).collect(Collectors.toSet());
			fieldWiseIssuesLatestMap.put(field, ids);
		});
		return fieldWiseIssuesLatestMap;
	}

	public static void populateTicketOpenVsClosedExcelData(String projectName, List<KanbanJiraIssue> kanbanJiraIssues,
			List<KPIExcelData> kpiExcelData) {
		kanbanJiraIssues.forEach(kanbanIssues -> {
			KPIExcelData excelData = new KPIExcelData();
			Map<String, String> storyMap = new HashMap<>();
			storyMap.put(kanbanIssues.getNumber(), checkEmptyURL(kanbanIssues));
			excelData.setProject(projectName);
			excelData.setTicketIssue(storyMap);
			excelData.setPriority(kanbanIssues.getPriority());
			excelData.setCreatedDate(LocalDate.parse(kanbanIssues.getCreatedDate().split("T")[0]).toString());
			excelData.setIssueStatus(kanbanIssues.getJiraStatus());
			kpiExcelData.add(excelData);
		});
	}

	public static void populateOpenVsClosedExcelData(String date, String projectName,
			List<KanbanJiraIssue> dateWiseIssueTypeList, List<KanbanIssueCustomHistory> dateWiseIssueClosedStatusList,
			List<KPIExcelData> excelDataList, String kpiId) {
		if (CollectionUtils.isNotEmpty(dateWiseIssueTypeList)
				|| CollectionUtils.isNotEmpty(dateWiseIssueClosedStatusList)) {
			dateWiseIssueTypeList.forEach(issue -> {
				KPIExcelData kpiExcelDataObject = new KPIExcelData();
				kpiExcelDataObject.setProject(projectName);
				kpiExcelDataObject.setDayWeekMonth(date);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issue.getNumber(), issue.getUrl());
				kpiExcelDataObject.setTicketIssue(storyDetails);
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.getKpiId())) {
					kpiExcelDataObject.setIssueType(issue.getTypeName());
				} //
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId())) {
					kpiExcelDataObject.setIssuePriority(issue.getPriority());
				}
				kpiExcelDataObject.setIssueStatus("Open");
				excelDataList.add(kpiExcelDataObject);
			});

			dateWiseIssueClosedStatusList.forEach(issue -> {
				KPIExcelData kpiExcelDataObject = new KPIExcelData();
				kpiExcelDataObject.setProject(projectName);
				kpiExcelDataObject.setDayWeekMonth(date);
				Map<String, String> storyDetails = new HashMap<>();
				storyDetails.put(issue.getStoryID(), issue.getUrl());
				kpiExcelDataObject.setTicketIssue(storyDetails);
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.getKpiId())) {
					kpiExcelDataObject.setIssueType(issue.getStoryType());
				}
				if (kpiId.equalsIgnoreCase(KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId())) {
					kpiExcelDataObject.setIssuePriority(issue.getPriority());
				}
				kpiExcelDataObject.setIssueStatus("Closed");
				excelDataList.add(kpiExcelDataObject);
			});
		}
	}

}