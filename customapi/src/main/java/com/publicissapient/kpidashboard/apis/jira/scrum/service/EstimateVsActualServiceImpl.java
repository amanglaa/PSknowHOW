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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalColoumn;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Component
public class EstimateVsActualServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EstimateVsActualServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String MODAL_HEAD_ISSUE_ID = "Issue Id";
	private static final String MODAL_HEAD_ISSUE_DESC = "Issue Description";
	private static final String ORIGINAL_ESTIMATES = "Original Estimates";
	private static final String LOGGED_WORK = "Logged Work";
	private static final String OVERALL = "Overall";
	private static final String HOURS = "Hours";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.ESTIMATE_VS_ACTUAL.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			LOGGER.info("Estimate Vs Actual -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues =  KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(totalIssues,
							basicProjectConfigId);
					resultListMap.put(ISSUES, issueList);
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 * 
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Estimate Vs Actual -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());

			Map<String, List<JiraIssue>> typeWiseIssues = allIssues.stream()
					.collect(Collectors.groupingBy(JiraIssue::getTypeName));

			Set<String> issueTypes = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllOrigEst = Arrays.asList(0);
			List<Integer> overAllLogWork = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeWiseIssues.forEach((issueType, issues) -> {
				issueTypes.add(issueType);
				List<IterationKpiModalValue> modalValues = new ArrayList<>();
				int origEstData = 0;
				int logWorkData = 0;
				for (JiraIssue jiraIssue : issues) {
					IterationKpiModalColoumn iterationKpiModalColoumn = new IterationKpiModalColoumn(
							jiraIssue.getNumber(), jiraIssue.getUrl());
					IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue(iterationKpiModalColoumn,
							jiraIssue.getName(), jiraIssue.getStatus(), jiraIssue.getTypeName());
					modalValues.add(iterationKpiModalValue);
					overAllmodalValues.add(iterationKpiModalValue);

					if (null != jiraIssue.getOriginalEstimateMinutes()) {
						origEstData = origEstData + jiraIssue.getOriginalEstimateMinutes();
						overAllOrigEst.set(0, overAllOrigEst.get(0) + jiraIssue.getOriginalEstimateMinutes());
					}
					if (null != jiraIssue.getTimeSpentInMinutes()) {
						logWorkData = logWorkData + jiraIssue.getTimeSpentInMinutes();
						overAllLogWork.set(0, overAllLogWork.get(0) + jiraIssue.getTimeSpentInMinutes());
					}
				}
				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData originalEstimates = new IterationKpiData(ORIGINAL_ESTIMATES,
						Double.valueOf(origEstData), null, null, HOURS,modalValues);
				IterationKpiData loggedWork = new IterationKpiData(LOGGED_WORK, Double.valueOf(logWorkData), null, null,
						HOURS,null);
				data.add(originalEstimates);
				data.add(loggedWork);
				IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, null, data);
				iterationKpiValues.add(iterationKpiValue);

			});
			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllorigEstimates = new IterationKpiData(ORIGINAL_ESTIMATES,
					Double.valueOf(overAllOrigEst.get(0)), null, null, HOURS,overAllmodalValues);
			IterationKpiData overAllloggedWork = new IterationKpiData(LOGGED_WORK,
					Double.valueOf(overAllLogWork.get(0)), null, null, HOURS,null);
			data.add(overAllorigEstimates);
			data.add(overAllloggedWork);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, null);
			// Modal Heads Options
			List<String> modalHeads = Arrays.asList(MODAL_HEAD_ISSUE_ID, MODAL_HEAD_ISSUE_DESC, CommonConstant.MODAL_HEAD_ISSUE_STATUS,
					CommonConstant.MODAL_HEAD_ISSUE_TYPE);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(modalHeads);
			kpiElement.setTrendValueList(trendValue);
		}
	}
}
