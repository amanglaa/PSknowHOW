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

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import org.apache.commons.collections.MapUtils;

import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

	private KPIExcelUtility() {
	}

	/**
	 * This method populate the excel data for DIR KPI
	 *
	 * @param storyIds
	 * @param defects
	 * @param issueData
	 * 
	 */
	public static void populateDirExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
		storyIds.forEach(story -> {
			Map<String, String> linkedDefects = defects.stream().filter(d -> d.getDefectStoryID().contains(story))
					.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getUrl));
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			excelData.setLinkedDefects(linkedDefects);
			if (MapUtils.isNotEmpty(issueData)) {
				JiraIssue jiraIssue = issueData.get(story);
				if (null != jiraIssue) {
					excelData.setIssueDesc(jiraIssue.getName());
					Map<String, String> storyId = new HashMap<>();
					storyId.put(story, jiraIssue.getUrl());
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
					excelData.setIssueDesc(jiraIssue.getName());
					Map<String, String> storyId = new HashMap<>();
					storyId.put(story, jiraIssue.getUrl());
					excelData.setStoryId(storyId);
				}
			}
			excelData.setFirstTimePass(collect.contains(story) ? "Y" : "N");
			kpiExcelData.add(excelData);
		});
	}

	public static void populateDefectDensityExcelData(String sprint, List<String> storyIds, List<JiraIssue> ftprStories,
			List<KPIExcelData> kpiExcelData) {
		List<String> collect = ftprStories.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
		storyIds.forEach(story -> {
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			// excelData.setStoryId(story);
			excelData.setFirstTimePass(collect.contains(story) ? "Y" : "N");
			kpiExcelData.add(excelData);
		});
	}

	public static void populateDefectRelatedExcelData(String sprint, Map<String, JiraIssue> totalBugList,
													  List<JiraIssue> seepage, List<KPIExcelData> kpiExcelData, String kpiId) {
		List<String> conditionalList = seepage.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

		if (MapUtils.isNotEmpty(totalBugList)) {
			totalBugList.forEach((defectId, jiraIssue) -> {
				KPIExcelData excelData = new KPIExcelData();
				excelData.setSprintName(sprint);
				Map<String,String> defectIdDetails =new HashMap<>();
				defectIdDetails.put(defectId,jiraIssue.getUrl());
				excelData.setDefectId(defectIdDetails);
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId())){
					excelData.setRemovedDefect(conditionalList.contains(defectId) ? "Y" : "N");
				}
				if (kpiId.equalsIgnoreCase(KPICode.DEFECT_SEEPAGE_RATE.getKpiId())){
					excelData.setEscapedDefect(conditionalList.contains(defectId) ? "Y" : "N");
				}

				kpiExcelData.add(excelData);
			});
		}
	}

}