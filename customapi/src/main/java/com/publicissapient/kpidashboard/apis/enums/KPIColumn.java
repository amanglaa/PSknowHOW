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
 * The enum Kpi code.
 *
 * @author tauakram Mapping of MasterData KPI Code with KPI Name.
 */
public enum KPIColumn {

	DEFECT_INJECTION_RATE("kpi14", Arrays.asList("Sprint Name","Story ID","Issue Description","Linked Defects")),

	FIRST_TIME_PASS_RATE("kpi82", Arrays.asList("Sprint Name","Story ID","Issue Description","First Time Pass")),

	DEFECT_DENSITY("kpi111",  Arrays.asList("Sprint Name","Story ID","Issue Description","Linked Defects")),

	DEFECT_SEEPAGE_RATE("kpi35", Arrays.asList("Sprint Name","Defect ID","Issue Description","Escaped Defect")),

	DEFECT_REMOVAL_EFFICIENCY("kpi34", Arrays.asList("Sprint Name","Defect ID","Issue Description","Defect Removed")),

	DEFECT_REJECTION_RATE("kpi37", Arrays.asList("Sprint Name","Defect ID","Issue Description","Defect Rejected")),

	DEFECT_COUNT_BY_PRIORITY("kpi28", Arrays.asList("Sprint Name","Defect ID","Issue Description","Priority")),

	DEFECT_COUNT_BY_RCA("kpi36",  Arrays.asList("Sprint Name","Defect ID","Issue Description","Root Cause")),

	CREATED_VS_RESOLVED_DEFECTS("kpi126", Arrays.asList("Sprint Name","Created Defect ID","Issue Description","Resolved")),

	REGRESSION_AUTOMATION_COVERAGE("kpi42", Arrays.asList("Sprint Name","Test Case ID","Automated")),

	INSPRINT_AUTOMATION_COVERAGE("kpi16", Arrays.asList("Sprint Name","Test Case ID","Linked Story ID","Automated")),

	UNIT_TEST_COVERAGE("kpi17", Arrays.asList("Project","Job Name","Unit Coverage","Weeks")),

	SONAR_VIOLATIONS("kpi38", Arrays.asList("Project","Job Name","Sonar Violations","Weeks")),

	SONAR_TECH_DEBT("kpi27", Arrays.asList("Project","Job Name","Tech Debt (in days)","Weeks")),

	CHANGE_FAILURE_RATE("kpi116", Arrays.asList("Project","Job Name","Total Build Count","Total Build Failure Count","Build Failure Percentage","Weeks")),

	TEST_EXECUTION_AND_PASS_PERCENTAGE("kpi70", Arrays.asList("Sprint Name","Total Test","Executed Test","Execution %","Passed Test","Passed %")),
		/**
	 * Invalid kpi code.
	 */
	INVALID("INVALID_KPI", Arrays.asList("Invalid"));

	// @formatter:on

	private String kpiId;

	private List<String> columns;

	KPIColumn(String kpiID, List<String> columns) {
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