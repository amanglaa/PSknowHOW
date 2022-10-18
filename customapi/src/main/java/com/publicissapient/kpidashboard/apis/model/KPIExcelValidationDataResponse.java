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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Validation Data response. Variables to be added to serve the need of other
 * KPI's.
 *
 * @author tauakram
 */
@JsonInclude(Include.NON_NULL)
public class KPIExcelValidationDataResponse {

    private String kpiName;
    private String kpiId;

    /**
     * The Map of sprint and data.
     */
    @JsonProperty("validationData")
    /* package */ Map<String, ValidationData> mapOfSprintAndData;

    /**
     * Gets map of sprint and data.
     *
     * @return the map of sprint and data
     */
    public Map<String, ValidationData> getMapOfSprintAndData() {
        return mapOfSprintAndData;
    }

    /**
     * Sets map of sprint and data.
     *
     * @param mapOfSprintAndData the map of sprint and data
     */
    public void setMapOfSprintAndData(Map<String, ValidationData> mapOfSprintAndData) {
        this.mapOfSprintAndData = mapOfSprintAndData;
    }

    /**
     * Gets kpi name.
     *
     * @return the kpi name
     */
    public String getKpiName() {
        return kpiName;
    }

    /**
     * Sets kpi name.
     *
     * @param kpiName the kpi name
     */
    public void setKpiName(String kpiName) {
        this.kpiName = kpiName;
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
     * Sets kpi id.
     *
     * @param kpiId the kpi id
     */
    public void setKpiId(String kpiId) {
        this.kpiId = kpiId;
    }

}
