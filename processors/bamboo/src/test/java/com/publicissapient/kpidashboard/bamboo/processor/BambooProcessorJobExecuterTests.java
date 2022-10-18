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

package com.publicissapient.kpidashboard.bamboo.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientBuildImpl;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientDeployImpl;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.bamboo.factory.BambooClientFactory;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessor;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessorItem;
import com.publicissapient.kpidashboard.bamboo.repository.BambooJobRepository;
import com.publicissapient.kpidashboard.bamboo.repository.BambooProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@RunWith(MockitoJUnitRunner.class)
public class BambooProcessorJobExecuterTests {

	private static final String EXCEPTION = "rest client exception";
	private static final String JOB2_URL = "JOB2_URL";
	private static final String JOB1_1_URL = "JOB1_1_URL";
	private static final String JOB1_URL = "JOB1_URL";
	private static final String HTTP_URL = "http://does:matter@bamboo.com";
	private static final String SERVER1 = "server1";
	private static final List<ProcessorToolConnection> pt = new ArrayList<>();
	private static final List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private static final List<Deployment> deploymentList = new ArrayList<>();
	private static final List<Deployment> queuedDeploymentList = new ArrayList<>();
	private static final List<Deployment> serverList = new ArrayList<>();
	private static final List<Deployment> maxDeployment = new ArrayList<>();
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER = new ProcessorToolConnection();// new
	// BambooServer(HTTP_URL,
	// "", "does",
	// "matter");
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER1 = new ProcessorToolConnection();// new
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER2 = new ProcessorToolConnection();// new
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private BambooClient bambooClient;
	@Mock
	private BambooJobRepository bambooJobRepository;
	@Mock
	private BambooProcessorRepository bambooProcessorRepository;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private DeploymentRepository deploymentRepository;
	@Mock
	private BambooConfig bambooConfig;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private BambooClientFactory bambooClientFactory;
	@Mock
	private BambooClientBuildImpl bambooClientBuild;
	@Mock
	private BambooClientDeployImpl bambooClientDeploy;
	@InjectMocks
	private BambooProcessorJobExecuter task;

