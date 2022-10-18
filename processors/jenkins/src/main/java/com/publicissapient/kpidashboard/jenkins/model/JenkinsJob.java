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

package com.publicissapient.kpidashboard.jenkins.model;

import com.publicissapient.kpidashboard.common.model.generic.JobProcessorItem;

/**
 * ProcessorItem extension to store the instance, build job and build url.
 */
public class JenkinsJob extends JobProcessorItem {

	/**
	 * Overridden method of Object's equals method.
	 * 
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		JenkinsJob jenkinsJob = (JenkinsJob) obj;

		return getInstanceUrl().equals(jenkinsJob.getInstanceUrl()) && getJobName().equals(jenkinsJob.getJobName());
	}

	/**
	 * Overridden method of Object's hashCode method.
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		int result = getInstanceUrl().hashCode();
		result = 31 * result + getJobName().hashCode();
		return result;
	}
}
