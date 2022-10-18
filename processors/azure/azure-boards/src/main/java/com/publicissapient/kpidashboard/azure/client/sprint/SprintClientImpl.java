package com.publicissapient.kpidashboard.azure.client.sprint;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Hiren Babariya
 *
 */
@Service
@Slf4j
public class SprintClientImpl implements SprintClient {

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private AzureProcessorRepository azureProcessorRepository;

	/**
	 * This method process print data
	 *
	 * @param projectConfig
	 *            projectConfig
	 * @param sprintDetailsSet
	 * 				sprintDetailsSet          
	 */
	@Override
	public void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {
		ObjectId jiraProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
			sprintDetailsSet.forEach(sprint -> updateWithExistingSprintID(sprint, jiraProcessorId,
					projectConfig.getBasicProjectConfigId()));
			sprintRepository.saveAll(sprintDetailsSet);
			log.info("{} sprints found", sprintDetailsSet.size());
		}
	}

	/**
	 * update sprint id
	 * 
	 * @param sprint
	 *            sprint
	 */
	private void updateWithExistingSprintID(SprintDetails sprint, ObjectId jiraProcessorId,
			ObjectId projectBasicConfigId) {
		sprint.setProcessorId(jiraProcessorId);
		sprint.setBasicProjectConfigId(projectBasicConfigId);
		SprintDetails sprintById = sprintRepository.findBySprintID(sprint.getSprintID());
		if (sprintById != null) {
			sprint.setId(sprintById.getId());
		}
	}
}