	// BambooServer(HTTP_URL,
	// HTTP_URL,
	// "does",
	// null);

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		BambooProcessor bambooProcessor = new BambooProcessor();
		Mockito.when(bambooConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");

		BAMBOOSAMPLESERVER.setId(new ObjectId());
		BAMBOOSAMPLESERVER.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER.setJobName("IN");
		BAMBOOSAMPLESERVER.setBranch("branch");
		BAMBOOSAMPLESERVER.setToolName("Bamboo");
		BAMBOOSAMPLESERVER.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER.setUsername("does");
		BAMBOOSAMPLESERVER.setPassword("matter");
		BAMBOOSAMPLESERVER.setJobType("build");

		BAMBOOSAMPLESERVER1.setId(new ObjectId());
		BAMBOOSAMPLESERVER1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER1.setJobName("IN");
		BAMBOOSAMPLESERVER1.setBranch("branch");
		BAMBOOSAMPLESERVER1.setToolName("Bamboo");
		BAMBOOSAMPLESERVER1.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER1.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER1.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER1.setUsername("does");
		BAMBOOSAMPLESERVER1.setPassword(null);
		BAMBOOSAMPLESERVER1.setJobType("build");

		BAMBOOSAMPLESERVER2.setId(new ObjectId("6296661b307f0239477f1e9e"));// toolId
		BAMBOOSAMPLESERVER2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER2.setJobName("IN");
		BAMBOOSAMPLESERVER2.setBranch("branch");
		BAMBOOSAMPLESERVER2.setToolName("Bamboo");
		BAMBOOSAMPLESERVER2.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER2.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER2.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER2.setUsername("does");
		BAMBOOSAMPLESERVER2.setPassword(null);
		BAMBOOSAMPLESERVER2.setJobType("deploy");
		BAMBOOSAMPLESERVER2.setDeploymentProjectName("KnowHowDeployemnt");
		BAMBOOSAMPLESERVER2.setDeploymentProjectId("190709761");

		pt.add(BAMBOOSAMPLESERVER);
		pt.add(BAMBOOSAMPLESERVER1);
		pt.add(BAMBOOSAMPLESERVER2);

		ProjectBasicConfig basicConfig = new ProjectBasicConfig();
		basicConfig.setId(new ObjectId("60b7dbb489c5974a407e923b"));
		basicConfig.setId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		projectConfigList.add(basicConfig);

		Deployment deployment = new Deployment();
		deployment.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment.setEnvId("190775300");
		deployment.setStartTime("1970-01-01T00:00:00.000Z");
		deployment.setEndTime("1970-01-01T00:00:00.000Z");
		deployment.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
		deployment.setJobId("190709761");
		deployment.setNumber("189988914");
		deployment.setJobName("KnowHowDeployemnt");

		Deployment deployment1 = new Deployment();
		deployment1.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment1.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment1.setEnvId("190775300");
		deployment1.setStartTime("2022-06-02T14:38:54.000Z");
		deployment1.setEndTime("2022-06-02T14:38:54.000Z");
		deployment1.setDeploymentStatus(DeploymentStatus.SUCCESS);
		deployment1.setJobId("190709761");
		deployment1.setNumber("189988914");
		deployment1.setJobName("KnowHowDeployemnt");

		Deployment deployment2 = new Deployment();
		deployment2.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment2.setProjectToolConfigId(new ObjectId("6706661b307f0239477f1e9e"));
		deployment2.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment2.setEnvId("190775300");
		deployment2.setStartTime("1970-01-01T00:00:00.000Z");
		deployment2.setEndTime("1970-01-01T00:00:00.000Z");
		deployment2.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
		deployment2.setJobId("190709761");
		deployment2.setNumber("189988914");
		deployment2.setJobName("KnowHowDeployemnt");

		Deployment deployment3 = new Deployment();
		deployment3.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment3.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment3.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment3.setEnvId("190775300");
		deployment3.setEnvId("190775300");
		deployment3.setStartTime("2022-06-02T14:39:08.000Z");
		deployment3.setEndTime("2022-06-02T14:39:13.000Z");
		deployment3.setDeploymentStatus(DeploymentStatus.SUCCESS);
		deployment3.setJobId("190709761");
		deployment3.setNumber("189988914");
		deployment3.setJobName("KnowHowDeployemnt");
		queuedDeploymentList.add(deployment);
		deploymentList.add(deployment);
		deploymentList.add(deployment1);
		deploymentList.add(deployment2);
		serverList.add(deployment);
		serverList.add(deployment1);
		maxDeployment.add(deployment);
		maxDeployment.add(deployment1);
		maxDeployment.add(deployment3);

	}

	@Test
	public void collectNoBuildServersNothingAdded() {
		task.execute(BambooProcessor.prototype());
		verifyNoInteractions(bambooClient, buildRepository);
	}

	@Test
	public void testClean() throws MalformedURLException, ParseException {
		task.execute(processorWithOneServer());
		verifyNoMoreInteractions(bambooClient, buildRepository);
	}

