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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import org.apache.commons.collections.MapUtils;

import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

	private static final String MONTH_YEAR_FORMAT = "MMM yyyy";

	private static final String DATE_YEAR_MONTH_FORMAT = "dd-MMM-yy";

	private KPIExcelUtility() {
	}

	/**
	 * This method populate the excel data for DIR KPI
	 * @param sprint
	 * @param storyIds
	 * @param defects
	 * @param kpiExcelData
	 * @param issueData
	 */
	public static void populateDirExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData,Map<String,JiraIssue> issueData) {
		storyIds.forEach(story -> {
			Map<String, String> linkedDefects = defects.stream().filter(d -> d.getDefectStoryID().contains(story))
					.map(defect -> {
						if (StringUtils.isEmpty(defect.getUrl())) {
							defect.setUrl("");
						}
						return defect;
					}).collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getUrl));
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			excelData.setLinkedDefects(linkedDefects);
			if(MapUtils.isNotEmpty(issueData)) {
				JiraIssue jiraIssue=issueData.get(story);
				if(null!=jiraIssue) {
					excelData.setIssueDesc(jiraIssue.getName());
					Map<String,String> storyId=new HashMap<>();
					storyId.put(story, jiraIssue.getUrl());
					excelData.setStoryId(storyId);
				}
			}
			kpiExcelData.add(excelData);

		});
	}

	public static void populateCODExcelData(String projectName, List<JiraIssue> epicList, Map<String, String> dateList, List<KPIExcelData> kpiExcelData) {

		epicList.forEach(epic -> {
			if(null!=epic) {
				Map<String, String> epicLink = new HashMap<>();
				epicLink.put(epic.getNumber(), epic.getUrl());
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setEpicID(epicLink);
				excelData.setEpicName(epic.getName());
				excelData.setCostOfDelay(epic.getCostOfDelay());
				excelData.setMonth(dateList.get(epic.getNumber()));
				DateTime dateValue = DateTime.parse(epic.getChangeDate());
				excelData.setEpicEndDate(dateValue.toString(DATE_YEAR_MONTH_FORMAT));
				kpiExcelData.add(excelData);
			}
		});
	}

	public static void populateKanbanCODExcelData(String projectName, List<KanbanJiraIssue> epicList, List<KPIExcelData> kpiExcelData) {

		epicList.forEach(epic -> {
			if (!epic.getProjectName().isEmpty()) {
				Map<String, String> epicLink = new HashMap<>();
				epicLink.put(epic.getNumber(), epic.getUrl());
				KPIExcelData excelData = new KPIExcelData();
				excelData.setProjectName(projectName);
				excelData.setEpicID(epicLink);
				excelData.setEpicName(epic.getName());
				excelData.setCostOfDelay(epic.getCostOfDelay());
				DateTime dateValue = DateTime.parse(epic.getChangeDate());
				excelData.setMonth(dateValue.toString(MONTH_YEAR_FORMAT));
				excelData.setEpicEndDate(dateValue.toString(DATE_YEAR_MONTH_FORMAT));
				kpiExcelData.add(excelData);
			}
		});
	}

	public static void populateReleaseFreqExcelData(List<ProjectVersion> projectVersionList, String projectName, Map<Long, String> dateMap, List<KPIExcelData> kpiExcelData) {

		projectVersionList.forEach(pv -> {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setReleaseName(pv.getDescription());
			excelData.setReleaseDesc(pv.getName());
			excelData.setReleaseEndDate(pv.getReleaseDate().toString(DATE_YEAR_MONTH_FORMAT));
			excelData.setMonth(dateMap.get(pv.getId()));
			kpiExcelData.add(excelData);

		});

	}

	// incomplete for this pr
	public static void populateDeploymentFrequencyExcelData(String projectName, DeploymentFrequencyInfo dfi, List<KPIExcelData> kpiExcelData) {

		for (int i=0; i < dfi.getJobNameList().size(); i++) {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setProjectName(projectName);
			excelData.setDeploymentDate(dfi.getDeploymentDateList().get(i));
			excelData.setDeploymentJobName(dfi.getJobNameList().get(i));
			excelData.setMonth(dfi.getMonthList().get(i));
			excelData.setDeploymentEnvironment(dfi.getEnvironmentList().get(i));
			excelData.setMonth(dfi.getMonthList().get(i));
			kpiExcelData.add(excelData);

		}

	}

}