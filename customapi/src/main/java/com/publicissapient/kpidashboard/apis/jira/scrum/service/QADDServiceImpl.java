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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the QA Defect Density KPI and trend analysis of the
 * same.
 *
 * @author prasaxen3
 */
@Component
@Slf4j
public class QADDServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String STORY_DATA = "storyData";

	private static final String DEFECT_DATA = "defectData";

	private static final String STORY_POINTS_DATA = "Size of Closed Stories";

	private static final String STORY_POINTS = "storyPoints";

	private static final String DEFECT = "Defects";


	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.DEFECT_DENSITY.name();
	}

	/**
	 * Gets the kpi data.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @param kpiElement
	 *            the kpi element
	 * @param treeAggregatorDetail
	 *            the tree aggregator detail
	 * @return the kpi data
	 * @throws ApplicationException
	 *             the application exception
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

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_DENSITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue,KPICode.DEFECT_DENSITY);
		kpiElement.setTrendValueList(trendValues);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the
	 * trend analysis at sprint wise.
	 *
	 * @param mapTmp
	 *            node is map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend data
	 * @param kpiElement
	 *            KpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	public void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);
		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap.get(STORY_DATA);
		List<JiraIssue> storyFilteredList = (List<JiraIssue>) storyDefectDataListMap.get(STORY_POINTS);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<String, String> sprintIdSprintNameMap = sprintWiseStoryList.stream().collect(
				Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getSprintName, (name1, name2) -> name1));

		Map<String, Double> storyWithStoryPoint = storyFilteredList.stream().collect(
				Collectors.toMap(JiraIssue::getNumber, JiraIssue::getStoryPoints, (filter1, filter2) -> filter1));

		Map<Pair<String, String>, Double> sprintWiseQADDMap = new HashMap<>();
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap = new HashMap<>();

		processHowerMap(sprintWiseMap, storyDefectDataListMap, sprintIdSprintNameMap, kpiRequest, requestTrackerId,
				kpiElement, validationDataMap, sprintWiseQADDMap, sprintWiseHowerMap, storyFilteredList,
				storyWithStoryPoint);

		processSprintNodelist(sprintLeafNodeList, kpiRequest, sprintWiseQADDMap, trendValueList, requestTrackerId,
				sprintWiseHowerMap, mapTmp);

	}

	/**
	 * Sets the hower map.
	 *
	 * @param sprintWiseHowerMap
	 *            the sprint wise hower map
	 * @param sprint
	 *            the sprint
	 * @param storyList
	 *            the story list
	 * @param sprintWiseDefectList
	 *            the sprint wise defect list
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<JiraIssue> storyList, Set<JiraIssue> sprintWiseDefectList) {
		Map<String, Integer> howerMap = new LinkedHashMap<>();
		double storyPointsTotal = storyList.stream().mapToDouble(JiraIssue::getStoryPoints).sum();
		if (CollectionUtils.isNotEmpty(sprintWiseDefectList)) {
			howerMap.put(DEFECT, sprintWiseDefectList.size());
		} else {
			howerMap.put(DEFECT, 0);
		}
		howerMap.put(STORY_POINTS_DATA, (int) storyPointsTotal);
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	/**
	 * Process sprint nodelist and sets trend value list.
	 *
	 * @param sprintLeafNodeList
	 *            the sprint leaf node list
	 * @param kpiRequest
	 *            the kpi request
	 * @param sprintWiseQADDMap
	 *            the sprint wise QADD map
	 * @param trendValueList
	 *            the trend value list
	 * @param requestTrackerId
	 *            the request tracker id
	 * @param sprintWiseHowerMap
	 *            the sprint wise hower map
	 * @param mapTmp
	 *            the map tmp
	 */
	private void processSprintNodelist(List<Node> sprintLeafNodeList, KpiRequest kpiRequest,
			Map<Pair<String, String>, Double> sprintWiseQADDMap, List<DataCount> trendValueList,
			String requestTrackerId, Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap,
			Map<String, Node> mapTmp) {
		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			double qaddForCurrentLeaf;

			if (sprintWiseQADDMap.containsKey(currentNodeIdentifier)) {
				qaddForCurrentLeaf = sprintWiseQADDMap.get(currentNodeIdentifier);
			} else {
				qaddForCurrentLeaf = 0.0d;
			}
			// aggregated value to exclude the sprint with sum of story points
			// is zero
			if (qaddForCurrentLeaf == -1000.0) {
				qaddForCurrentLeaf = 0.0d;
			}
			log.debug("[QADD-SPRINT-WISE][{}]. QADD for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), qaddForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(qaddForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(qaddForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));

			trendValueList.add(dataCount);
		});
	}

	/**
	 * Process hower map and sets sprintwise KPI value map.
	 *
	 * @param sprintWiseMap
	 *            the sprint wise map
	 * @param storyDefectDataListMap
	 *            the story defect data list map
	 * @param sprintIdSprintNameMap
	 *            the sprint id sprint name map
	 * @param kpiRequest
	 *            the kpi request
	 * @param requestTrackerId
	 *            the request tracker id
	 * @param kpiElement
	 *            the kpi element
	 * @param validationDataMap
	 *            the validation data map
	 * @param sprintWiseQADDMap
	 *            the sprint wise QADD map
	 * @param sprintWiseHowerMap
	 *            the sprint wise hower map
	 * @param storyFilteredList
	 *            the story filtered list
	 */
	private void processHowerMap(Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap, // NOPMD//NOSONAR
			Map<String, Object> storyDefectDataListMap, Map<String, String> sprintIdSprintNameMap, // NOSONAR
			KpiRequest kpiRequest, String requestTrackerId, KpiElement kpiElement, // NOSONAR
			Map<String, ValidationData> validationDataMap, Map<Pair<String, String>, Double> sprintWiseQADDMap, // NOSONAR
			Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap, List<JiraIssue> storyFilteredList,
			Map<String, Double> storyWithStoryPoint) {// NOSONAR
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {
			Set<JiraIssue> sprintWiseDefectList = new HashSet<>();
			List<Double> qaddList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();
			List<JiraIssue> storyList = new ArrayList<>();
			List<String> storyPointList = new ArrayList<>();
			List<String> storyIds = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(storyIds::addAll);
			processSubCategoryMap(storyIds, storyDefectDataListMap, qaddList, sprintWiseDefectList, totalStoryIdList,
					storyList, storyFilteredList, storyPointList);

			String validationDataKey = sprintIdSprintNameMap.get(sprint.getValue());
			populateValidationDataObject(kpiElement, requestTrackerId, validationDataKey, validationDataMap,
					totalStoryIdList, sprintWiseDefectList, storyWithStoryPoint);
			double sprintWiseQADD = calculateKpiValue(qaddList, KPICode.DEFECT_DENSITY.getKpiId());
			sprintWiseQADDMap.put(sprint, sprintWiseQADD);
			setHowerMap(sprintWiseHowerMap, sprint, storyList, sprintWiseDefectList);
		});
	}

	/**
	 * Process sub category map and evaluates the KPI value.
	 *
	 * @param storyIdList
	 *            the story id list
	 * @param storyDefectDataListMap
	 *            the story defect data list map
	 * @param qaddList
	 *            the qadd list
	 * @param sprintWiseDefectList
	 *            the sprint wise defect list
	 * @param totalStoryIdList
	 *            the total story id list
	 * @param storyList
	 *            the story list
	 * @param storyFilteredList
	 *            the story filtered list
	 * @param storyPointList2
	 *            the story point list 2
	 */
	private void processSubCategoryMap(List<String> storyIdList, Map<String, Object> storyDefectDataListMap, // NOPMD
																											 // //NOSONAR
			List<Double> qaddList, Set<JiraIssue> sprintWiseDefectList, List<String> totalStoryIdList, // NOSONAR
			List<JiraIssue> storyList, List<JiraIssue> storyFilteredList, List<String> storyPointList2) {// NOSONAR
		HashMap<String, JiraIssue> mapOfStories = new HashMap<>();
		for (JiraIssue f : storyFilteredList) {
			mapOfStories.put(f.getNumber(), f);
		}

		@SuppressWarnings("unchecked")
		List<JiraIssue> additionalFilterDefectList = ((List<JiraIssue>) storyDefectDataListMap.get(DEFECT_DATA))
				.stream().filter(f -> CollectionUtils.containsAny(f.getDefectStoryID(),
						storyIdList == null ? Collections.emptyList() : storyIdList))
				.collect(Collectors.toList());
		populateList(additionalFilterDefectList, mapOfStories);
		@SuppressWarnings("unchecked")
		List<JiraIssue> storyPointList = ((List<JiraIssue>) storyDefectDataListMap.get(STORY_POINTS)).stream()
				.filter(f -> CollectionUtils.isNotEmpty(storyIdList) && storyIdList.contains(f.getNumber()))
				.collect(Collectors.toList());
		storyList.addAll(storyPointList);
		for (JiraIssue f : storyPointList) {
			storyPointList2.add(String.valueOf(f.getStoryPoints()));
		}
		double qaddForCurrentLeaf = 0.0d;
		double storyPointsTotal;
		if (CollectionUtils.isNotEmpty(storyList)) {
			storyPointsTotal = storyList.stream().mapToDouble(JiraIssue::getStoryPoints).sum();// NOPMD
			if (storyPointsTotal == 0.0d) {// NOPMD
				qaddForCurrentLeaf = -1000.0;
			} else if (CollectionUtils.isNotEmpty(additionalFilterDefectList)) {
				qaddForCurrentLeaf = (additionalFilterDefectList.size() / storyPointsTotal) * 100;
			}
		} else {
			qaddForCurrentLeaf = -1000.0;
		}
		qaddList.add(qaddForCurrentLeaf);
		sprintWiseDefectList.addAll(additionalFilterDefectList);
		totalStoryIdList.addAll(storyIdList == null ? Collections.emptyList() : storyIdList);
	}

	private void populateList(List<JiraIssue> additionalFilterDefectList, HashMap<String, JiraIssue> mapOfStories) {
		if (!additionalFilterDefectList.isEmpty()) {
			// Filter for QA tagged defects
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(new ObjectId(additionalFilterDefectList.get(0).getBasicProjectConfigId()));

			if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByQAValue())) {
				additionalFilterDefectList = additionalFilterDefectList.stream().filter(f -> f.isDefectRaisedByQA()) // NOSONAR
						.collect(Collectors.toList());
			} else if (null != fieldMapping && CollectionUtils.isNotEmpty(fieldMapping.getJiraBugRaisedByValue())) {
				additionalFilterDefectList = additionalFilterDefectList.stream()
						.filter(f -> f.getDefectRaisedBy() != null && !(f.getDefectRaisedBy().equalsIgnoreCase("UAT")))
						.collect(Collectors.toList());
			}

			// Filter for defects NOT linked to stories in a given sprint
			additionalFilterDefectList.addAll(additionalFilterDefectList.stream()
					.filter(f -> (!f.getDefectStoryID().isEmpty()
							&& mapOfStories.containsKey(f.getDefectStoryID().iterator().next())))
					.collect(Collectors.toList()));
		}

	}

	/**
	 * Fetch filtered KPI data from database.
	 *
	 * @param leafNodeList
	 *            the leaf node list
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @param kpiRequest
	 *            the kpi request
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		long startTime = System.currentTimeMillis();

		Map<String, Object> resultListMap = kpiHelperService.fetchQADDFromDb(leafNodeList, kpiRequest);

		if (log.isDebugEnabled()) {
			List<SprintWiseStory> storyDataList = (List<SprintWiseStory>) resultListMap.get(STORY_DATA);
			List<JiraIssue> defectDataList = (List<JiraIssue>) resultListMap.get(DEFECT_DATA);// NOPMD
			log.info("[QADD-DB-QUERY][]. storyData count: {} defectData count: {}  time: {}", storyDataList.size(), // NOPMD
					defectDataList.size(), System.currentTimeMillis() - startTime);
		}

		return resultListMap;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> objectMap) {
		return null;
	}

	/**
	 * This method populates KPI Element with Validation data. It will be
	 * triggered only for request originated to get Excel data.
	 *
	 * @param kpiElement
	 *            KpiElement
	 * @param requestTrackerId
	 *            request id
	 * @param validationDataKey
	 *            validation data key
	 * @param validationDataMap
	 *            validation data map
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectList
	 *            sprints defect list
	 * @param storyWithStoryPoint
	 *            story with story point map
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId, String validationDataKey,
			Map<String, ValidationData> validationDataMap, List<String> storyIdList,
			Set<JiraIssue> sprintWiseDefectList, Map<String, Double> storyWithStoryPoint) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			List<String> story = new ArrayList<>();
			List<String> storyPoint = new ArrayList<>();
			ValidationData validationData = new ValidationData();
			for (String s : storyIdList) {
				storyWithStoryPoint.computeIfPresent(s, (key, val) -> {
					story.add(key);
					storyPoint.add(val.toString());
					return val;
				});
			}
			validationData.setStoryKeyList(story);
			validationData.setStoryPointList(storyPoint);
			validationData.setDefectKeyList(
					sprintWiseDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			validationDataMap.put(validationDataKey, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

}
