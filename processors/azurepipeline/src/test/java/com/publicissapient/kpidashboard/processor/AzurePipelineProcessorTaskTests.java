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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.azurepipeline.config.AzurePipelineConfig;
import com.publicissapient.kpidashboard.azurepipeline.factory.AzurePipelineFactory;
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineJob;
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineProcessor;
import com.publicissapient.kpidashboard.azurepipeline.processor.AzurePipelineProcessorJobExecutor;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.AzurePipelineDeploymentClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.DefaultAzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.repository.AzurePipelineJobRepository;
import com.publicissapient.kpidashboard.azurepipeline.repository.AzurePipelineProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@ExtendWith(SpringExtension.class)
public class AzurePipelineProcessorTaskTests {


	@InjectMocks
	private AzurePipelineProcessorJobExecutor task;
	@Mock
	private TaskScheduler taskScheduler;
	@Mock
	private AzurePipelineProcessorRepository azurePipelineProcessorRepository;
	@Mock
	private AzurePipelineJobRepository azurePipelineJobRepository;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private AzurePipelineClient azurePipelineClient;
	@Mock
	private AzurePipelineConfig config;
	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private AzurePipelineConfig azurePipelineConfig;
	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private AzurePipelineFactory azurePipelineFactory;

	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;
	
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private DefaultAzurePipelineClient buildClient;

	@Mock
	private AzurePipelineDeploymentClient deployClient;

	@Mock
	private DeploymentRepository deploymentRepository;

	
	private static final String SERVER1 = "server1";
	private static final String NICENAME1 = "niceName1";
	private static final ProcessorToolConnection AZUREPIPELINE_SAMPLE_SERVER = new ProcessorToolConnection();
	private static final long LASTUPDATEDTIME = 0;

	Map<String, List<ProjectToolConfig>> azurePipelineJobFromConfig = Maps.newHashMap();
	List<ProjectBasicConfig> listProjectBasicConfig = new ArrayList<>();
	List<ProcessorToolConnection> listProcessorToolConnection = new ArrayList<>();
	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		AzurePipelineProcessor processor = new AzurePipelineProcessor();
		Mockito.when(azurePipelineProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(processor);
		Mockito.when(azurePipelineProcessorRepository.save(processor)).thenReturn(processor);
		Mockito.when(azurePipelineConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		when(processorToolConnectionService.findByTool(ProcessorConstants.AZUREPIPELINE)).thenReturn(new ArrayList<>());
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(anyString(),any())).thenReturn(new ArrayList<>());
		AZUREPIPELINE_SAMPLE_SERVER.setUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		AZUREPIPELINE_SAMPLE_SERVER.setApiVersion("5.1");
		AZUREPIPELINE_SAMPLE_SERVER.setJobName("1");
		AZUREPIPELINE_SAMPLE_SERVER.setPat("patKey");
		//projectBasicConfig = ProjectBasicConfig.builder().build();
		ProjectBasicConfig projectBasicConfig=new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("507f191e810c19729de860ea"));
		projectBasicConfig.setConsumerCreatedOn("consumerCreatedOn");
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("projectName");
		projectBasicConfig.setUpdatedAt("updatedAt");
		ProjectBasicConfig projectBasicConfig2=new ProjectBasicConfig();
		projectBasicConfig2.setId(new ObjectId("507f191e810c19729de860ea"));
		projectBasicConfig2.setConsumerCreatedOn("consumerCreatedOn2");
		projectBasicConfig2.setIsKanban(false);
		projectBasicConfig2.setProjectName("projectName2");
		projectBasicConfig2.setUpdatedAt("updatedAt2");
		listProjectBasicConfig.add(projectBasicConfig);
		listProjectBasicConfig.add(projectBasicConfig2);
		
