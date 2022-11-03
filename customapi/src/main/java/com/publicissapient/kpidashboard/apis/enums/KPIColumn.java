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
	/**
	 * Invalid kpi code.
	 */
	AVERAGE_RESOLUTION_TIME("kpi83", Arrays.asList("Sprint Name","Story ID","Issue Description","Issue Type","Resolution Time(In Days)")),
	LEAD_TIME("kpi3", Arrays.asList("Project Name","Intake to DOR(In Days)","DOR to DOD (In Days)","DOD TO Live (In Days)","Lead Time (In Days)")),
	SPRINT_VELOCITY("kpi39", Arrays.asList("Sprint Name","Story ID","Issue Description","Story Size(In story point)")),
	SPRINT_CAPACITY_UTILIZATION("kpi46", Arrays.asList("Sprint Name","Story ID","Issue Description","Original Time Estimate (in hours)","Total Time Spent (in hours)")),
	COMMITMENT_RELIABILITY("kpi72",Arrays.asList("Sprint Name","Story ID","Closed")),
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