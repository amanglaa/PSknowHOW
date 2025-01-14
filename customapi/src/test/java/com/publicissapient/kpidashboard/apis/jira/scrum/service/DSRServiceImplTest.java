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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.data.SprintWiseStoryDataFactory;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class DSRServiceImplTest {
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;

	private KpiRequest kpiRequest;
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> uatBugList = new ArrayList<>();
	List<JiraIssue> totalBugList = new ArrayList<>();
	List<SprintWiseStory> sprintWiseStoryList = new ArrayList<>();
	private final static String UATBUGKEY = "uatBugData";
	private final static String TOTALBUGKEY = "totalBugData";
	private Map<String, String> kpiWiseAggregation = new HashMap<>();

	@Mock
	JiraIssueRepository jiraIssueRepository;

	@Mock
	CacheService cacheService;

	@Mock
	ConfigHelperService configHelperService;

	@Mock
	private KpiHelperService kpiHelperService;

	@InjectMocks
	DSRServiceImpl dsrServiceImpl;

	@Mock
	ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	FieldMappingRepository fieldMappingRepository;

	@Mock
	private CommonService commonService;

	@Mock
	private FilterHelperService filterHelperService;

	@Mock
	CustomApiConfig customApiSetting;

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		SprintWiseStoryDataFactory sprintWiseStoryDataFactory = SprintWiseStoryDataFactory.newInstance();
		sprintWiseStoryList= sprintWiseStoryDataFactory.getSprintWiseStories();


		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalBugList = jiraIssueDataFactory.getBugs();

		uatBugList = totalBugList.stream()
				.filter(f -> NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue().equalsIgnoreCase(f.getDefectRaisedBy()))
				.collect(Collectors.toList());

		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

		kpiWiseAggregation.put("defectSeepageRate", "percentile");
	}

	@After
	public void cleanup() {
		jiraIssueRepository.deleteAll();

	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> filterComponentIdWiseDefectMap = new HashMap<>();
		filterComponentIdWiseDefectMap.put(UATBUGKEY, uatBugList);
		filterComponentIdWiseDefectMap.put(TOTALBUGKEY, totalBugList);
		Double dsrValue = dsrServiceImpl.calculateKPIMetrics(filterComponentIdWiseDefectMap);
		assertThat("DSR value :", dsrValue, equalTo(53.0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();
		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssueByStoryNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(totalBugList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> defectDataListMap = dsrServiceImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Total Defects value :", ((List<JiraIssue>) (defectDataListMap.get(TOTALBUGKEY))).size(),
				equalTo(0));
	}

	@Test
	public void testGetDSR() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put("defectSeepageRate", Arrays.asList("-30", "30-10", "10-5", "5-2", "2-"));

		when(configHelperService.calculateMaturity()).thenReturn(maturityRangeMap);

		when(jiraIssueRepository.findIssuesGroupBySprint(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(sprintWiseStoryList);

		when(jiraIssueRepository.findIssueByStoryNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(totalBugList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(dsrServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		try {
			KpiElement kpiElement = dsrServiceImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("DSR Value :",
					((ArrayList) ((List<DataCount>) kpiElement.getTrendValueList()).get(0).getValue()).size(),
					equalTo(5));
		} catch (ApplicationException enfe) {

		}
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", dsrServiceImpl.getQualifierType(), equalTo(KPICode.DEFECT_SEEPAGE_RATE.name()));
	}

}
