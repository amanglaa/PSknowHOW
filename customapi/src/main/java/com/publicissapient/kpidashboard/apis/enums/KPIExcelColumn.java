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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;
import java.util.List;

/**
 * to order the headings of excel columns
 */
public enum KPIExcelColumn {

    CODE_BUILD_TIME("kpi8", Arrays.asList("Project Name", "Job Name", "Start Time", "End Time", "Duration", "Build Status", "Started By", "Build Url", "Weeks")),
    STORY_COUNT("kpi40", Arrays.asList("Sprint Name", "Story ID", "Issue Description")),
    CODE_COMMIT("kpi11", Arrays.asList("Project Name", "Repository Url", "Branch", "Day", "No. Of Commit", "No. of Merge")),

    MEAN_TIME_TO_MERGE("kpi84", Arrays.asList("Project", "Repository Url", "Branch", "Weeks", "Mean Time To Merge (In Hours)")),
    AVERAGE_RESOLUTION_TIME("kpi83", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Issue Type", "Resolution Time(In Days)")),
    LEAD_TIME("kpi3", Arrays.asList("Project Name", "Intake to DOR(In Days)", "DOR to DOD (In Days)", "DOD TO Live (In Days)", "Lead Time (In Days)")),

    LEAD_TIME_KANBAN("kpi53", Arrays.asList("Project Name", "Open to Triage(In Days)", "Triage to Complete (In Days)", "Complete TO Live (In Days)", "Lead Time (In Days)")),
    SPRINT_VELOCITY("kpi39", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Story Size(In story point)")),
    SPRINT_CAPACITY_UTILIZATION("kpi46", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Original Time Estimate (in hours)", "Total Time Spent (in hours)")),
    COMMITMENT_RELIABILITY("kpi72", Arrays.asList("Sprint Name", "Story ID", "Closed")),


    DEFECT_INJECTION_RATE("kpi14", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Linked Defects")),

    FIRST_TIME_PASS_RATE("kpi82", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "First Time Pass")),

    DEFECT_DENSITY("kpi111", Arrays.asList("Sprint Name", "Story ID", "Issue Description", "Linked Defects")),

    DEFECT_SEEPAGE_RATE("kpi35", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Escaped Defect")),

    DEFECT_REMOVAL_EFFICIENCY("kpi34", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Defect Removed")),

    DEFECT_REJECTION_RATE("kpi37", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Defect Rejected")),

    DEFECT_COUNT_BY_PRIORITY("kpi28", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Priority")),

    DEFECT_COUNT_BY_RCA("kpi36", Arrays.asList("Sprint Name", "Defect ID", "Issue Description", "Root Cause")),

    CREATED_VS_RESOLVED_DEFECTS("kpi126", Arrays.asList("Sprint Name", "Created Defect ID", "Issue Description", "Resolved")),

    REGRESSION_AUTOMATION_COVERAGE("kpi42", Arrays.asList("Sprint Name", "Test Case ID", "Automated")),

    INSPRINT_AUTOMATION_COVERAGE("kpi16", Arrays.asList("Sprint Name", "Test Case ID", "Linked Story ID", "Automated")),

    UNIT_TEST_COVERAGE("kpi17", Arrays.asList("Project", "Job Name", "Unit Coverage", "Weeks")),

    SONAR_VIOLATIONS("kpi38", Arrays.asList("Project", "Job Name", "Sonar Violations", "Weeks")),

    SONAR_TECH_DEBT("kpi27", Arrays.asList("Project", "Job Name", "Tech Debt (in days)", "Weeks")),

    CHANGE_FAILURE_RATE("kpi116", Arrays.asList("Project", "Job Name", "Total Build Count", "Total Build Failure Count", "Build Failure Percentage", "Weeks")),

    TEST_EXECUTION_AND_PASS_PERCENTAGE("kpi70", Arrays.asList("Sprint Name", "Total Test", "Executed Test", "Execution %", "Passed Test", "Passed %")),

    COST_OF_DELAY("kpi113", Arrays.asList("Project Name", "Cost of Delay", "Epic ID", "Epic Name", "Epic End Date", "Month")),

    RELEASE_FREQUENCY("kpi73", Arrays.asList("Project Name", "Release Name", "Release Description", "Release End Date", "Month")),

    DEPLOYMENT_FREQUENCY("kpi118", Arrays.asList("Project Name", "Date", "Job Name", "Month", "Environment")),

    DEFECTS_WITHOUT_STORY_LINK("kpi80", Arrays.asList("Sprint Name","Priority","Defects Without Story Link")),

    TEST_WITHOUT_STORY_LINK("kpi79", Arrays.asList("Project Name","Test Case ID","Linked to Story")),

    PRODUCTION_DEFECTS_AGEING("kpi127", Arrays.asList("Project Name", "Defect ID", "Priority", "Date", "Status")),

    INVALID("INVALID_KPI", Arrays.asList("Invalid"));

    // @formatter:on

    private String kpiId;

    private List<String> columns;

    KPIExcelColumn(String kpiID, List<String> columns) {
        this.kpiId = kpiID;
        this.setColumns(columns);
    }

    /**
     * Gets kpi id.
     *
     * @return the kpi id
     */
    public String getKpiId() {
        return kpiId;
    }


    /**
     * Gets source.
     *
     * @return the source
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Sets source.
     *
     * @return the source
     */
    private void setColumns(List<String> columns) {
        this.columns = columns;
    }

}