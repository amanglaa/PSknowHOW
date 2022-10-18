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

package com.publicissapient.kpidashboard.jira.client.release;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * The type Release data client. Store Release data for the projects in
 * persistence store
 */
@Slf4j
public class ReleaseDataClientImpl implements ReleaseDataClient {

	private final JiraAdapter jiraAdapter;
	private final ProjectReleaseRepo projectReleaseRepo;
	private final AccountHierarchyRepository accountHierarchyRepository;
	private final KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	/**
	 * Creates object
	 * 
	 * @param jiraAdapter                Jira Adapter instance
	 * @param projectReleaseRepo         Project release repository
	 * @param accountHierarchyRepository Account Hierarchy Respository
	 * @param kanbanAccountHierarchyRepo Kanban Account Hierarchy Respository
	 */
	public ReleaseDataClientImpl(JiraAdapter jiraAdapter, ProjectReleaseRepo projectReleaseRepo,
			AccountHierarchyRepository accountHierarchyRepository,
			KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo) {

		this.accountHierarchyRepository = accountHierarchyRepository;
		this.kanbanAccountHierarchyRepo = kanbanAccountHierarchyRepo;
		this.projectReleaseRepo = projectReleaseRepo;
		this.jiraAdapter = jiraAdapter;
	}

	@Override
	public void processReleaseInfo(ProjectConfFieldMapping projectConfig) {
		String projectKey = projectConfig.getJira().getProjectKey();
		boolean isKanban = projectConfig.isKanban();
		String projectName = projectConfig.getProjectName();
		if (isKanban) {
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList = kanbanAccountHierarchyRepo
					.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectConfig.getBasicProjectConfigId());
			KanbanAccountHierarchy kanbanAccountHierarchy = CollectionUtils.isNotEmpty(kanbanAccountHierarchyList)
					? kanbanAccountHierarchyList.get(0)
					: null;
			saveProjectRelease(projectKey, isKanban, projectName, null, kanbanAccountHierarchy);
		} else {
			List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository
					.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectConfig.getBasicProjectConfigId());
			AccountHierarchy accountHierarchy = CollectionUtils.isNotEmpty(accountHierarchyList)
					? accountHierarchyList.get(0)
					: null;
			saveProjectRelease(projectKey, isKanban, projectName, accountHierarchy, null);
		}
		log.info("JIRA Processor | Release Data | No hierarchy data found not processing for Version data");
	}

	/**
	 * @param projectKey
	 * @param isKanban
	 * @param projectName
	 * @param accountHierarchy
	 * @param kanbanAccountHierarchy
	 */
	private void saveProjectRelease(String projectKey, boolean isKanban, String projectName,
			AccountHierarchy accountHierarchy, KanbanAccountHierarchy kanbanAccountHierarchy) {
		List<Version> versions = jiraAdapter.getVersions(projectKey.toUpperCase());
		if (CollectionUtils.isNotEmpty(versions)) {
			if (isKanban && null != kanbanAccountHierarchy) {
				ProjectRelease projectRelease = projectReleaseRepo
						.findByConfigId(kanbanAccountHierarchy.getBasicProjectConfigId());
				projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
				projectRelease.setListProjectVersion(convertToProjectVersions(versions));
				projectRelease.setProjectName(kanbanAccountHierarchy.getNodeId());
				projectRelease.setProjectId(kanbanAccountHierarchy.getNodeId());
				projectRelease.setConfigId(kanbanAccountHierarchy.getBasicProjectConfigId());
				projectReleaseRepo.save(projectRelease);
			} else if (null != accountHierarchy) {
				ProjectRelease projectRelease = projectReleaseRepo
						.findByConfigId(accountHierarchy.getBasicProjectConfigId());
				projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
				projectRelease.setListProjectVersion(convertToProjectVersions(versions));
				projectRelease.setProjectName(accountHierarchy.getNodeId());
				projectRelease.setProjectId(accountHierarchy.getNodeId());
				projectRelease.setConfigId(accountHierarchy.getBasicProjectConfigId());
				projectReleaseRepo.save(projectRelease);
			}
			log.info("JIRA Processor | Release data | Versions saved for the project{} versions {}", projectName,
					versions);

		}
	}

	/**
	 * Converts object
	 *
	 * @param currentPagedJiraRs
	 * @return project version
	 */
	private List<ProjectVersion> convertToProjectVersions(List<Version> currentPagedJiraRs) {
		List<ProjectVersion> projectVersionList = new ArrayList<>();
		currentPagedJiraRs.forEach(version -> projectVersionList
				.add(new ProjectVersion(version.getId(), version.getName(), version.getDescription(),
						version.isArchived(), version.isReleased(), version.getReleaseDate())));
		return projectVersionList;
	}

}