	@Test
	public void collectJobsAdded() throws MalformedURLException, ParseException {
		try {
			BambooProcessor processor = processorWithOneServer();
			BambooProcessorItem job = bambooJob("1", HTTP_URL, JOB1_URL);
			Build build = build("1", JOB1_1_URL);
			when(bambooClient.getJobsFromServer(any())).thenReturn(oneJobWithBuilds(job, build));
			List<BambooProcessorItem> bambooJobs = new ArrayList<>();
			BambooProcessorItem bambooJob = bambooJob("1", SERVER1, JOB1_URL);
			bambooJob.setProcessorId(new ObjectId());
			BambooProcessorItem job1 = bambooJob("1", SERVER1, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
			job2.setProcessorId(new ObjectId());

			bambooJobs.add(bambooJob);
			bambooJobs.add(job1);
			bambooJobs.add(job2);

			when(bambooJobRepository.findByProcessorId(any())).thenReturn(Lists.newArrayList(job2));
			when(bambooJobRepository.findByProcessorIdIn(any())).thenReturn(Lists.newArrayList(bambooJob));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void collectJobsAddedWithNiceName() throws MalformedURLException, ParseException {

		try {
			BambooProcessor processor = processorWithOneServer();
			BambooProcessorItem job = bambooJob("1", HTTP_URL, JOB1_URL);
			Build build = build("1", JOB1_1_URL);
			when(bambooClient.getJobsFromServer(any())).thenReturn(oneJobWithBuilds(job, build));

			List<BambooProcessorItem> bambooJobs = new ArrayList<>();
			BambooProcessorItem bambooJob = bambooJob("1", HTTP_URL, JOB1_URL);
			BambooProcessorItem job1 = bambooJob("1", HTTP_URL, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", HTTP_URL, JOB2_URL);
			job2.setProcessorId(processor.getId());

			bambooJobs.add(bambooJob);
			bambooJobs.add(job1);
			bambooJobs.add(job2);
			when(bambooJobRepository.findByProcessorId(any())).thenReturn(bambooJobs);
			when(bambooJobRepository.findEnabledJobs(any(), any())).thenReturn(bambooJobs);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(deploymentRepository.findAll()).thenReturn(deploymentList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(pt);
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void collectTwoJobsJobsAddedRandomOrder() throws MalformedURLException, ParseException {
		try {
			BambooProcessor processor = processorWithOneServer();

			Build build = build("1", JOB1_1_URL);
			when(bambooClient.getJobsFromServer(any())).thenReturn(twoJobsWithTwoBuildsRandom(SERVER1));
			when(bambooClient.getBuildDetailsFromServer(any(), any(), any())).thenReturn(build);
			BambooProcessorItem job1 = bambooJob("1", SERVER1, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
			job2.setProcessorId(processor.getId());
			List<BambooProcessorItem> jobs = new ArrayList<>();
			jobs.add(job1);
			jobs.add(job2);
			when(bambooJobRepository.findEnabledJobs(any(), any())).thenReturn(jobs);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(deploymentRepository.findAll()).thenReturn(deploymentList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(pt);
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processor);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}

	}

	@Test
	public void collectOneJobExistsNotAdded() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		BambooProcessorItem job = bambooJob("1", SERVER1, JOB1_URL);
		task.execute(processor);

		verify(bambooJobRepository, never()).save(job);
	}

	@Test
	public void deleteJob() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		processor.setId(ObjectId.get());
		BambooProcessorItem job1 = bambooJob("1", SERVER1, JOB1_URL);
		job1.setProcessorId(processor.getId());
		BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
		job2.setProcessorId(processor.getId());
		List<BambooProcessorItem> jobs = new ArrayList<>();
		jobs.add(job1);
		jobs.add(job2);
		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		when(bambooJobRepository.findByProcessorIdIn(udId)).thenReturn(jobs);
		task.execute(processor);
		List<BambooProcessorItem> delete = new ArrayList<>();
		delete.add(job2);
		verify(bambooJobRepository, never()).deleteAll(Mockito.anyList());
	}

	@Test
	public void deleteNeverJob() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		processor.setId(ObjectId.get());
		BambooProcessorItem job1 = bambooJob("1", SERVER1, JOB1_URL);
		job1.setProcessorId(processor.getId());
		List<BambooProcessorItem> jobs = new ArrayList<>();
		jobs.add(job1);
		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		when(bambooJobRepository.findByProcessorIdIn(udId)).thenReturn(jobs);
		when(processorToolConnectionService.findByTool(ProcessorConstants.BAMBOO)).thenReturn(twoBambooJob());
		task.execute(processor);
		List<BambooProcessorItem> delete = new ArrayList<>();
		BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
		delete.add(job2);
		verify(bambooJobRepository, never()).deleteAll(delete);
	}

	@Test
	public void collectJobNotEnabledBuildNotAdded() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		BambooProcessorItem job = bambooJob("1", SERVER1, JOB1_URL);
		Build build = build("1", JOB1_1_URL);

		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledBuildExistsBuildNotAdded() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		BambooProcessorItem job = bambooJob("1", SERVER1, JOB1_URL);
		Build build = build("1", JOB1_1_URL);
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledNewBuildBuildAdded() throws MalformedURLException, ParseException {
		try {
			BambooProcessor processor = processorWithOneServer();
			BambooProcessorItem job = bambooJob("1", HTTP_URL, JOB1_URL);
			Build build = build("1", JOB1_1_URL);

			BambooProcessorItem job1 = bambooJob("1", HTTP_URL, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
			job2.setProcessorId(processor.getId());
			List<BambooProcessorItem> jobs = new ArrayList<>();
			jobs.add(job);
			jobs.add(job1);
			jobs.add(job2);
			Set<ObjectId> udId = new HashSet<>();
			udId.add(processor.getId());

			when(bambooClient.getJobsFromServer(Mockito.any(ProcessorToolConnection.class)))
					.thenReturn(oneJobWithBuilds(job, build));
			when(bambooJobRepository.findByProcessorId(processor.getId())).thenReturn(jobs);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}

	}

	@Test
	public void testAddNewBuildsInfoToDb_buildnull_success() throws Exception {
		try {
			BambooProcessor processor = processorWithOneServer();
			BambooProcessorItem job = bambooJob("1", HTTP_URL, JOB1_URL);
			Build build = build("1", JOB1_1_URL);
			BambooProcessorItem job1 = bambooJob("1", HTTP_URL, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
			job2.setProcessorId(processor.getId());
			List<BambooProcessorItem> jobs = new ArrayList<>();
			jobs.add(job);
			jobs.add(job1);
			jobs.add(job2);
			Whitebox.invokeMethod(task, "addNewBuildsInfoToDb", bambooClientBuild, jobs, oneJobWithBuilds(job, build),
					BAMBOOSAMPLESERVER);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	/**
	 * Test when build is available to save
	 */
	@Test
	public void testAddNewBuildsInfoToDb_buildNotNull_success() throws Exception {

		try {
			BambooProcessor processor = processorWithOneServer();
			BambooProcessorItem job = bambooJob("1", HTTP_URL, JOB1_URL);
			Build build = build("1", JOB1_1_URL);
			BambooProcessorItem job1 = bambooJob("1", HTTP_URL, JOB1_URL);
			job1.setProcessorId(processor.getId());
			BambooProcessorItem job2 = bambooJob("2", SERVER1, JOB2_URL);
			job2.setProcessorId(processor.getId());
			List<BambooProcessorItem> jobs = new ArrayList<>();
			jobs.add(job);
			jobs.add(job1);
			jobs.add(job2);
			when(bambooClient.getBuildDetailsFromServer(any(), any(), any())).thenReturn(build);

			Whitebox.invokeMethod(task, "addNewBuildsInfoToDb", bambooClientBuild, jobs, oneJobWithBuilds(job, build),
					BAMBOOSAMPLESERVER);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	/**
	 * Test when there is no tool config available
	 */
	@Test
	public void testProcessorToolConnectionisNull_success() {
		try {
			when(processorToolConnectionService.findByTool(any())).thenReturn(null);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForDeployedJobs() throws MalformedURLException, ParseException {
		try {
			when(bambooClientDeploy.getDeployJobsFromServer(any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(deploymentList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForNewDeployedJobsWithInProgress() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek mili jo queued hai
			when(bambooClientDeploy.getDeployJobsFromServer(any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForMaxDeployedJobs() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek mili jo queued hai
			when(bambooClientDeploy.getDeployJobsFromServer(any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(maxDeployment)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForFirstDeploymentQueuedJobs() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(new ArrayList<>());
			when(bambooClientDeploy.getDeployJobsFromServer(any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void deleteFromDeployments() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek tool Extra
			when(bambooClientDeploy.getDeployJobsFromServer(any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(oneLessTool());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	private List<ProcessorToolConnection> oneLessTool() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId("6296661b307f0239477f1e9e"));
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("deploy");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId("6296661b307f0239477f1e9e"));
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("deploy");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;

	}

	private BambooProcessor processorWithOneServer() {
		BambooProcessor processor = BambooProcessor.prototype();
		processor.setId(new ObjectId());
		return processor;
	}

	private Map<BambooProcessorItem, Set<Build>> oneJobWithBuilds(BambooProcessorItem job, Build... builds) {
		Map<BambooProcessorItem, Set<Build>> jobs = new HashMap<>();
		jobs.put(job, Sets.newHashSet(builds));
		return jobs;
	}

	private Map<Pair<ObjectId, String>, Set<Deployment>> oneDeployJob(Pair<ObjectId, String> id,
			Set<Deployment> deployments) {
		Map<Pair<ObjectId, String>, Set<Deployment>> jobs = new HashMap<>();
		jobs.put(id, deployments);
		return jobs;

	}

	private Map<BambooProcessorItem, Set<Build>> twoJobsWithTwoBuildsRandom(String server) {
		Map<BambooProcessorItem, Set<Build>> jobs = new HashMap<>();
		BambooProcessorItem bpi = bambooJob("2", server, JOB2_URL);
		bpi.setId(new ObjectId());
		BambooProcessorItem bpi2 = bambooJob("1", server, JOB1_URL);
		bpi.setId(new ObjectId());
		jobs.put(bpi, Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		jobs.put(bpi2, Sets.newHashSet(build("1", JOB1_1_URL), build("2", "JOB1_2_URL")));
		return jobs;
	}

	private BambooProcessorItem bambooJob(String jobName, String instanceUrl, String jobUrl) {
		BambooProcessorItem job = new BambooProcessorItem();
		job.setJobName(jobName);
		job.setInstanceUrl(instanceUrl);
		job.setJobUrl(jobUrl);
		return job;
	}

	private Build build(String number, String url) {
		Build build = new Build();
		build.setNumber(number);
		build.setBuildUrl(url);
		return build;
	}

	private List<ProcessorToolConnection> bambooJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		toolList.add(t1);
		return toolList;
	}

	private List<ProcessorToolConnection> twoBambooJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId());
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("build");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId());
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("build");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;
	}

	private List<ProcessorToolConnection> twoBambooDeployJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId());
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("deploy");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId());
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("deploy");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;
	}

	private List<Connection> twoConnectionJob() {
		List<Connection> connectionList = Lists.newArrayList();
		Connection c1 = new Connection();
		c1.setId(new ObjectId("5f9014743cb73ce896167658"));
		c1.setConnectionName("Bamboo Connection");
		c1.setType("Bamboo");
		c1.setBaseUrl(HTTP_URL);
		c1.setUsername("does");
		c1.setPassword("fdaaa");
		Connection c2 = new Connection();
		c2.setId(new ObjectId("5f9014743cb73ce896167659"));
		c2.setConnectionName("Bamboo Connection");
		c2.setType("Bamboo");
		c2.setBaseUrl(HTTP_URL);
		c2.setUsername("does");
		c2.setPassword("fdaaa");
		connectionList.add(c1);
		connectionList.add(c2);
		return connectionList;
	}

	private Set<String> connectionIdList() {
		return Sets.newHashSet("5f9014743cb73ce896167658");
	}
}
