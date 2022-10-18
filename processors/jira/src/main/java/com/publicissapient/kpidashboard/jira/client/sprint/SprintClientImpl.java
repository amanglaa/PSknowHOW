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
package com.publicissapient.kpidashboard.jira.client.sprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yasbano
 *
 */

@Service
@Slf4j
public class SprintClientImpl implements SprintClient {

	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;

	/**
	 * This method handles sprint detailsList
	 * 
	 * @param projectConfig
	 *            projectConfig
	 * @param sprintDetailsSet
	 *            sprintDetailsSet
	 */
	@Override
	public void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			JiraAdapter jiraAdapter) {
		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
			List<String> sprintIds = sprintDetailsSet.stream().map(SprintDetails::getSprintID)
					.collect(Collectors.toList());
			List<SprintDetails> dbSprints = sprintRepository.findBySprintIDIn(sprintIds);
			Map<String, ObjectId> dbSprintIdAndId = dbSprints.stream()
					.collect(Collectors.toMap(SprintDetails::getSprintID, SprintDetails::getId));
			List<SprintDetails> sprintToRemove = new ArrayList<>();
			sprintDetailsSet.forEach(sprint -> {
				sprint.setProcessorId(jiraProcessorId);
				sprint.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
				if (null != dbSprintIdAndId.get(sprint.getSprintID())) {
					sprint.setId(dbSprintIdAndId.get(sprint.getSprintID()));
					if (! (Arrays.asList(SprintDetails.SPRINT_STATE_CLOSED, SprintDetails.SPRINT_STATE_FUTURE)
							.contains(sprint.getState()))) {
						log.info("Sprint details fetch for sprintID : {}, BoardId: {} ", sprint.getOriginalSprintId(),
								sprint.getOriginBoardId());
						getSprintReport(sprint, jiraAdapter, projectConfig);
					} else {
						log.info("Sprint not to be saved again : {}, status: {} ", sprint.getOriginalSprintId(),
								sprint.getState());
						sprintToRemove.add(sprint);
					}
				} else {
					getSprintReport(sprint, jiraAdapter, projectConfig);
				}
			});
			if (CollectionUtils.isNotEmpty(sprintToRemove)) {
				sprintDetailsSet.removeAll(sprintToRemove);
			}
			sprintRepository.saveAll(sprintDetailsSet);
			log.info("{} sprints found", sprintDetailsSet.size());
		}
	}

	private void getSprintReport(SprintDetails sprint, JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		if(sprint.getOriginalSprintId() != null && sprint.getOriginBoardId() != null){
			jiraAdapter.getSprintReport(projectConfig, sprint.getOriginalSprintId(),
					sprint.getOriginBoardId(), sprint);
		}
	}
}
