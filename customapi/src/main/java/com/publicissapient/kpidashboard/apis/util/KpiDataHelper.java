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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * The class contains methods for helping kpi to prepare data
 *
 * @author anisingh4
 */
@Slf4j
public final class KpiDataHelper {
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private KpiDataHelper() {
	}

	/**
	 * Creates condition map for additional filters
	 *
	 * @param kpiRequest
	 * @param mapOfFilters
	 * @param methodology
	 * @param individualDevOrQa
	 * @return sub group category as String
	 */
	public static String createAdditionalFilterMap(KpiRequest kpiRequest, Map<String, List<String>> mapOfFilters,
			String methodology, String individualDevOrQa, FilterHelperService flterHelperService) {
		String subGroupCategory = Constant.SPRINT;
		if (methodology.equals(Constant.KANBAN)) {
			subGroupCategory = Constant.DATE;
		}
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCategory = addFilterCat.entrySet().stream()
	            .collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));
		
		if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())) {
			for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
				if(CollectionUtils.isNotEmpty(entry.getValue()) && null!=addFilterCategory.get(entry.getKey().toUpperCase())) {
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERID.getFieldValueInFeature(),Arrays.asList(entry.getKey()));
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERVALUES_VALUEID.getFieldValueInFeature(),entry.getValue());
					subGroupCategory = entry.getKey();
				}
			}
		}
		return subGroupCategory;
	}

	/**
	 * Creates subcategory wise map.
	 *
	 * @param subGroupCategory
	 * @param sprintWiseStoryList
	 * @return {@code Map<String , Map <String , List <String>>>} Map of sprint
	 *         and subcategory wise list of featureId
	 */
	public static Map<Pair<String, String>, Map<String, List<String>>> createSubCategoryWiseMap(String subGroupCategory,
			List<SprintWiseStory> sprintWiseStoryList, String filterToShowOnTrend) {

		Map<Pair<String, String>, Map<String, List<String>>> sprintWiseStoryMap = new HashMap<>();

		Map<Pair<String, String>, List<SprintWiseStory>> sprintAndFilterDataMap = sprintWiseStoryList.stream().collect(
				Collectors.groupingBy(sws -> Pair.of(sws.getProjectID(), sws.getSprint()), Collectors.toList()));

		sprintAndFilterDataMap.entrySet().forEach(data -> {
			Map<String, List<String>> subCategoryDataMap = new HashMap<>();
			if (Constant.SPRINT.equals(subGroupCategory)) {
				subCategoryDataMap = data.getValue().stream()
						.collect(Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getStoryList));
			}
			sprintWiseStoryMap.put(data.getKey(), subCategoryDataMap);
		});

		return sprintWiseStoryMap;
	}

	/**
	 * Creates date wise map for kanban
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 * @return
	 */
	public static Map<String, List<KanbanJiraIssue>> createProjectWiseMapKanban(List<KanbanJiraIssue> ticketList,
			String subGroupCategory, FilterHelperService flterHelperService) {
		Map<String, List<KanbanJiraIssue>> projectAndDateWiseTicketMap = new HashMap<>();
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList(addFilterCat.keySet());
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			projectAndDateWiseTicketMap = ticketList.stream()
					.collect(Collectors.groupingBy(KanbanJiraIssue::getBasicProjectConfigId));
		}

		return projectAndDateWiseTicketMap;
	}

	/**
	 * Creates date wise map for kanban history
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 * @return
	 */
	public static Map<String, List<KanbanIssueCustomHistory>> createProjectWiseMapKanbanHistory(
			List<KanbanIssueCustomHistory> ticketList, String subGroupCategory, FilterHelperService flterHelperService) {
		Map<String, List<KanbanIssueCustomHistory>> projectAndDateWiseTicketMap = new HashMap<>();
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList(addFilterCat.keySet());
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			projectAndDateWiseTicketMap = ticketList.stream()
					.collect(Collectors.groupingBy(KanbanIssueCustomHistory::getBasicProjectConfigId));
		}

		return projectAndDateWiseTicketMap;
	}

	/**
	 * Creates date wise category map
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 */
	public static Map<String, Map<String, List<KanbanCapacity>>> createDateWiseCapacityMap(
			List<KanbanCapacity> ticketList, String subGroupCategory, FilterHelperService flterHelperService) {
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList(addFilterCat.keySet());
		Map<String, Map<String, List<KanbanCapacity>>> projectAndDateWiseCapacityMap = new HashMap<>();
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			Map<String, List<KanbanCapacity>> projectWiseCapacityMap =  ticketList.stream()
						.collect(Collectors.groupingBy(ticket->ticket.getBasicProjectConfigId().toString(), Collectors.toList()));

			projectWiseCapacityMap.forEach((project, capacityList) -> {
				Map<String, List<KanbanCapacity>> dateWiseCapacityMap = new HashMap<>();
				capacityList.forEach(kanbanCapacity -> {
					for (LocalDate date = kanbanCapacity.getStartDate(); (date.isBefore(kanbanCapacity.getEndDate())
							|| date.isEqual(kanbanCapacity.getEndDate())); date = date.plusDays(1)) {
						String formattedDate = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
						dateWiseCapacityMap.putIfAbsent(formattedDate, new ArrayList<>());
						dateWiseCapacityMap.get(formattedDate).add(kanbanCapacity);
					}
				});
				projectAndDateWiseCapacityMap.put(project, dateWiseCapacityMap);
			});
		}
		return projectAndDateWiseCapacityMap;
	}


	public static LocalDate convertStringToDate(String dateString) {
		return LocalDate.parse(dateString.split("T")[0]);
	}


	public static CustomDateRange getStartAndEndDate(KpiRequest kpiRequest) {
		int dataPoint = (int) ObjectUtils.defaultIfNull(kpiRequest.getKanbanXaxisDataPoints(), 7) + 1;
		CustomDateRange cdr = new CustomDateRange();
		cdr.setEndDate(LocalDate.now());
		LocalDate startDate = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(dataPoint);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(dataPoint);
		} else {
			startDate = LocalDate.now().minusDays(dataPoint);
		}
		cdr.setStartDate(startDate);
		return cdr;
	}

	public static CustomDateRange getStartAndEndDateForDataFiltering(LocalDate date, String period) {
		CustomDateRange dateRange = new CustomDateRange();
		LocalDate startDate = null;
		LocalDate endDate = null;
		if (period.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDate monday = date;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			startDate = monday;
			LocalDate sunday = date;
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
			endDate = sunday;
		} else if (period.equalsIgnoreCase(CommonConstant.MONTH)) {
			YearMonth month = YearMonth.from(date);
			startDate = month.atDay(1);
			endDate = month.atEndOfMonth();
		} else {
			startDate = date;
			endDate = date;
		}
		dateRange.setStartDate(startDate);
		dateRange.setEndDate(endDate);
		return dateRange;
	}

	/**
	 * CustomDateRange calculation for Cumulative data and start date is always
	 * monday for week and or 1st day of month for months calculation.
	 *
	 * @param kpiRequest
	 * @return CustomDateRange
	 */
	public static CustomDateRange getStartAndEndDatesForCumulative(KpiRequest kpiRequest) {
		int dataPoint = (int) ObjectUtils.defaultIfNull(kpiRequest.getKanbanXaxisDataPoints(), 7) - 1;
		CustomDateRange cdr = new CustomDateRange();
		LocalDate startDate = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(dataPoint);
			LocalDate monday = startDate;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			startDate = monday;
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(dataPoint);
			YearMonth month = YearMonth.from(startDate);
			startDate = month.atDay(1);
		} else {
			startDate = LocalDate.now().minusDays(dataPoint);
		}
		cdr.setStartDate(startDate);
		cdr.setEndDate(LocalDate.now());
		return cdr;
	}

	/**
	 * months calculation for how many months past data is measure Data for
	 * Cumulative
	 *
	 * @param pastMonthsCount
	 * @return startDate
	 */
	public static CustomDateRange getMonthsForPastDataHistory(int pastMonthsCount) {
		CustomDateRange cdr = new CustomDateRange();
		int dataPoint = (int) ObjectUtils.defaultIfNull(pastMonthsCount, 15) - 1;
		LocalDate endDate = LocalDate.now();
		YearMonth month = YearMonth.from(endDate.minusMonths(dataPoint));
		LocalDate startDate = month.atDay(1);
		cdr.setStartDate(startDate);
		cdr.setEndDate(endDate);
		return cdr;
	}

	/**
	 * Based on sprint details type converted sprint issue objects to jira issue
	 * number ids list
	 * 
	 * @param sprintDetails
	 * @param issueType
	 * @return
	 */
	public static List<String> getIssuesIdListBasedOnTypeFromSprintDetails(SprintDetails sprintDetails,
			String issueType) {
		if (issueType.equalsIgnoreCase(CommonConstant.COMPLETED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getCompletedIssues()).stream().filter(Objects::nonNull).map(SprintIssue::getNumber)
					.distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.NOT_COMPLETED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getNotCompletedIssues()).stream().filter(Objects::nonNull).map(SprintIssue::getNumber)
					.distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.PUNTED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getPuntedIssues()).stream().filter(Objects::nonNull).map(SprintIssue::getNumber)
					.distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getCompletedIssuesAnotherSprint()).stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.TOTAL_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getTotalIssues()).stream().filter(Objects::nonNull).map(SprintIssue::getNumber)
					.distinct().collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public static void prepareFieldMappingDefectTypeTransformation(Map<String, Object> mapOfProjectFilters,
																   FieldMapping fieldMapping, List<String> kpiWiseDefectsFieldMapping, String key) {
		if (Optional.ofNullable(fieldMapping.getJiradefecttype()).isPresent()
				&& CollectionUtils.containsAny(kpiWiseDefectsFieldMapping, fieldMapping.getJiradefecttype())) {
			kpiWiseDefectsFieldMapping.removeIf(x -> fieldMapping.getJiradefecttype().contains(x));
			kpiWiseDefectsFieldMapping.add(NormalizedJira.DEFECT_TYPE.getValue());
		}
		mapOfProjectFilters.put(key, CommonUtils.convertToPatternList(kpiWiseDefectsFieldMapping));
	}

	/**
	 * replace some details of jira issue as per sprint report
	 *
	 * @param sprintDetails
	 * @param sprintIssues
	 * @param allJiraIssue
	 * @return
	 */
	public static Set<JiraIssue> getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(SprintDetails sprintDetails,
			Set<SprintIssue> sprintIssues, List<JiraIssue> allJiraIssue) {
		Set<JiraIssue> filteredIssues = new HashSet<>();
		if (CollectionUtils.isNotEmpty(sprintIssues)) {
			sprintIssues.stream().forEach(sprintIssue -> allJiraIssue.stream().forEach(jiraIssue -> {
				if (sprintIssue.getNumber().equals(jiraIssue.getNumber()) && (jiraIssue.getBasicProjectConfigId()
						.equalsIgnoreCase(sprintDetails.getBasicProjectConfigId().toString()))) {
					JiraIssue filterJiraIssue = null;
					try {
						filterJiraIssue = (JiraIssue) jiraIssue.clone();
					} catch (CloneNotSupportedException e) {
						filterJiraIssue = jiraIssue;
						log.error("[KPIDataHelper]. exception while clone ing object jira issue{}", e);
					}
					filterJiraIssue.setStoryPoints(sprintIssue.getStoryPoints());
					filterJiraIssue.setPriority(sprintIssue.getPriority());
					filterJiraIssue.setStatus(sprintIssue.getStatus());
					filteredIssues.add(filterJiraIssue);
				}
			}));
		}
		return filteredIssues;
	}
}