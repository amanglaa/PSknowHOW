package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.class)
public class DefectsWithoutStoryLinkServiceImplTest {
	@Mock
	CacheService cacheService;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private FieldMappingDataFactory fieldMappingDataFactory;
	@Mock
	private JiraIssueDataFactory jiraIssueDataFactory;
	@Mock
	private ProjectBasicConfigDataFactory projectBasicConfigDataFactory;
	@Mock
	private AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory;
	@Mock
	private KpiRequestFactory kpiRequestFactory;
	@Mock
	private ConfigHelperService configHelperService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private CommonService commonService;
	@InjectMocks
	private DefectsWithoutStoryLinkServiceImpl defectsWithoutStoryLinkServiceImpl;

	private List<JiraIssue> storyList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private List<AccountHierarchyData> ahdList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private List<JiraIssue> defectWithoutStoryList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private static final String P1 = "p1,P1 - Blocker, blocker, 1, 0, p0, Urgent";
	private static final String P2 = "p2, critical, P2 - Critical, 2, High";
	private static final String P3 = "p3, P3 - Major, major, 3, Medium";
	private static final String P4 = "p4, P4 - Minor, minor, 4, Low";
	private static final String P5 = "Overall";

	@Before
	public void setup() {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi16");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		storyList = JiraIssueDataFactory.newInstance().getJiraIssues();
		defectWithoutStoryList = storyList.stream().filter(issue->null != issue.getDefectStoryID()).collect(Collectors.toList());
		fieldMappingList = FieldMappingDataFactory.newInstance("/json/default/project_field_mappings.json").getFieldMappings();
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});
		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);
		setTreadValuesDataCount();
	}

	@Test
	public void testCalculateKPIMetrics() {
		Map<String, Object> subCategoryMap = new HashMap<>();
		Integer storyCount = defectsWithoutStoryLinkServiceImpl.calculateKPIMetrics(subCategoryMap);
		assertThat("Story List : ", storyCount, equalTo(null));
	}

	@Test
	public void getQualifierType() {
		assertThat(KPICode.DEFECTS_WITHOUT_STORY_LINK.name(),
				equalTo(defectsWithoutStoryLinkServiceImpl.getQualifierType()));
	}

	/**
	 * Project to show on trend line.
	 */
	@Test
	public void testGetKpiDataProject() throws ApplicationException {

		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(storyList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(defectsWithoutStoryLinkServiceImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		when(jiraIssueRepository.findDefectsWithoutStoryLink(any(), any())).thenReturn(storyList);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);

		try {
			fetchKPIDataFromDbDup();
			KpiElement kpiElement = defectsWithoutStoryLinkServiceImpl.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);
			assertThat("Defect Count : ", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(5));
		} catch (ApplicationException enfe) {

		}

	}

	@Test
	public void fetchKPIDataFromDb() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);
		when(jiraIssueRepository.findDefectsWithoutStoryLink(Mockito.anyMap(), Mockito.anyMap())).thenReturn(defectWithoutStoryList);
		Map<String, Object> outputMap = defectsWithoutStoryLinkServiceImpl.fetchKPIDataFromDb(leafNodeList,
				null, null, kpiRequest);
		assertThat("output map size :", outputMap.size(),
				equalTo(2));
	}

	@Test
	public void fetchKPIDataFromDbDup() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		when(jiraIssueRepository.findIssuesBySprintAndType(Mockito.anyMap(), Mockito.anyMap())).thenReturn(storyList);
		when(jiraIssueRepository.findDefectsWithoutStoryLink(Mockito.anyMap(), Mockito.anyMap())).thenReturn(defectWithoutStoryList);
		Map<String, Object> outputMap = defectsWithoutStoryLinkServiceImpl.fetchKPIDataFromDb(leafNodeList,
				null, null, kpiRequest);
		assertThat("output map size :", outputMap.size(),
				equalTo(2));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put(P1, trendValues);
		trendValueMap.put(P2, trendValues);
		trendValueMap.put(P3, trendValues);
		trendValueMap.put(P4, trendValues);
		trendValueMap.put(P5, trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}
}
