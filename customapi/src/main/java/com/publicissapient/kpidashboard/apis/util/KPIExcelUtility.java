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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import org.apache.commons.lang3.StringUtils;

/**
 * The class contains mapping of kpi and Excel columns.
 *
 * @author pkum34
 */
public class KPIExcelUtility {

    private KPIExcelUtility() {
    }

    private static final String LEAD_TIME = "Lead Time";
    private static final String INTAKE_TO_DOR = "Intake - DoR";
    private static final String DOR_TO_DOD = "DoR - DoD";
    private static final String DOD_TO_LIVE = "DoD - Live";
    private static final DecimalFormat df2 = new DecimalFormat(".##");

    /**
     * This method populate the excel data for DIR KPI
     *
     * @param sprint
     * @param storyIds
     * @param defects
     * @param kpiExcelData
     * @param issueData
     */
    public static void populateDirExcelData(String sprint, List<String> storyIds, List<JiraIssue> defects,
                                            List<KPIExcelData> kpiExcelData, Map<String, JiraIssue> issueData) {
        storyIds.forEach(story -> {
            Map<String, String> linkedDefects = defects.stream().filter(d -> d.getDefectStoryID().contains(story))
                    .map(defect -> {
                        if (StringUtils.isEmpty(defect.getUrl())) {
                            defect.setUrl("");
                        }
                        return defect;
                    }).collect(Collectors.toMap(JiraIssue::getNumber, JiraIssue::getUrl));
            KPIExcelData excelData = new KPIExcelData();
            excelData.setSprintName(sprint);
            excelData.setLinkedDefects(linkedDefects);
            if (MapUtils.isNotEmpty(issueData)) {
                JiraIssue jiraIssue = issueData.get(story);
                if (null != jiraIssue) {
                    excelData.setIssueDesc(jiraIssue.getName());
                    Map<String, String> storyId = new HashMap<>();
                    storyId.put(story, jiraIssue.getUrl());
                    excelData.setStoryId(storyId);
                }
            }
            kpiExcelData.add(excelData);
        });
    }

    public static void populateSprintVelocity(String sprint, Map<String, JiraIssue> totalStoriesMap,
                                              List<KPIExcelData> kpiExcelData) {


        if (MapUtils.isNotEmpty(totalStoriesMap)) {
            totalStoriesMap.forEach((storyId, jiraIssue) -> {

                KPIExcelData excelData = new KPIExcelData();
                excelData.setSprintName(sprint);
                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(storyId, jiraIssue.getUrl());
                excelData.setStoryId(storyDetails);
                excelData.setIssueDesc(jiraIssue.getName());
                excelData.setStoryPoints(jiraIssue.getStoryPoints().toString());


                kpiExcelData.add(excelData);
            });
        }
    }

    public static void populateSprintCapacity(String sprint, Map<String, JiraIssue> totalStoriesMap,
                                              List<KPIExcelData> kpiExcelData, Double estimateTime) {

        List<String> originalTimeList = new ArrayList<>();
        List<String> loggedTimeList = new ArrayList<>();
        KPIExcelData excelData = new KPIExcelData();
        originalTimeList.add(df2.format(estimateTime));


        if (MapUtils.isNotEmpty(totalStoriesMap)) {
            totalStoriesMap.forEach((storyId, jiraIssue) -> {
                excelData.setSprintName(sprint);
                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(storyId, jiraIssue.getUrl());
                excelData.setStoryId(storyDetails);
                excelData.setIssueDesc(jiraIssue.getName());
                Double daysLogged;
                if (jiraIssue.getTimeSpentInMinutes() != null) {
                    daysLogged = Double.valueOf(jiraIssue.getTimeSpentInMinutes()) / 60;
                    loggedTimeList.add(df2.format(daysLogged));
                }
                excelData.setOriginalTimeEstimate(originalTimeList.toString());
                excelData.setTotalTimeSpent(loggedTimeList.toString());
                kpiExcelData.add(excelData);
            });
        }
    }

    public static void populateAverageResolutionTime(Map<String, List<ResolutionTimeValidation>> sprintWiseResolution,
                                                     List<KPIExcelData> kpiExcelData) {


        if (MapUtils.isNotEmpty(sprintWiseResolution)) {
            sprintWiseResolution.forEach((sprint, resolutionTimesValidationList) -> {

                KPIExcelData excelData = new KPIExcelData();
                excelData.setSprintName(sprint);
                resolutionTimesValidationList.stream().forEach(resolutionTimeValidation -> {
                    Map<String, String> storyDetails = new HashMap<>();
                    storyDetails.put(resolutionTimeValidation.getIssueNumber(), resolutionTimeValidation.getUrl());
                    excelData.setStoryId(storyDetails);
                    excelData.setIssueDesc(resolutionTimeValidation.getIssueDescription());
                    excelData.setIssueType(resolutionTimeValidation.getIssueType());
                    excelData.setResolutionTime(resolutionTimeValidation.getResolutionTime().toString());
                    kpiExcelData.add(excelData);

                });

            });
        }
    }


