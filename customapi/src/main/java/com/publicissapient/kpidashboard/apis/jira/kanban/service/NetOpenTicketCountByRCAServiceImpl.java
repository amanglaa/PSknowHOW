package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;

@Component
public class NetOpenTicketCountByRCAServiceImpl
		extends JiraKPIService<Long, List<Object>, Map<String, Map<String, Map<String, Set<String>>>>> {

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	private static final Logger LOGGER = LoggerFactory.getLogger(NetOpenTicketCountByRCAServiceImpl.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String FIELD_RCA = "rca";

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>TICKET_RCA</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.name();
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

		LOGGER.info("NET-OPEN-TICKET-COUNT-BY-RCA {}", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.NET_OPEN_TICKET_COUNT_BY_RCA);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.NET_OPEN_TICKET_COUNT_BY_RCA);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, dateWiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(dateWiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		// map aggregation implementation over

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		LOGGER.debug(
				"[NET-OPEN-TICKET-COUNT-BY-RCA-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data From Database
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return resultListMap
	 */
	@Override
	public Map<String, Map<String, Map<String, Set<String>>>> fetchKPIDataFromDb(List<Node> leafNodeList,
			String startDate, String endDate, KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = kpiHelperService.fetchJiraCustomHistoryDataFromDbForKanban(leafNodeList,
				startDate, endDate, kpiRequest, FIELD_RCA);

		CustomDateRange dateRangeForCumulative = KpiDataHelper.getStartAndEndDatesForCumulative(kpiRequest);
		String startDateForCumulative = dateRangeForCumulative.getStartDate().format(DATE_FORMATTER);

		Map<String, List<KanbanIssueCustomHistory>> projectWiseNonClosedTickets = kpiHelperService
				.removeClosedTicketsFromHistoryIssuesData(resultListMap, startDateForCumulative);

		return kpiHelperService.computeProjectWiseJiraHistoryByFieldAndDate(projectWiseNonClosedTickets,
				startDateForCumulative, resultListMap, FIELD_RCA);
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch dates for past history data
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(15);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		// past all tickets and given range ticket data fetch from db
		Map<String, Map<String, Map<String, Set<String>>>> resultMap = fetchKPIDataFromDb(leafNodeList, startDate,
				endDate, kpiRequest);

		kpiWithFilter(resultMap, mapTmp, leafNodeList, kpiElement, kpiRequest);
	}

	private void kpiWithFilter(Map<String, Map<String, Map<String, Set<String>>>> resultMap, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		String requestTrackerId = getKanbanRequestTrackerId();

		leafNodeList.forEach(node -> {
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Map<String, Map<String, Set<String>>> jiraHistoryRCAAndDateWiseIssueMap = resultMap
					.getOrDefault(projectNodeId, new HashMap<>());
			if (MapUtils.isNotEmpty(jiraHistoryRCAAndDateWiseIssueMap)) {
				Set<String> projectWiseRCAList = new HashSet<>();
				projectWiseRCAList.addAll(jiraHistoryRCAAndDateWiseIssueMap.keySet());
				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {

					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					Map<String, Long> projectWiseRCACountMap = filterKanbanDataBasedOnDateAndRCAWise(
							jiraHistoryRCAAndDateWiseIssueMap, projectWiseRCAList, dateRange.getEndDate());

					String date = getRange(dateRange, kpiRequest);

					populateProjectFilterWiseDataMap(projectWiseRCACountMap, trendValueMap,
							node.getProjectFilter().getId(), date);

					currentDate = getNextRangeDate(kpiRequest, currentDate);

				}
				// Populates data in Excel for validation for tickets created before
				populateValidationDataObject(kpiElement, requestTrackerId, jiraHistoryRCAAndDateWiseIssueMap,
						validationDataMap, node, projectWiseRCAList);
				mapTmp.get(node.getId()).setValue(trendValueMap);
			}
		});
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Long
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Map<String, Map<String, Set<String>>>> subCategoryMap) {
		return subCategoryMap == null ? 0L : subCategoryMap.size();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	/**
	 * Total tickets data as per given date range and type If range is DAYS then
	 * filter data as consider data is currentDate data. If range Weeks then filter
	 * data as consider sunday data for given week data and If range Month then
	 * Filter data as consider last month data for given month data. If range date
	 * is after than today date then consider as today date for data
	 *
	 * @param jiraHistoryRCAAndDateWiseIssueMap
	 * @param rcaList
	 * @param currentDate
	 */
	public Map<String, Long> filterKanbanDataBasedOnDateAndRCAWise(
			Map<String, Map<String, Set<String>>> jiraHistoryRCAAndDateWiseIssueMap, Set<String> rcaList,
			LocalDate currentDate) {
		String date;
		if (currentDate.isAfter(LocalDate.now())) {
			date = LocalDate.now().toString();
		} else {
			date = currentDate.toString();
		}
		Map<String, Long> projectRCAMap = new HashMap<>();
		rcaList.forEach(rca -> {
			Set<String> ids = jiraHistoryRCAAndDateWiseIssueMap.get(rca).getOrDefault(date, new HashSet<>()).stream()
					.filter(Objects::nonNull).collect(Collectors.toSet());
			projectRCAMap.put(rca, Long.valueOf(ids.size()));
		});
		rcaList.forEach(rca -> projectRCAMap.computeIfAbsent(rca, val -> 0L));
		return projectRCAMap;
	}

	/**
	 * RCA wise prepare data count list and treadValueMap
	 *
	 * @param projectWiseRCAMap
	 * @param projectFilterWiseDataMap
	 * @param projectNodeId
	 * @param date
	 */
	private void populateProjectFilterWiseDataMap(Map<String, Long> projectWiseRCAMap,
			Map<String, List<DataCount>> projectFilterWiseDataMap, String projectNodeId, String date) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

		Map<String, Integer> hoverValueMap = new HashMap<>();
		projectWiseRCAMap.forEach((key, value) -> {
			hoverValueMap.put(key, value.intValue());
			DataCount dcObj = getDataCountObject(value, projectName, date, projectNodeId, key, hoverValueMap);
			projectFilterWiseDataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});

		Long aggLineValue = projectWiseRCAMap.values().stream().mapToLong(p -> p).sum();

		projectFilterWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(getDataCountObject(
				aggLineValue, projectName, date, projectNodeId, CommonConstant.OVERALL, hoverValueMap));
	}

	/**
	 * as per date type given next range date
	 *
	 * @param kpiRequest
	 * @param currentDate
	 */
	@NotNull
	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			currentDate = currentDate.minusWeeks(1);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param dateRange
	 * @param kpiRequest
	 */
	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = dateRange.getStartDate().toString() + " to " + dateRange.getEndDate().toString();
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param value
	 * @param projectName
	 * @param date
	 * @param projectNodeId
	 * @param rca
	 * @param
	 */
	private DataCount getDataCountObject(Long value, String projectName, String date, String projectNodeId, String rca,
			Map<String, Integer> overAllHoverValueMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setKpiGroup(rca);
		Map<String, Integer> hoverValueMap = new HashMap<>();
		if (rca.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(rca, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		return dataCount;
	}

	/**
	 * Populates Validation Data Object for excel. Only Latest today cumulative data
	 * export in excel
	 *
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param jiraHistoryRCAAndDateWiseIssueMap
	 * @param validationDataMap
	 * @param node
	 * @param projectWiseRCAList
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
			Map<String, Map<String, Set<String>>> jiraHistoryRCAAndDateWiseIssueMap,
			Map<String, ValidationData> validationDataMap, Node node, Set<String> projectWiseRCAList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String dateProjectKey = node.getAccountHierarchyKanban().getNodeName();

			if (MapUtils.isNotEmpty(jiraHistoryRCAAndDateWiseIssueMap)) {
				ValidationData validationData = kpiHelperService.prepareExcelForKanbanCumulativeDataMap(
						jiraHistoryRCAAndDateWiseIssueMap, FIELD_RCA, projectWiseRCAList);
				validationDataMap.put(dateProjectKey, validationData);
			}
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}
}
