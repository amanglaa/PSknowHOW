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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumns;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KPIExcelResponse;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

	private KPIExcelUtility() {
	}

	private static final Map<String, List<KPIExcelColumns>> kpiWiseExcelColumns = new HashMap<>(
			Map.of(KPICode.DEFECT_INJECTION_RATE.name(),
					Arrays.asList(KPIExcelColumns.sprintName, KPIExcelColumns.storyId, KPIExcelColumns.linkedDefects)

			));

	/*
	 * This method returns list of columns enums
	 * 
	 * @param kpiCode
	 * 
	 * @return list of enum
	 */
	public static List<KPIExcelColumns> getExcelColumns(String kpiCode) {
		return kpiWiseExcelColumns.get(kpiCode);
	}

	/**
	 * This method populate the excel data for DIR KPI
	 * 
	 * @param kpiName
	 * @param storyIds
	 * @param defects
	 * 
	 */
	public static void populateDirExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
			List<KPIExcelData> kpiExcelData) {
		storyIds.forEach(story -> {
			Map<String, String> linkedDefects = defects.stream().filter(d -> d.getDefectStoryID().contains(story))
					.collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getUrl));
			KPIExcelData excelData = new KPIExcelData();
			excelData.setSprintName(sprint);
			excelData.setStoryId(story);
			excelData.setLinkedDefects(linkedDefects);
			kpiExcelData.add(excelData);
		});
	}

	public static KPIExcelResponse createKPIExcelResponse(String kpiName, List<KPIExcelData> excelData) {
		KPIExcelResponse excelResponse = new KPIExcelResponse();

		return excelResponse;
	}
}