    public static void populateSprintCountExcelData(String sprint, Map<String, JiraIssue> totalStoriesMap,
                                                    List<KPIExcelData> kpiExcelData) {


        if (MapUtils.isNotEmpty(totalStoriesMap)) {
            totalStoriesMap.forEach((storyId, jiraIssue) -> {

                KPIExcelData excelData = new KPIExcelData();
                excelData.setSprintName(sprint);

                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(storyId, jiraIssue.getUrl());
                excelData.setStoryId(storyDetails);
                excelData.setIssueDesc(jiraIssue.getName());


                kpiExcelData.add(excelData);
            });
        }
    }

    public static void populateLeadTime(
            List<KPIExcelData> kpiExcelData, String projectName, Map<String, Long> cycleMap) {


        if (MapUtils.isNotEmpty(cycleMap)) {

            KPIExcelData excelData = new KPIExcelData();
            excelData.setProjectName(projectName);
            excelData.setIntakeToDOR(cycleMap.get(INTAKE_TO_DOR).toString());
            excelData.setDorToDod(cycleMap.get(DOR_TO_DOD).toString());
            excelData.setDodToLive(cycleMap.get(DOD_TO_LIVE).toString());
            excelData.setLeadTime(cycleMap.get(LEAD_TIME).toString());
            kpiExcelData.add(excelData);

        }
    }

    /**
     * TO GET Constant.EXCEL_YES/"N" from complete list of defects if defect is
     * present in conditional list then Constant.EXCEL_YES else
     * Constant.EMPTY_STRING kpi specific
     *
     * @param sprint
     * @param totalStoriesMap
     * @param conditionStories
     * @param kpiExcelData
     */

    public static void populateCommittmentReliability(String sprint, Map<String, JiraIssue> totalStoriesMap,
                                                          List<JiraIssue> conditionStories, List<KPIExcelData> kpiExcelData) {
        if (MapUtils.isNotEmpty(totalStoriesMap)) {
            List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
                    .collect(Collectors.toList());
            totalStoriesMap.forEach((storyId, jiraIssue) -> {
                String present = conditionalList.contains(storyId) ? Constant.EXCEL_YES : Constant.EMPTY_STRING;
                KPIExcelData excelData = new KPIExcelData();
                excelData.setSprintName(sprint);
                excelData.setIssueDesc(checkEmptyName(jiraIssue));
                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(storyId, checkEmptyURL(jiraIssue));
                excelData.setStoryId(storyDetails);
                excelData.setStatus(present);



                kpiExcelData.add(excelData);
            });
        }
    }

   /* public static void populateCommittmentReliability(String sprint, List<KPIExcelData> kpiExcelData,
                                                      Map<String, JiraIssue> totalIssueNumbers, List<JiraIssue> completedIssueNumber) {
        if (MapUtils.isNotEmpty(totalIssueNumbers)){
            totalIssueNumbers.forEach((story,jiraissue)->{
                completedIssueNumber.stream().filter(completedIssue-> co)
                String present = conditionalList.contains(testIssue.getNumber()) ? Constant.EXCEL_YES
                        : Constant.EMPTY_STRING;
                List<String> conditionalList = conditionStories.stream().map(JiraIssue::getNumber)
                        .collect(Collectors.toList());
            });
            KPIExcelData excelData = new KPIExcelData();
            excelData.setSprintName(sprint);
            totalPresentJiraIssue.stream().forEach(data -> {
                Map<String, String> storyDetails = new HashMap<>();
                storyDetails.put(data.getNumber(),data.getUrl());
                excelData.setStoryId(storyDetails);

                if(data.getStatus().equals("Closed")){
                    excelData.setStatus("Y");
                }
                else{
                    excelData.setStatus(" ");
                }


                kpiExcelData.add(excelData);
        });
        }
    }*/
    private static String checkEmptyName(JiraIssue jiraIssue) {
        return StringUtils.isEmpty(jiraIssue.getName()) ? Constant.EMPTY_STRING : jiraIssue.getName();
    }



    private static String checkEmptyURL(JiraIssue jiraIssue) {
        return StringUtils.isEmpty(jiraIssue.getUrl()) ? Constant.EMPTY_STRING : jiraIssue.getUrl();
    }
}