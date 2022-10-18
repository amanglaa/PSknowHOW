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

package com.publicissapient.kpidashboard.processor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;
import com.publicissapient.kpidashboard.jenkins.factory.JenkinsClientFactory;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsJob;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.JenkinsProcessorJobExecutor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsBuildClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsDeployClient;
import com.publicissapient.kpidashboard.jenkins.repository.JenkinsJobRepository;
import com.publicissapient.kpidashboard.jenkins.repository.JenkinsProcessorRepository;

@ExtendWith(SpringExtension.class)
public class JenkinsProcessorTaskTests {

	@InjectMocks
	private JenkinsProcessorJobExecutor task;
	@Mock
	private TaskScheduler taskScheduler;
	@Mock
	private JenkinsProcessorRepository jenkinsProcessorRepository;
	@Mock
	private JenkinsClientFactory jenkinsClientFactory;
	@Mock
	private JenkinsJobRepository jenkinsJobRepository;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private JenkinsClient jenkinsClient;
	@Mock
	private JenkinsConfig jenkinsConfig;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	private static final String SERVER1 = "server1";
	private static final String NICENAME1 = "niceName1";
	private List<ProcessorToolConnection> connList = new ArrayList<>();
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();

	private static final ProcessorToolConnection JENKINSSAMPLESERVER = new ProcessorToolConnection();

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		JenkinsProcessor processor = new JenkinsProcessor();
		JENKINSSAMPLESERVER.setUrl("http://does:matter@jenkins.com");
		JENKINSSAMPLESERVER.setUsername("does");
		JENKINSSAMPLESERVER.setApiKey("matter");
		JENKINSSAMPLESERVER.setJobName("JOB1");
		JENKINSSAMPLESERVER.setJobType("build");
		JENKINSSAMPLESERVER.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));

		connList.add(JENKINSSAMPLESERVER);

		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfigList.add(projectConfig);
		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));

		Mockito.when(jenkinsConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
	}

	@Test
	public void collect_noBuildServers_nothingAdded() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(new HashMap<JenkinsJob, Set<Build>>());

		JenkinsProcessor jenkinsProcessor = new JenkinsProcessor();
		task.execute(jenkinsProcessor);
		verifyNoInteractions(jenkinsClient, buildRepository);
	}

	@Test
	public void collect_noJobsOnServer_nothingAdded() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(new HashMap<JenkinsJob, Set<Build>>());
		task.execute(processorWithOneServer());
		verifyNoMoreInteractions(jenkinsClient, buildRepository);
	}

	@Test
	public void collect_twoJobs_jobsAdded() {

		List<JenkinsJob> jenkinsJob = new ArrayList<>();
		JenkinsJob jenJob = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, true);
		jenJob.setProcessorId(ObjectId.get());
		jenkinsJob.add(jenJob);
		ProcessorExecutionTraceLog p1 = new ProcessorExecutionTraceLog();
		p1.setBasicProjectConfigId("62171d0f26dd266803fa87da");
		JenkinsClient client2 = mock(JenkinsClient.class);
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		pl.add(p1);

		projectConfig.setId(new ObjectId("62171d0f26dd266803fa87da"));
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(twoJobsWithTwoBuilds(SERVER1, NICENAME1));
		when(jenkinsJobRepository.findJob(any(), any(), any(), any())).thenReturn(jenJob);
		when(processorExecutionTraceLogService.getTraceLogs("Jenkins", "62171d0f26dd266803fa87da")).thenReturn(pl);
		task.execute(processorWithOneServer());
		verify(jenkinsJobRepository, times(0)).saveAll(Mockito.anyList());
	}

	@Test
	public void collect_twoJobs_jobsAdded_random_order() {

		List<JenkinsJob> jenkinsJobs = new ArrayList<>();
		JenkinsJob jenkinsJob = jenkinsJob("2", SERVER1, "JOB2_URL", NICENAME1, true);
		jenkinsJob.setProcessorId(ObjectId.get());
		jenkinsJobs.add(jenkinsJob);
		JenkinsClient client2 = mock(JenkinsClient.class);

		when(jenkinsJobRepository.save(any(JenkinsJob.class))).thenReturn(jenkinsJob);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(twoJobsWithTwoBuildsRandom(SERVER1, NICENAME1));

		task.execute(processorWithOneServer());
		verify(jenkinsJobRepository, times(0)).saveAll(Mockito.anyList());
	}

	@Test
	public void collect_oneJob_exists_notAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		JenkinsJob job = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, false);
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(oneJobWithBuilds(job));
		when(jenkinsJobRepository.findJob(processor.getId(), SERVER1, job.getJobName(), connList.get(0).getId()))
				.thenReturn(job);

		task.execute(processor);

		verify(jenkinsJobRepository, never()).save(job);
	}

	@Test
	public void delete_job() {
		JenkinsProcessor processor = processorWithOneServer();
		processor.setId(ObjectId.get());
		JenkinsJob job1 = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, true);
		job1.setProcessorId(processor.getId());
		JenkinsJob job2 = jenkinsJob("2", SERVER1, "JOB2_URL", NICENAME1, false);
		job2.setProcessorId(processor.getId());
		List<JenkinsJob> jobs = new ArrayList<>();
		jobs.add(job1);
		jobs.add(job2);
		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(oneJobWithBuilds(job1));
		task.execute(processor);
		List<JenkinsJob> delete = new ArrayList<>();
		delete.add(job2);
		verify(jenkinsJobRepository, times(0)).deleteAll(delete);
	}

	// @Test
	public void collect_jobNotEnabled_buildNotAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		JenkinsJob job = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, false);
		Build build = build("1", "JOB1_1_URL");

		when(jenkinsClient.getBuildJobsFromServer(JENKINSSAMPLESERVER)).thenReturn(oneJobWithBuilds(job, build));
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collect_jobEnabled_buildExists_buildNotAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		JenkinsJob job = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, false);
		Build build = build("1", "JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);

		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(JENKINSSAMPLESERVER)).thenReturn(oneJobWithBuilds(job, build));
		when(jenkinsJobRepository.findJob(any(), any(), any(), any())).thenReturn(job);
		when(buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(build);
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collect_jobEnabled_newBuild_buildAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		JenkinsJob job = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, true);
		Build build = build("1", "JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(jenkinsJobRepository.findJob(any(), any(), any(), any())).thenReturn(job);
		when(client2.getBuildJobsFromServer(any())).thenReturn(oneJobWithBuilds(job, build));
		when(buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(null);

		assertTrue(task.execute(processor));
	}

	@Test
	public void collect_clean() {
		JenkinsProcessor processor = processorWithOneServer();
		List<JenkinsJob> jenkinsJobs = new ArrayList<>();
		JenkinsJob jenkinsJob = jenkinsJob("1", SERVER1, "JOB1_URL", NICENAME1, true);
		ObjectId id = ObjectId.get();
		processor.setId(id);
		jenkinsJob.setProcessorId(id);
		jenkinsJobs.add(jenkinsJob);
		Map<ProcessorType, List<ProcessorItem>> processorItem = new HashMap<ProcessorType, List<ProcessorItem>>();
		processorItem.put(ProcessorType.BUILD, Arrays.asList(getProcessorItems(id)));
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(new HashMap<JenkinsJob, Set<Build>>());

		assertTrue(task.execute(processor));

	}

	@Test
	public void collect_enable_Job() {

		List<JenkinsJob> jenkinsJobs = new ArrayList<>();
		JenkinsJob jenkinsJob = jenkinsJob("1", "http://does:matter@jenkins.com", "JOB1_URL", NICENAME1, true);
		jenkinsJob.setProcessorId(ObjectId.get());
		ProjectBasicConfig p1;
		jenkinsJobs.add(jenkinsJob);
		jenkinsJob.setToolConfigId(new ObjectId("62171d0f26dd266803fa87da"));
		JenkinsClient client2 = mock(JenkinsClient.class);

		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any())).thenReturn(twoJobsWithTwoBuilds(SERVER1, NICENAME1));
		when(jenkinsJobRepository.findJob(any(), any(), any(), any())).thenReturn(jenkinsJob);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId("Jenkins",
				new ObjectId("62171d0f26dd266803fa87da"))).thenReturn(connList);
		task.execute(processorWithOneServer());
		verify(jenkinsJobRepository, times(0)).saveAll(Mockito.anyList());
	}

	@Test
	public void CheckJenkinsClientSelector() {
		JenkinsBuildClient jenkinsClientMock = mock(JenkinsBuildClient.class);
		JenkinsDeployClient jenkins2ClientMock = mock(JenkinsDeployClient.class);
		JenkinsClientFactory clientSelector = new JenkinsClientFactory(jenkinsClientMock, jenkins2ClientMock);

		assertSame(clientSelector.getJenkinsClient("build"), jenkinsClientMock);
		assertSame(clientSelector.getJenkinsClient("deploy"), jenkins2ClientMock);
	}

	private JenkinsProcessor processorWithOneServer() {
		return JenkinsProcessor.buildProcessor();
	}

	private ProcessorItem getProcessorItems(ObjectId id) {
		ProcessorItem item = new ProcessorItem();
		item.setProcessorId(id);
		return item;
	}

	private Map<JenkinsJob, Set<Build>> oneJobWithBuilds(JenkinsJob job, Build... builds) {
		Map<JenkinsJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(job, Sets.newHashSet(builds));
		return jobs;
	}

	private Map<JenkinsJob, Set<Build>> twoJobsWithTwoBuilds(String server, String niceName) {
		Map<JenkinsJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(jenkinsJob("1", server, "JOB1_URL", niceName, true),
				Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		jobs.put(jenkinsJob("2", server, "JOB2_URL", niceName, true),
				Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		return jobs;
	}

	private Map<JenkinsJob, Set<Build>> twoJobsWithTwoBuildsRandom(String server, String niceName) {
		Map<JenkinsJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(jenkinsJob("2", server, "JOB2_URL", niceName, true),
				Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		jobs.put(jenkinsJob("1", server, "JOB1_URL", niceName, true),
				Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		return jobs;
	}

	private JenkinsJob jenkinsJob(String jobName, String instanceUrl, String jobUrl, String niceName,
			boolean isEnabled) {
		JenkinsJob job = new JenkinsJob();
		job.setJobName(jobName);
		job.setInstanceUrl(instanceUrl);
		job.setJobUrl(jobUrl);
		job.setActive(isEnabled);
		job.setProcessorId(ObjectId.get());
		return job;
	}

	private Build build(String number, String url) {
		Build build = new Build();
		build.setNumber(number);
		build.setBuildUrl(url);
		return build;
	}

	private List<ProjectToolConfig> jenkinsJob() {
		List<ProjectToolConfig> toolList = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setToolName("Jenkins");
		t1.setJobName("1");
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		toolList.add(t1);
		return toolList;
	}

	private Optional<Connection> jenkinsConnection() {
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setBaseUrl("http://does:matter@jenkins.com");
		conn.get().setUsername("does");
		conn.get().setApiKey("matter");
		conn.get().setId(new ObjectId("5f9014743cb73ce896167658"));
		return conn;
	}

}