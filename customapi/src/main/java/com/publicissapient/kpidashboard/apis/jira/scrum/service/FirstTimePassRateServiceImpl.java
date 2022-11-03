/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
@Component
public class FirstTimePassRateServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String FIRST_TIME_PASS_STORIES = "ftpStories";
	private static final String SPRINT_WISE_CLOSED_STORIES = "sprintWiseClosedStories";
	private static final String HOVER_KEY_CLOSED_STORIES = "Closed Stories";
	private static final String HOVER_KEY_FTP_STORIES = "FTP Stories";
	private static final String ISSUE_DATA = "Issue Data";

	private static final String DEV = "DeveloperKpi";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.FIRST_TIME_PASS_RATE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		log.debug("[FTPR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.FIRST_TIME_PASS_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.FIRST_TIME_PASS_RATE);
		kpiElement.setTrendValueList(trendValues);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		log.debug("[STORYCOUNT-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);

		return kpiElement;
	}

	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) resultMap.get(SPRINT_WISE_CLOSED_STORIES);

		Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
				.groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

		Map<String, String> sprintIdSprintNameMap = sprintWiseStoryList.stream().collect(
				Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getSprintName, (name1, name2) -> name1));

		Map<String, JiraIssue> issueData = (Map<String, JiraIssue>) resultMap.get(ISSUE_DATA);

		Map<Pair<String, String>, Double> sprintWiseFTPRMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintWiseMap.forEach((sprint, sprintWiseStories) -> {
			List<Double> addFilterFtprList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();
			sprintWiseStories.stream().map(SprintWiseStory::getStoryList).collect(Collectors.toList())
					.forEach(totalStoryIdList::addAll);

			List<JiraIssue> ftpStoriesList = ((List<JiraIssue>) resultMap.get(FIRST_TIME_PASS_STORIES)).stream()
					.filter(jiraIssue -> jiraIssue.getSprintID().equals(sprint.getValue()))
					.collect(Collectors.toList());

			double ftprForCurrentLeaf = 0.0d;
			if (CollectionUtils.isNotEmpty(ftpStoriesList) && CollectionUtils.isNotEmpty(totalStoryIdList)) {
				ftprForCurrentLeaf = ((double) ftpStoriesList.size() / totalStoryIdList.size()) * 100;
			}
			addFilterFtprList.add(ftprForCurrentLeaf);

			// if for populating excel data
			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				String sprintName = sprintIdSprintNameMap.get(sprint.getValue());
				KPIExcelUtility.populateFTPRExcelData(sprintName, totalStoryIdList, ftpStoriesList, excelData,
						issueData);
			}

			double sprintWiseFtpr = calculateKpiValue(addFilterFtprList, KPICode.FIRST_TIME_PASS_RATE.getKpiId());
			sprintWiseFTPRMap.put(sprint, sprintWiseFtpr);
			setHowerMap(sprintWiseHowerMap, sprint, totalStoryIdList, ftpStoriesList);
		});

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			double ftprForCurrentLeaf;

			if (sprintWiseFTPRMap.containsKey(currentNodeIdentifier)) {
				ftprForCurrentLeaf = sprintWiseFTPRMap.get(currentNodeIdentifier);
			} else {
				ftprForCurrentLeaf = 0.0d;
			}

			log.debug("[FTPR-SPRINT-WISE][{}]. FTPR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), ftprForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(ftprForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(ftprForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));

			trendValueList.add(dataCount);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.FIRST_TIME_PASS_RATE.getColumns());

	}

	private void setHowerMap(Map<Pair<String, String>, Map<String, Integer>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<String> storyIdList, List<JiraIssue> sprintWiseFtpStories) {
		Map<String, Integer> howerMap = new LinkedHashMap<>();
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(sprintWiseFtpStories)) {
			howerMap.put(HOVER_KEY_FTP_STORIES, sprintWiseFtpStories.size());
		} else {
			howerMap.put(HOVER_KEY_FTP_STORIES, 0);
		}
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(storyIdList)) {
			howerMap.put(HOVER_KEY_CLOSED_STORIES, storyIdList.size());
		} else {
			howerMap.put(HOVER_KEY_CLOSED_STORIES, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return 0.0D;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Pair<String, String>> sprintWithDateMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		Map<String, List<String>> statusConfigsOfResolutionTypeForRejection = new HashMap<>();
		Map<String, List<String>> statusConfigsOfDefectRejectionStatus = new HashMap<>();
		Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject = new HashMap<>();
		Map<String, List<String>> projectWisePriority = new HashMap<>();
		Map<String, List<String>> configPriority = customApiConfig.getPriority();
		Map<String, Set<String>> projectWiseRCA = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			sprintList.add(leaf.getSprintFilter().getId());
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			sprintWithDateMap.put(leaf.getSprintFilter().getId(),
					Pair.of(leaf.getSprintFilter().getStartDate(), leaf.getSprintFilter().getEndDate()));
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			addPriorityProjectWise(projectWisePriority, configPriority, leaf, fieldMapping);
			addRCAProjectWise(projectWiseRCA, leaf, fieldMapping);

			statusConfigsOfResolutionTypeForRejection.put(basicProjectConfigId.toString(),
					fieldMapping.getResolutionTypeForRejection() == null ? new ArrayList<>()
							: fieldMapping.getResolutionTypeForRejection().stream().map(String::toLowerCase)
									.collect(Collectors.toList()));

			statusConfigsOfDefectRejectionStatus.put(basicProjectConfigId.toString(),
					Arrays.asList(fieldMapping.getJiraDefectRejectionStatus()));

			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraStoryIdentification()));
			mapOfProjectFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
					fieldMapping.getJiraIssueDeliverdStatus());
			KpiHelperService.getDroppedDefectsFilters(statusConfigsOfRejectedStoriesByProject, basicProjectConfigId,
					fieldMapping);

			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
				sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		// Fetch Story ID grouped by Sprint
		List<SprintWiseStory> sprintWiseStories = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
				uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);

		List<JiraIssue> issuesBySprintAndType = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
				uniqueProjectMap);

		// do not change the order of remove methods
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, issuesBySprintAndType,
				defectListWoDrop);

		KpiHelperService.removeRejectedStoriesFromSprint(sprintWiseStories, defectListWoDrop);

		removeStoriesWithDefect(defectListWoDrop, projectWisePriority, projectWiseRCA,
				statusConfigsOfRejectedStoriesByProject);

		List<String> storyIds = getIssueIds(defectListWoDrop);
		List<JiraIssueCustomHistory> storiesHistory = jiraIssueCustomHistoryRepository.findByStoryIDIn(storyIds);

		removeStoriesWithReturnTransaction(defectListWoDrop, storiesHistory);

		List<String> storyIdList = new ArrayList<>();
		sprintWiseStories.forEach(s -> storyIdList.addAll(s.getStoryList()));
		Set<JiraIssue> issueData = jiraIssueRepository.findIssueAndDescByNumber(storyIdList);
		Map<String, JiraIssue> issueMapping = new HashMap<>();
		issueData.stream().forEach(issue -> issueMapping.putIfAbsent(issue.getNumber(), issue));

		resultListMap.put(SPRINT_WISE_CLOSED_STORIES, sprintWiseStories);
		resultListMap.put(FIRST_TIME_PASS_STORIES, defectListWoDrop);
		resultListMap.put(ISSUE_DATA, issueMapping);
		return resultListMap;
	}

	/**
	 * @param projectWiseRCA
	 * @param leaf
	 * @param fieldMapping
	 */
	private void addRCAProjectWise(Map<String, Set<String>> projectWiseRCA, Node leaf, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getExcludeRCAFromFTPR())) {
			Set<String> uniqueRCA = new HashSet<>();
			for (String rca : fieldMapping.getExcludeRCAFromFTPR()) {
				if (rca.equalsIgnoreCase(Constant.CODING) || rca.equalsIgnoreCase(Constant.CODE)) {
					rca = Constant.CODE_ISSUE;
				}
				uniqueRCA.add(rca.toLowerCase());
			}
			projectWiseRCA.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(), uniqueRCA);
		}
	}

	/**
	 * @param projectWisePriority
	 * @param configPriority
	 * @param leaf
	 * @param fieldMapping
	 */
	private void addPriorityProjectWise(Map<String, List<String>> projectWisePriority,
			Map<String, List<String>> configPriority, Node leaf, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getDefectPriority())) {
			List<String> priorValue = fieldMapping.getDefectPriority().stream().map(String::toUpperCase)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(priorValue)) {
				List<String> priorityValues = new ArrayList<>();
				priorValue.forEach(priority -> priorityValues.addAll(configPriority.get(priority)));
				projectWisePriority.put(leaf.getProjectFilter().getBasicProjectConfigId().toString(), priorityValues);
			}
		}
	}

	@NotNull
	private List<String> getIssueIds(List<JiraIssue> issuesBySprintAndType) {
		List<String> storyIds = new ArrayList<>();
		CollectionUtils.emptyIfNull(issuesBySprintAndType).forEach(story -> storyIds.add(story.getNumber()));
		return storyIds;
	}

	/**
	 * @param issuesBySprintAndType
	 * @param projectWisePriority
	 * @param projectWiseRCA
	 */
	private void removeStoriesWithDefect(List<JiraIssue> issuesBySprintAndType,
			Map<String, List<String>> projectWisePriority, Map<String, Set<String>> projectWiseRCA,
			Map<String, Map<String, List<String>>> statusConfigsOfRejectedStoriesByProject) {
		List<JiraIssue> allDefects = jiraIssueRepository.findByTypeNameAndDefectStoryIDIn(
				NormalizedJira.DEFECT_TYPE.getValue(), getIssueIds(issuesBySprintAndType));
		Set<JiraIssue> defects = new HashSet<>();
		List<JiraIssue> defectListWoDrop = new ArrayList<>();
		KpiHelperService.getDefectsWithoutDrop(statusConfigsOfRejectedStoriesByProject, allDefects, defectListWoDrop);
		defectListWoDrop.stream().forEach(d -> issuesBySprintAndType.stream().forEach(i -> {
			if (i.getProjectName().equalsIgnoreCase(d.getProjectName())) {
				defects.add(d);
			}
		}));

		List<JiraIssue> remainingDefects = new ArrayList<>();
		for (JiraIssue jiraIssue : defects) {
			if (CollectionUtils.isNotEmpty(projectWisePriority.get(jiraIssue.getBasicProjectConfigId()))) {
				if (!(projectWisePriority.get(jiraIssue.getBasicProjectConfigId()).contains(jiraIssue.getPriority()))) {
					remainingDefects.add(jiraIssue);
				}
			} else {
				remainingDefects.add(jiraIssue);
			}
		}

		List<JiraIssue> notFTPRDefects = new ArrayList<>();
		for (JiraIssue jiraIssue : defects) {
			if (CollectionUtils.isNotEmpty(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()))) {
				for (String toFindRca : jiraIssue.getRootCauseList()) {
					if (!(projectWiseRCA.get(jiraIssue.getBasicProjectConfigId()).contains(toFindRca.toLowerCase()))) {
						notFTPRDefects.add(jiraIssue);
					}
				}
			} else {
				notFTPRDefects.add(jiraIssue);
			}
		}

		Set<String> storyIdsWithDefect = new HashSet<>();
		remainingDefects.stream().forEach(pi -> notFTPRDefects.stream().forEach(ri -> {
			if (pi.getNumber().equalsIgnoreCase(ri.getNumber())) {
				storyIdsWithDefect.addAll(ri.getDefectStoryID());
			}
		}));
		issuesBySprintAndType.removeIf(issue -> storyIdsWithDefect.contains(issue.getNumber()));
	}

	private void removeStoriesWithReturnTransaction(List<JiraIssue> issuesBySprintAndType,
			List<JiraIssueCustomHistory> storiesHistory) {

		issuesBySprintAndType.removeIf(issue -> hasReturnTransaction(issue, storiesHistory));

	}

	private boolean hasReturnTransaction(JiraIssue issue, List<JiraIssueCustomHistory> storiesHistory) {
		JiraIssueCustomHistory jiraIssueCustomHistory = storiesHistory.stream()
				.filter(issueHistory -> issueHistory.getStoryID().equals(issue.getNumber())).findFirst().orElse(null);
		if (jiraIssueCustomHistory == null) {
			return false;
		} else {

			List<JiraIssueSprint> storySprintDetails = jiraIssueCustomHistory.getStorySprintDetails();
			Collections.sort(storySprintDetails, Comparator.comparing(JiraIssueSprint::getActivityDate));

			JiraIssueSprint latestClosedStatusDetail = storySprintDetails.stream()
					.filter(statusHistory -> statusHistory.getFromStatus().equals(issue.getJiraStatus())).findFirst()
					.orElse(null);

			if (latestClosedStatusDetail != null) {
				Map<ObjectId, FieldMapping> fieldMappingMap = configHelperService.getFieldMappingMap();
				FieldMapping fieldMapping = fieldMappingMap.get(new ObjectId(issue.getBasicProjectConfigId()));
				List<String> storyDeliveredStatuses = (List<String>) CollectionUtils
						.emptyIfNull(fieldMapping.getJiraIssueDeliverdStatus());
				DateTime latestClosedStatusTime = latestClosedStatusDetail.getActivityDate();
				return storySprintDetails.stream()
						.filter(statusHistory -> statusHistory.getActivityDate().isAfter(latestClosedStatusTime))
						.anyMatch(statusHistory -> storyDeliveredStatuses.contains(statusHistory.getFromStatus()));
			}
			return false;
		}

	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}
}
