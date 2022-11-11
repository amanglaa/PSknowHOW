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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 *
 * @author pkum34
 */
@Component
@Slf4j
public class SprintVelocityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String SPRINTVELOCITYKEY = "sprintVelocityKey";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String TOTAL_ISSUE_WITH_STORYPOINTS = "totalIssueWithStoryPoints";
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>SPRINT_VELOCITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_VELOCITY.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[SPRINT-VELOCITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_VELOCITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.SPRINT_VELOCITY);
		kpiElement.setTrendValueList(trendValues);
		log.debug("[SPRINT-VELOCITY-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = kpiHelperService.fetchSprintVelocityDataFromDb(leafNodeList, kpiRequest);
		return resultListMap;

	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param techDebtStoryMap
	 * @return Double
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> techDebtStoryMap) {

		String requestTrackerId = getRequestTrackerId();
		Double sprintVelocity = 0.0d;
		List<JiraIssue> sprintVelocityList = (List<JiraIssue>) techDebtStoryMap.get(SPRINTVELOCITYKEY);
		log.debug("[SPRINT-VELOCITY][{}]. Stories Count: {}", requestTrackerId, sprintVelocityList.size());
		for (JiraIssue jiraIssue : sprintVelocityList) {
			sprintVelocity = sprintVelocity + Double.valueOf(jiraIssue.getEstimate());
		}
		return sprintVelocity;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param trendValueList
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		Map<String, Object> sprintVelocityStoryMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) sprintVelocityStoryMap.get(SPRINTVELOCITYKEY);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues = new HashMap<>();
		Map<Pair<String, String>, Map<String,Double>> sprintWiseEstimate = new HashMap<>();

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintVelocityStoryMap.get(SPRINT_WISE_SPRINTDETAILS);

			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintDetails.forEach(sd -> {
					Map<Pair<String, String>, Map<String, Double>> sprintWiseNumberStoryMap = (Map<Pair<String, String>, Map<String, Double>>) sprintVelocityStoryMap.get(TOTAL_ISSUE_WITH_STORYPOINTS);
					Map<String, Double> storyWiseStoryPoint = sprintWiseNumberStoryMap.get((Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID())));
					sprintWiseEstimate.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							storyWiseStoryPoint);

				});
			} else {
				if(CollectionUtils.isNotEmpty(allJiraIssue)) {
				//start : for azure board sprint details collections empty so that we have to prepare data from jira issue
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> sprintWiseIssues
							.put(Pair.of(basicProjectConfigId, sprintId), sprintWiseIssuesList));
				});
			}
			// end : for azure board sprint details collections empty so that we have to
			// prepare data from jira issue.
		}

		 Map<String, ValidationData> validationDataMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			Map<String, List<JiraIssue>> currentSprintLeafVelocityMap = new HashMap<>();

			double sprintVelocityForCurrentLeaf = calculateSprintVelocityValue(sprintWiseEstimate, currentNodeIdentifier,
					sprintWiseIssues);
			populateValidationDataObject(kpiElement, requestTrackerId, validationDataMap, sprintWiseIssues, node,
					sprintWiseEstimate, currentNodeIdentifier);


			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(sprintVelocityForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(sprintVelocityForCurrentLeaf);
			dataCount.setHoverValue(new HashMap<>());
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_VELOCITY.getColumns());
	}

	private double calculateSprintVelocityValue(Map<Pair<String, String>, Map<String, Double>> sprintWiseEstimate, Pair<String, String> currentNodeIdentifier, Map<Pair<String, String>, List<JiraIssue>> sprintJiraIssues) {
		double sprintVelocityForCurrentLeaf = 0.0d;
		if (CollectionUtils.isNotEmpty(sprintJiraIssues.get(currentNodeIdentifier))) {
			List<JiraIssue> jiraIssueList = sprintJiraIssues.get(currentNodeIdentifier);
			sprintVelocityForCurrentLeaf = jiraIssueList.stream().mapToDouble(ji -> Double.valueOf(ji.getEstimate()))
					.sum();
		} else {
			if (MapUtils.isNotEmpty(sprintWiseEstimate.get(currentNodeIdentifier))) {
				Map<String, Double> storyWiseStoryPoints = sprintWiseEstimate.get(currentNodeIdentifier);
				for (Map.Entry<String, Double> map : storyWiseStoryPoints.entrySet()) {
					sprintVelocityForCurrentLeaf = sprintVelocityForCurrentLeaf
							+ Optional.ofNullable(map.getValue()).orElse(0.0d).doubleValue();
				}
			}

		}
		return sprintVelocityForCurrentLeaf;
	}

	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
											  Map<String, ValidationData> validationDataMap, Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues,
											  Node node, Map<Pair<String, String>, Map<String, Double>> sprintWiseEstimate, Pair<String, String> currentNodeIdentifier) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			List<String> defectKeyList = new ArrayList<>();
			List<String> storyPointList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(sprintWiseIssues.get(currentNodeIdentifier))) {
				List<JiraIssue> jiraIssues = sprintWiseIssues.get(currentNodeIdentifier);
				for (JiraIssue jiraIssue : jiraIssues) {
					defectKeyList.add(jiraIssue.getNumber());
					storyPointList.add(jiraIssue.getEstimate());
				}
			} else {
				if (MapUtils.isNotEmpty(sprintWiseEstimate)
						&& MapUtils.isNotEmpty(sprintWiseEstimate.get(currentNodeIdentifier))) {
					for (Map.Entry<String, Double> storyWiseStoryPointns : sprintWiseEstimate.get(currentNodeIdentifier).entrySet()) {
						defectKeyList.add(storyWiseStoryPointns.getKey());
						storyPointList.add(Optional.ofNullable(storyWiseStoryPointns.getValue()).orElse(0.0).toString());
					}

				}
			}

			String keyForValidation = node.getSprintFilter().getName();

			ValidationData validationData = new ValidationData();
			validationData.setStoryKeyList(defectKeyList);
			validationData.setStoryPointList(storyPointList);

			validationDataMap.put(keyForValidation, validationData);

			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
