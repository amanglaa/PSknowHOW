package com.publicissapient.kpidashboard.azure.client.sprint;

import java.util.Set;

import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author Hiren Babariya
 *
 */
public interface SprintClient {

	/**
	 * This method process print data
	 *
	 * @param projectConfig
	 *            projectConfig
	 * @param sprintDetailsSet
	 *            sprintDetailsSet
	 */
	 void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet);

}