		ProcessorToolConnection processorToolConnection= new ProcessorToolConnection();
		processorToolConnection.setId(new ObjectId("507f191e810c19729de860ea"));
		processorToolConnection.setToolName("toolName");
		processorToolConnection.setProjectId("projectId");
		processorToolConnection.setProjectKey("projectKey");
		ProcessorToolConnection processorToolConnection2= new ProcessorToolConnection();
		processorToolConnection.setId(new ObjectId("507f191e810c19729de860ea"));
		processorToolConnection.setToolName("toolName2");
		processorToolConnection.setProjectId("projectId2");
		processorToolConnection.setProjectKey("projectKey2");
		processorToolConnection.setJobType("Build");
		processorToolConnection2.setJobType("Build");
		listProcessorToolConnection.add(processorToolConnection);
		listProcessorToolConnection.add(processorToolConnection2);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(anyString(),any())).thenReturn(listProcessorToolConnection);
	}

	@Test
	public void collectNoBuildServersNothingAdded() {
		when(azurePipelineFactory.getAzurePipelineClient("Build")).thenReturn(buildClient);
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		when(buildClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(new HashMap<AzurePipelineJob,Set<Build>>());
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		AzurePipelineProcessor azurePipelineProcessor = new AzurePipelineProcessor();

		task.execute(azurePipelineProcessor);
		verifyNoInteractions(buildRepository);
	}


	@Test
	public void collectNoJobsOnServerNothingAdded() {
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(new HashMap<AzurePipelineJob, Set<Build>>());
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());

		task.execute(processorWithOneServer());
		verifyNoMoreInteractions(buildRepository);
	}

	@Test
	public void collectTwoJobsJobsAdded() {

		List<AzurePipelineJob> azurePipelineJob = new ArrayList<>();
		AzurePipelineJob azureJob = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1,
				NICENAME1, true);
		azureJob.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		azurePipelineJob.add(azureJob);

		AzurePipelineProcessor processor = processorWithOneServer();
		processor.setId(new ObjectId("507f191e810c19729de860ea"));

		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(azurePipelineJob);
		when(azurePipelineClient.getInstanceJobs(any(), Mockito.anyLong()))
				.thenReturn(twoJobsWithTwoBuilds("https://dev.azure.com/sundeepm/AzureSpeedy", NICENAME1));
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(azurePipelineJobRepository.findByProcessorId(Mockito.any())).thenReturn(azurePipelineJob);
		when(azurePipelineJobRepository.findEnabledJobs(processor.getId(),
				"https://dev.azure.com/sundeepm/AzureSpeedy")).thenReturn(azurePipelineJob);
		when(azurePipelineJobRepository.saveAll(Mockito.anyList())).thenReturn(null);
		when(buildRepository.findByProcessorItemIdAndNumber(Mockito.any(), Mockito.anyString())).thenReturn(null);
		when(buildRepository.findByProcessorItemIdAndBuildJob(Mockito.any(), Mockito.anyString()))
				.thenReturn(listOfBuilds());

		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	@Test
	public void collectTwoJobsJobsAddedDeploy() {

		List<Deployment> deployments = new ArrayList<>();
		Deployment deployJob = new Deployment();
		deployJob.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		deployments.add(deployJob);

		AzurePipelineProcessor processor = processorWithOneServer();
		processor.setId(new ObjectId("507f191e810c19729de860ea"));

		when(deploymentRepository.findByProcessorIdIn(Mockito.any())).thenReturn(deployments);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(deploymentRepository.findByProcessorIdIn(Mockito.any())).thenReturn(deployments);
		when(deploymentRepository.saveAll(Mockito.anyList())).thenReturn(null);
		when(deploymentRepository.findByProjectToolConfigIdAndJobName(Mockito.any(), Mockito.anyString())).thenReturn(null);
		when(deploymentRepository.findByProcessorIdIn(anyCollection())).thenReturn(listOfDeployments());

		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	@Test
	public void collectTwoJobsJobsAddedRandomOrder() {
		// List<Connection> dataConnection1 = new ArrayList<>();
		// Connection
		// azurePipelineJob1=azurePipelineJob1("5f9014743cb73ce896167658","https://dev.azure.com/sundeepm/AzureSpeedy","");
		// dataConnection1.add(dataConnection);
		List<AzurePipelineJob> azurePipelineJobs = new ArrayList<>();
		AzurePipelineJob azurePipelineJob = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1,
				NICENAME1, true);
		azurePipelineJob.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		azurePipelineJobs.add(azurePipelineJob);

		AzurePipelineProcessor processor = processorWithOneServer();

		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(azurePipelineJobs);
		when(azurePipelineJobRepository.findByProcessorId(Mockito.any())).thenReturn(azurePipelineJobs);
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(twoJobsWithTwoBuildsRandom(SERVER1, NICENAME1));
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(azurePipelineJobRepository.findEnabledJobs(processor.getId(),
				"https://dev.azure.com/sundeepm/AzureSpeedy")).thenReturn(azurePipelineJobs);

		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	@Test
	public void collectOneJobExistsNotAdded() {
		AzurePipelineProcessor processor = processorWithOneServer();
		AzurePipelineJob job = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1, NICENAME1,
				false);
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(oneJobWithBuilds(job));
		when(azurePipelineJobRepository.findJob(processor.getId(), SERVER1, job.getJobName())).thenReturn(job);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());

		task.execute(processor);
		verify(azurePipelineJobRepository, never()).save(job);
	}

	@Test
	public void collectOneJobExistsNotAddedDeploy() {
		AzurePipelineProcessor processor = processorWithOneServer();
		Deployment deployJob = new Deployment();
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());

		task.execute(processor);
		verify(deploymentRepository, never()).save(deployJob);
	}

	@Test
	public void deleteJob() {
		AzurePipelineProcessor processor = processorWithOneServer();
		List<AzurePipelineJob> azurePipelineJobs = new ArrayList<>();
		AzurePipelineJob azurePipelineJob = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1,
				NICENAME1, true);
		azurePipelineJob.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		azurePipelineJobs.add(azurePipelineJob);
		
		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(twoJobsWithTwoBuildsRandom(SERVER1, NICENAME1));
		when(azurePipelineJobRepository.findByProcessorIdIn(udId)).thenReturn(azurePipelineJobs);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		task.execute(processor);
		List<AzurePipelineJob> delete = new ArrayList<>();
		verify(azurePipelineJobRepository,never()).deleteAll(delete);
	}

	@Test
	public void deleteNeverJob() {
		AzurePipelineProcessor processor = processorWithOneServer();
		processor.setId(new ObjectId("507f191e810c19729de860ea"));
		AzurePipelineJob job1 = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1, NICENAME1,
				false);
		job1.setProcessorId(processor.getId());
		List<AzurePipelineJob> jobs = new ArrayList<>();
		jobs.add(job1);
		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(jobs);
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(oneJobWithBuilds(job1));
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}


	@Test
	public void deleteJobDeploy() {
		AzurePipelineProcessor processor = processorWithOneServer();
		List<Deployment> deployJobs = new ArrayList<>();
		Deployment deploy = new Deployment();
		deploy.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		deployJobs.add(deploy);

		Set<ObjectId> udId = new HashSet<>();
		udId.add(processor.getId());
		when(deployClient.getDeploymentJobs(any(), any(Long.class)))
				.thenReturn(twoJobsWithTwoDeployRandom(SERVER1,NICENAME1));
		when(deploymentRepository.findByProcessorIdIn(udId)).thenReturn(deployJobs);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(null);
		task.execute(processor);
		List<Deployment> delete = new ArrayList<>();
		verify(deploymentRepository,never()).deleteAll(delete);
	}

	@Test
	public void collectJobNotEnabledBuildNotAdded() {
		AzurePipelineProcessor processor = processorWithOneServer();
		AzurePipelineJob job = azurePipelineJob("1", SERVER1, "JOB1_URL", NICENAME1, false);
		Build build = build("1", "JOB1_1_URL");

		when(azurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER, LASTUPDATEDTIME))
				.thenReturn(oneJobWithBuilds(job, build));
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledBuildExistsBuildNotAdded() {
		AzurePipelineProcessor processor = processorWithOneServer();
		AzurePipelineJob job = azurePipelineJob("1", SERVER1, "JOB1_URL", NICENAME1, false);
		Build build = build("1", "JOB1_1_URL");
		AzurePipelineClient client2 = mock(AzurePipelineClient.class);
		when(client2.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER, LASTUPDATEDTIME))
				.thenReturn(oneJobWithBuilds(job, build));
		when(azurePipelineJobRepository.findEnabledJobs(processor.getId(), SERVER1)).thenReturn(Arrays.asList(job));
		when(buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(build);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledNewBuildAdded() {
		AzurePipelineProcessor processor = processorWithOneServer();
		AzurePipelineJob job = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1, NICENAME1,
				true);
		Build build = build("1", "JOB1_1_URL");
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(azurePipelineJobRepository.findEnabledJobs(processor.getId(),
				"https://dev.azure.com/sundeepm/AzureSpeedy")).thenReturn(Arrays.asList(job));
		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(Arrays.asList(job));
		when(azurePipelineJobRepository.findByProcessorId(Mockito.any())).thenReturn(Arrays.asList(job));
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(oneJobWithBuilds(job, build));
		when(buildRepository.findByProcessorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(null);

		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	@SuppressWarnings("unused")
	@Test
	public void collectClean() {
		AzurePipelineProcessor processor = processorWithOneServer();
		List<AzurePipelineJob> azurePipelineJobs = new ArrayList<>();
		AzurePipelineJob azurePipelineJob = azurePipelineJob("1", SERVER1, "JOB1_URL", NICENAME1, true);
		ObjectId id = ObjectId.get();
		processor.setId(id);
		azurePipelineJob.setProcessorId(id);
		azurePipelineJobs.add(azurePipelineJob);
		Map<ProcessorType, List<ProcessorItem>> processorItem = new HashMap<ProcessorType, List<ProcessorItem>>();
		processorItem.put(ProcessorType.BUILD, Arrays.asList(getProcessorItems(id)));
		AzurePipelineClient client2 = mock(AzurePipelineClient.class);
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(client2.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(new HashMap<AzurePipelineJob, Set<Build>>());
		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(azurePipelineJobs);

		task.execute(processor);
		verify(azurePipelineJobRepository, never()).save(azurePipelineJob);
	}

	@Test
	public void collectEnableJob() {

		List<AzurePipelineJob> azurePipelineJobs = new ArrayList<>();
		AzurePipelineJob azurePipelineJob = azurePipelineJob("1", "https://dev.azure.com/sundeepm/AzureSpeedy", SERVER1,
				NICENAME1, true);
		azurePipelineJob.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		azurePipelineJobs.add(azurePipelineJob);
		AzurePipelineProcessor processor = processorWithOneServer();

		when(azurePipelineJobRepository.findByProcessorIdIn(Mockito.any())).thenReturn(azurePipelineJobs);
		when(azurePipelineClient.getInstanceJobs(any(), any(Long.class)))
				.thenReturn(twoJobsWithTwoBuilds(SERVER1, NICENAME1));
		when(projectToolConfigRepository.findByToolName("AzurePipeline")).thenReturn(azurePipelineJob());
		when(azurePipelineJobRepository.findByProcessorId(Mockito.any())).thenReturn(azurePipelineJobs);
		when(azurePipelineJobRepository.findEnabledJobs(processor.getId(), "server1")).thenReturn(azurePipelineJobs);

		boolean actualStatus = task.execute(processor);
		boolean expectedStatus = true;
		assertEquals(expectedStatus, actualStatus);
	}

	private AzurePipelineProcessor processorWithOneServer() {
		return AzurePipelineProcessor.buildProcessor();
	}

	private ProcessorItem getProcessorItems(ObjectId id) {
		ProcessorItem item = new ProcessorItem();
		item.setProcessorId(id);
		return item;
	}

	private Map<AzurePipelineJob, Set<Build>> oneJobWithBuilds(AzurePipelineJob job, Build... builds) {
		Map<AzurePipelineJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(job, Sets.newHashSet(builds));
		return jobs;
	}

	private Map<AzurePipelineJob, Set<Build>> twoJobsWithTwoBuilds(String server, String niceName) {
		Map<AzurePipelineJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(azurePipelineJob("1", server, SERVER1, niceName, true),
				Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		jobs.put(azurePipelineJob("2", server, SERVER1, niceName, true),
				Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		return jobs;
	}

	private Map<AzurePipelineJob, Set<Build>> twoJobsWithTwoBuildsRandom(String server, String niceName) {
		Map<AzurePipelineJob, Set<Build>> jobs = new HashMap<>();
		jobs.put(azurePipelineJob("2", server, "JOB2_URL", niceName, true),
				Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		jobs.put(azurePipelineJob("1", server, "JOB1_URL", niceName, true),
				Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		return jobs;
	}

	private Map<Deployment, Set<Deployment>> twoJobsWithTwoDeployRandom(String server, String niceName) {
		Map<Deployment, Set<Deployment>> jobs = new HashMap<>();
		Deployment deployJob1=new Deployment();
		deployJob1.setDeploymentStatus(DeploymentStatus.SUCCESS);
		deployJob1.setProjectToolConfigId(new ObjectId("507f191e810c19729de860ea"));
		deployJob1.setStartTime("100");
		deployJob1.setEndTime("499");
		jobs.put(deployJob1,Sets.newHashSet());
		return jobs;
	}

	private AzurePipelineJob azurePipelineJob(String jobName, String instanceUrl, String jobUrl, String niceName,
			boolean isEnabled) {
		AzurePipelineJob job = new AzurePipelineJob();
		job.setJobName(jobName);
		job.setInstanceUrl(instanceUrl);
		job.setJobUrl(jobUrl);
		job.setActive(isEnabled);
		job.setProcessorId(new ObjectId("507f191e810c19729de860ea"));
		return job;
	}

	private Build build(String number, String url) {
		Build build = new Build();
		build.setNumber(number);
		build.setBuildUrl(url);
		return build;
	}

	private List<ProjectToolConfig> azurePipelineJob() {
		List<ProjectToolConfig> toolList = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setToolName("AzurePipeline");
		t1.setApiVersion("5.1");
		t1.setJobName("1");
		toolList.add(t1);
		return toolList;
	}

	private List<Build> listOfBuilds() {
		List<Build> builds = new ArrayList<>();
		Build b1 = new Build();
		b1.setNumber("1");
		b1.setBuildUrl("https://azurepipeline/build/1");
		b1.setStartTime(100);
		Build b2 = new Build();
		b2.setNumber("2");
		b2.setBuildUrl("https://azurepipeline/build/2");
		b2.setStartTime(200);

		builds.add(b1);
		builds.add(b2);

		return builds;
	}

	private List<Deployment> listOfDeployments() {
		List<Deployment> releases = new ArrayList<>();
		Deployment d1 = new Deployment();
		d1.setNumber("1");
		d1.setEnvUrl("https://azurepipeline/deploy/1");
		d1.setStartTime(String.valueOf(100));
		Deployment d2 = new Deployment();
		d2.setNumber("2");
		d2.setEnvUrl("https://azurepipeline/build/2");
		d2.setStartTime(String.valueOf(200));

		releases.add(d1);
		releases.add(d2);

		return releases;
	}

}