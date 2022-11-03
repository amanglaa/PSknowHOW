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

package com.publicissapient.kpidashboard.apis.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Excel Data for KPIs
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KPIExcelData {

    @JsonProperty("Sprint Name")
    private String sprintName;

    @JsonProperty("Story ID")
    private Map<String, String> storyId;

    @JsonProperty("Issue Description")
    private String issueDesc;

    @JsonProperty("Linked Defects")
    private Map<String, String> linkedDefects;

    @JsonProperty("Project Name")
    private String projectName;

    @JsonProperty("Epic ID")
    private Map<String, String> epicID;

    @JsonProperty("Cost of Delay")
    private Double costOfDelay;

    @JsonProperty("Epic Name")
    private String epicName;

    @JsonProperty("Epic End Date")
    private String epicEndDate;

    @JsonProperty("Release Name")
    private String releaseName;

    @JsonProperty("Release Description")
    private String releaseDesc;

    @JsonProperty("Release End Date")
    private String releaseEndDate;

    @JsonProperty("Date")
    private String deploymentDate;

    @JsonProperty("Job Name")
    private String deploymentJobName;

    @JsonProperty("Environment")
    private String deploymentEnvironment;

    @JsonProperty("Month")
    private String month;
}
