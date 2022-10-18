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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Service
@Slf4j
public class CostOfDelayServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	private static final String COD_DATA = "costOfDelayData";

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}
	
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, getRequestTrackerId(), kpiRequest);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.COST_OF_DELAY);
		// 3rd change : remove code to set trendValuelist and call
		// getTrendValues method
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue,KPICode.COST_OF_DELAY);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {

			leafNodeList.forEach(leaf -> basicProjectConfigIds.add(leaf.getProjectFilter().getBasicProjectConfigId().toString()));
		}

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.ISSUE_TYPE.getValue()));
		mapOfFilters.put(JiraFeature.STATUS.getFieldValueInFeature(), Arrays.asList(NormalizedJira.STATUS.getValue()));
		List<JiraIssue> codList = jiraIssueRepository.findCostOfDelayByType(mapOfFilters);
		resultListMap.put(COD_DATA, codList);
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.COST_OF_DELAY.name();
	}

	
	/**
	 * Calculate KPI value for selected project nodes.
	 *
	 * @param projectLeafNodeList
	 *            list of sprint leaf nodes
	 * @param trendValueList
	 *            list containing data to show on KPI
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, String requestTrackerId, KpiRequest kpiRequest) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, kpiRequest);
		Map<String, List<JiraIssue>> filterWiseDataMap = createProjectWiseDelay(
				(List<JiraIssue>) resultMap.get(COD_DATA));

		Map<String, ValidationData> validationDataMap = new HashMap<>();

		projectLeafNodeList.forEach(node -> {
			String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<JiraIssue> delayDetail = filterWiseDataMap.get(currentProjectId);
			if (CollectionUtils.isNotEmpty(delayDetail)) {
				setProjectNodeValue(mapTmp, node, kpiElement, delayDetail, trendValueList, requestTrackerId,
						validationDataMap);
			}

		});
	}

	/**
	 * Gets the KPI value for project node.
	 *
	 * @param kpiElement
	 * @param jiraIssues
	 * @param trendValueList
	 * @return
	 */
	private void setProjectNodeValue(Map<String, Node> mapTmp, Node node, KpiElement kpiElement,
			List<JiraIssue> jiraIssues, List<DataCount> trendValueList, String requestTrackerId,
			Map<String, ValidationData> validationDataMap) {
		Map<String, Double> lastNMonthMap = getLastNMonth(customApiConfig.getJiraXaxisMonthCount());
		String projectName = node.getProjectFilter().getName();
		List<JiraIssue> epicList = new ArrayList<>();
		List<String> dateList = new ArrayList<>();
		Map<String, Map<String, Integer>> howerMap = new HashMap<>();
		for (JiraIssue js : jiraIssues) {
			String number = js.getNumber();
			String dateTime = js.getChangeDate() == null ? js.getUpdateDate() : js.getChangeDate();
			if (dateTime != null) {
				DateTime dateValue = DateTime.parse(dateTime);
				epicList.add(js);
				String date = dateValue.getYear() + Constant.DASH + dateValue.getMonthOfYear();
				// dateValue.getM
				dateList.add(date);
				lastNMonthMap.computeIfPresent(date, (key, value) -> {
					Integer costOfDelay = (int) js.getCostOfDelay();
					Map<String, Integer> epicWiseCost = new HashMap<>();
					epicWiseCost.put(number, costOfDelay);
					if (howerMap.containsKey(date)) {
						epicWiseCost.putAll(howerMap.get(date));
						howerMap.put(date, epicWiseCost);
					} else {
						howerMap.put(date, epicWiseCost);
					}
					return value + costOfDelay;
				});

			}

		}

		List<DataCount> dcList = new ArrayList<>();
		lastNMonthMap.forEach((k, v) -> {
			DataCount dataCount = new DataCount();
			dataCount.setDate(k);
			dataCount.setValue(v);
			dataCount.setData(v.toString());
			dataCount.setSProjectName(projectName);
			dataCount.setHoverValue(new HashMap<>());
			dcList.add(dataCount);
			trendValueList.add(dataCount);

		});
		mapTmp.get(node.getId()).setValue(dcList);
		populateValidationDataObject(kpiElement, requestTrackerId, epicList, validationDataMap, projectName, dateList);
	}

	/**
	 * Group list of data by project.
	 *
	 * @param resultList
	 * @return
	 */

	private Map<String, List<JiraIssue>> createProjectWiseDelay(List<JiraIssue> resultList) {
		return resultList.stream().filter(p -> p.getBasicProjectConfigId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
	}

	/**
	 * This method check for API request source. If it is Excel it populates the
	 * validation data node of the KPI element.
	 *
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param validationDataMap
	 * @param validationKey
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId, List<JiraIssue> epicList,
			Map<String, ValidationData> validationDataMap, String validationKey, List<String> dateList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			List<Double> codValueList = new ArrayList<>();
			List<String> epicDateList = new ArrayList<>();
			List<String> epicIdList = new ArrayList<>();
			List<String> epicNameList = new ArrayList<>();
			epicList.forEach(el -> {
				codValueList.add(el.getCostOfDelay());
				epicDateList.add(el.getChangeDate());
				epicIdList.add(el.getNumber());
				epicNameList.add(el.getName());
			});

			ValidationData validationData = new ValidationData();
			validationData.setMonthList(dateList);
			validationData.setEpicIdList(epicIdList);
			validationData.setEpicNameList(epicNameList);
			validationData.setEpicEndDateList(epicDateList);
			validationData.setCostOfDelayList(codValueList);
			validationDataMap.put(validationKey, validationData);

			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
