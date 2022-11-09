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
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * This class calculates the DRR and trend analysis of the DRR.
 * 
 * @author pkum34
 *
 */
@Component
@Slf4j
public class SprintVelocityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SEPARATOR_ASTERISK = "*************************************";
	private static final String SPRINTVELOCITYKEY = "sprintVelocityKey";
	private static final String SPRINT_WISE_SPRINTDETAILS = "sprintWiseSprintDetailMap";
	private static final String PROJECT_WISE_CLOSED_STATUS_MAP = "projectWiseClosedStatusMap";
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
		setDbQueryLogger((List<JiraIssue>) resultListMap.get(SPRINTVELOCITYKEY));
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
		Map<String, List<String>> closedStatusMap = (Map<String, List<String>>) sprintVelocityStoryMap.get(PROJECT_WISE_CLOSED_STATUS_MAP);

		if(CollectionUtils.isNotEmpty(allJiraIssue)) {
			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintDetails.forEach(sd -> {
					List<String> closedStatus = closedStatusMap.getOrDefault(sd.getBasicProjectConfigId().toString(),
							new ArrayList<>());
					Map<String, Double> totalIssues = new HashMap<>();
					sd.getTotalIssues().stream().forEach(sprintIssue -> {
						if (closedStatus.contains(sprintIssue.getStatus())) {
							totalIssues.putIfAbsent(sprintIssue.getNumber(), sprintIssue.getStoryPoints());
						}
					});
					List<JiraIssue> sprintIssues = allJiraIssue.stream()
							.filter(element -> totalIssues.containsKey(element.getNumber()))
							.collect(Collectors.toList());
					sprintWiseIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							sprintIssues);
					sprintWiseEstimate.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							totalIssues);

				});
			} else {
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
			// end : for azure board sprint details collections empty so that we have to prepare data from jira issue.
		}

		Map<String, ValidationData> validationDataMap = new HashMap<>();
		sprintLeafNodeList.forEach(node -> {
			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			Map<String, List<JiraIssue>> currentSprintLeafVelocityMap = new HashMap<>();
			double sprintVelocityForCurrentLeaf = 0.0d;
			if (CollectionUtils.isNotEmpty(sprintWiseIssues.get(currentNodeIdentifier))) {
				List<JiraIssue> sprintJiraIssues = sprintWiseIssues.get(currentNodeIdentifier);
				sprintVelocityForCurrentLeaf = calculateSprintValue(sprintWiseEstimate, currentNodeIdentifier, sprintJiraIssues);
				populateValidationDataObject(kpiElement, requestTrackerId, validationDataMap, sprintJiraIssues, node,sprintWiseEstimate,currentNodeIdentifier);
			}

			setSprintWiseLogger(node.getSprintFilter().getName(), currentSprintLeafVelocityMap.get(SPRINTVELOCITYKEY),
					sprintVelocityForCurrentLeaf);

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
	}

	private double calculateSprintValue(Map<Pair<String, String>, Map<String, Double>> sprintWiseEstimate, Pair<String, String> currentNodeIdentifier, List<JiraIssue> sprintJiraIssues) {
		double sprintVelocityForCurrentLeaf = 0.0;
		if (MapUtils.isNotEmpty(sprintWiseEstimate)
				&& MapUtils.isNotEmpty(sprintWiseEstimate.get(currentNodeIdentifier))) {
			Map<String, Double> stringDoubleMap = sprintWiseEstimate.get(currentNodeIdentifier);
			for (JiraIssue jiraIssue : sprintJiraIssues) {
				sprintVelocityForCurrentLeaf = sprintVelocityForCurrentLeaf
						+ Optional.ofNullable(stringDoubleMap.get(jiraIssue.getNumber())).orElse(0.0);
			}
		} else {
			sprintVelocityForCurrentLeaf = sprintJiraIssues.stream().mapToDouble(ji -> Double.valueOf(ji.getEstimate()))
					.sum();
		}
		return sprintVelocityForCurrentLeaf;
	}

	/**
	 * Populates Validation Data Object
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param validationDataMap
	 * @param jiraIssues
	 * @param node
	 * @param sprintWiseEstimate
	 * @param currentNodeIdentifier
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
											  Map<String, ValidationData> validationDataMap, List<JiraIssue> jiraIssues,
											  Node node, Map<Pair<String, String>, Map<String, Double>> sprintWiseEstimate, Pair<String, String> currentNodeIdentifier) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			List<String> defectKeyList = new ArrayList<>();
			List<String> storyPointList = new ArrayList<>();

			if(MapUtils.isNotEmpty(sprintWiseEstimate) && MapUtils.isNotEmpty(sprintWiseEstimate.get(currentNodeIdentifier))){
				Map<String, Double> stringDoubleMap = sprintWiseEstimate.get(currentNodeIdentifier);
				for (JiraIssue jiraIssue : jiraIssues) {
					defectKeyList.add(jiraIssue.getNumber());
					storyPointList.add(Optional.ofNullable(stringDoubleMap.get(jiraIssue.getNumber())).orElse(0.0).toString());
				}
			}
			else {
				for (JiraIssue jiraIssue : jiraIssues) {
					defectKeyList.add(jiraIssue.getNumber());
					storyPointList.add(jiraIssue.getEstimate());
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

	/**
	 * Sets DB query Logger
	 * 
	 * @param jiraIssues
	 */
	private void setDbQueryLogger(List<JiraIssue> jiraIssues) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* Sprint Velocity (dB) *******************");
			if (null != jiraIssues && !jiraIssues.isEmpty()) {
				List<String> storyIdList = jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList());
				log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			}
			log.info(SEPARATOR_ASTERISK);
			log.info("******************X----X*******************");
		}
	}

	/**
	 * Sets Sprint wise Logger
	 * 
	 * @param sprint
	 * @param storyFeatureList
	 * @param sprintVelocity
	 */
	private void setSprintWiseLogger(String sprint, List<JiraIssue> storyFeatureList, Double sprintVelocity) {

		if (customApiConfig.getApplicationDetailedLogger().equalsIgnoreCase("on")) {
			log.info(SEPARATOR_ASTERISK);
			log.info("************* SPRINT WISE Sprint Velocity *******************");
			log.info("Sprint: {}", sprint);
			if (null != storyFeatureList && !storyFeatureList.isEmpty()) {
				List<String> storyIdList = storyFeatureList.stream().map(JiraIssue::getNumber)
						.collect(Collectors.toList());
				log.info("Story[{}]: {}", storyIdList.size(), storyIdList);
			}
			log.info("Sprint Velocity: {}", sprintVelocity);
			log.info(SEPARATOR_ASTERISK);
			log.info(SEPARATOR_ASTERISK);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
