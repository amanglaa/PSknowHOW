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

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.bamboo.factory.BambooClientFactory;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessor;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessorItem;
import com.publicissapient.kpidashboard.bamboo.repository.BambooJobRepository;
import com.publicissapient.kpidashboard.bamboo.repository.BambooProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides Processor Task that fetches Build information from Bamboo
 * with the help of scheduler
 */
@Component
@Slf4j
public class BambooProcessorJobExecuter extends ProcessorJobExecutor<BambooProcessor> {

	private static final String BUILD = "build";
	private boolean executionStatus = true;
	private int failureCount;
	private int newBuildCount;

	@Autowired
	private BambooProcessorRepository bambooProcessorRepository;

	@Autowired
	private BambooJobRepository bambooJobRepository;

	@Autowired
	private BuildRepository buildRepository;

	@Autowired
	private BambooConfig bambooConfig;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private BambooClientFactory bambooClientFactory;

	@Autowired
	private DeploymentRepository deploymentRepository;

	/**
	 * Initializes and calls the base parameterized constructor of
	 * {@link ProcessorJobExecutor}
	 *
	 * @param taskScheduler
	 *            gets the configured scheduler from the properties file
	 */
	@Autowired
	public BambooProcessorJobExecuter(TaskScheduler taskScheduler) {
		super(taskScheduler, ProcessorConstants.BAMBOO);
	}

	/**
	 * Provides a base processor instance of {@link BambooProcessor}
	 */
	@Override
	public BambooProcessor getProcessor() {
		return BambooProcessor.prototype();
	}

	/**
	 * Provides the ProcessorRepository instance for Bamboo to do CRUD operations on
	 * collection processor
	 */
	@Override
	public ProcessorRepository<BambooProcessor> getProcessorRepository() {
		return bambooProcessorRepository;
	}

	/**
	 * Gets the Cron expression from the properties file
	 */
	@Override
	public String getCron() {
		return bambooConfig.getCron();
	}

	/**
	 * Iterates over the enabled build jobs and adds new builds to the database.
	 *
	 * @param bambooJobsFromDb
	 *            list of enabled {@link BambooProcessorItem}s
	 * @param buildsByJobMap
	 *            maps a {@link BambooProcessorItem} to a set of {@link Build}s.
	 * @return count of new build info added in db
	 */
	private int addNewBuildsInfoToDb(BambooClient bambooClient, List<BambooProcessorItem> bambooJobsFromDb,
									 Map<BambooProcessorItem, Set<Build>> buildsByJobMap, ProcessorToolConnection bambooserver) {
		int count = 0;
		for (BambooProcessorItem job : bambooJobsFromDb) {
			// process new builds in the order of their build numbers - this has
			// implication to handling of commits in BuildEventListener
			ArrayList<Build> buildsForSpecificJob = new ArrayList<>(nullSafe(buildsByJobMap.get(job)));
			buildsForSpecificJob
					.sort((Build b1, Build b2) -> Integer.valueOf(b1.getNumber()) - Integer.valueOf(b2.getNumber()));
			List<Build> buildsToSave = new ArrayList<>();
			for (Build buildInfo : buildsForSpecificJob) {
				if (isNewBuild(job.getId(), buildInfo.getNumber())) {
					Build build = bambooClient.getBuildDetailsFromServer(buildInfo.getBuildUrl(), job.getInstanceUrl(),
							bambooserver);
					if (null != build) {
						build.setProcessorItemId(job.getId());
						build.setBuildJob(job.getJobName());
						buildsToSave.add(build);
						count++;
						log.info("Saving build info for jobName {}, jobId: {}, buildNumber() : {} in DB.",
								job.getJobName(), job.getId(), buildInfo.getNumber());
					}
				}
			}
			if (CollectionUtils.isNotEmpty(buildsToSave)) {
				buildRepository.saveAll(buildsToSave);
			}
		}
		log.info("Added {} new builds in the DB.", count);
		return count;
	}

	/**
	 * Null check safety for the builds collection
	 *
	 * @param builds
	 *            builds info
	 * @return builds or a new empty set
	 */
	private Set<Build> nullSafe(Set<Build> builds) {
		return builds == null ? new HashSet<>() : builds;
	}

	/**
	 * Adds new {@link BambooProcessorItem}s to the database.
	 *
	 * @param allJobsFromBamboo
	 *            set of all Jobs From Bamboo server {@link BambooProcessorItem}s
	 * @param existingJobsInDb
	 *            list of existing Jobs In Db {@link BambooProcessorItem}s
	 * @param processorId
	 *            uniqueId for Bamboo processor from the DB
	 */
	private void addNewBambooBuildJobsToDb(Set<BambooProcessorItem> allJobsFromBamboo,
										   List<BambooProcessorItem> existingJobsInDb, ObjectId processorId) {
		int count = 0;
		List<BambooProcessorItem> newJobs = new ArrayList<>();
		for (BambooProcessorItem job : allJobsFromBamboo) {
			BambooProcessorItem existing = null;
			if (!CollectionUtils.isEmpty(existingJobsInDb) && (existingJobsInDb.contains(job))) {
				existing = existingJobsInDb.get(existingJobsInDb.indexOf(job));
			}

			if (existing == null) {
				job.setProcessorId(processorId);
				job.setActive(true);
				job.setDesc(job.getJobName());
				newJobs.add(job);
				count++;
			}
		}
		// save all in one shot
		if (!CollectionUtils.isEmpty(newJobs)) {
			bambooJobRepository.saveAll(newJobs);
		}
		log.info("{} new bamboo jobs added to repo.", count);
	}

	/**
	 * Checks if its a new build not present in repo
	 *
	 * @param jobId
	 *            Bamboo jobId
	 * @param buildNumber
	 *            Bamboo build Number
	 * @return true if build not already present in repo
	 */
	private boolean isNewBuild(ObjectId jobId, String buildNumber) {
		return buildRepository.findByProcessorItemIdAndNumber(jobId, buildNumber) == null;
	}

	/**
	 * Runs when the scheduler is called and executes the business logic to get the
	 * Bamboo data to store in DB
	 */
	@Override
	public boolean execute(BambooProcessor processor) {
		long start = System.currentTimeMillis();
		int totalCount = 0;
		String uid = UUID.randomUUID().toString();
		MDC.put("processorExecutionUid", uid);
		ObjectId processorId = processor.getId();
		if (null != processorId) {
			MDC.put("processorId", processorId.toString());
			Set<ObjectId> bambooProcessorIds = new HashSet<>();
			bambooProcessorIds.add(processorId);

			List<ProjectBasicConfig> projectConfigList = getSelectedProjects();
			clearSelectedBasicProjectConfigIds();

			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs = getAllInformationfromDeployment(processorId);

			List<BambooProcessorItem> activeBuildJobs = new ArrayList<>();
			List<Deployment> activeDeployJobs = new ArrayList<>();
			Set<ObjectId> nonExistentToolConfig = new HashSet<>();

			MDC.put("TotalSelectedProjectsForProcessing", String.valueOf(projectConfigList.size()));
			for (ProjectBasicConfig proBasicConfig : projectConfigList) {
				log.info("Fetching data for project : {}", proBasicConfig.getProjectName());
				List<ProcessorToolConnection> bambooJobList = processorToolConnectionService
						.findByToolAndBasicProjectConfigId(ProcessorConstants.BAMBOO, proBasicConfig.getId());
				checkNonExistingTool(bambooJobList, existingDeployJobs, nonExistentToolConfig);

				if (!CollectionUtils.isEmpty(bambooJobList)) {
					totalCount = bambooJobList.size();
					processEachBambooJobOnJobType(bambooJobList, existingDeployJobs, activeBuildJobs,
							activeDeployJobs,processorId);
				}
			}
			// Delete jobs that will be no longer collected because servers have
			// moved etc.
			deleteJobs(newBuildCount, activeDeployJobs, nonExistentToolConfig);
			long end = System.currentTimeMillis();
			MDC.put("processorStartTime", String.valueOf(start));
			MDC.put("processorEndTime", String.valueOf(end));
			MDC.put("executionTime", String.valueOf(end - start));
			MDC.put("failureCount", String.valueOf(failureCount));
			MDC.put("totalJobsCount", String.valueOf(totalCount));
			MDC.put("executionStatus", String.valueOf(executionStatus));
			log.info("Bamboo Processor execution completed.");
			MDC.clear();
		}
		return executionStatus;
	}

	private void processEachBambooJobOnJobType(List<ProcessorToolConnection> bambooJobList,
											   Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, List<BambooProcessorItem> activeBuildJobs,
											   List<Deployment> activeDeployJobs, ObjectId processorId) {
		for (ProcessorToolConnection bambooJobConfig : bambooJobList) {
			String jobType = bambooJobConfig.getJobType();
			ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLogBamboo(
					bambooJobConfig.getBasicProjectConfigId().toHexString());
			processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
			MDC.put("bambooInstanceUrl", bambooJobConfig.getUrl());
			MDC.put("JobName", BUILD.equalsIgnoreCase(jobType) ? bambooJobConfig.getJobName()
					: bambooJobConfig.getDeploymentProjectId());
			bambooJobConfig.setPassword(decryptPassword(bambooJobConfig.getPassword()));
			List<BambooProcessorItem> existingBuildJobs = bambooJobRepository
					.findByProcessorIdAndToolConfigId(processorId, bambooJobConfig.getId());
			try {
				BambooClient bambooClient = bambooClientFactory.getBambooClient(jobType);
				if (BUILD.equalsIgnoreCase(jobType)) {
					newBuildCount = processBuildJob(bambooClient, existingBuildJobs, bambooJobConfig,
							processorExecutionTraceLog, activeBuildJobs, newBuildCount,processorId);
				} else {
					processDeployJob(bambooClient, existingDeployJobs, bambooJobConfig, processorExecutionTraceLog,
							activeDeployJobs,processorId);
				}

			} catch (MalformedURLException | ParseException rcp) {
				processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
				processorExecutionTraceLog.setExecutionSuccess(false);
				processorExecutionTraceLogService.save(processorExecutionTraceLog);
				log.error("Error getting jobs for: {}", bambooJobConfig.getUrl());
				failureCount++;
				executionStatus = false;
			} finally {
				MDC.remove("JobName");
				MDC.remove("bambooInstanceUrl");
			}
		}
	}

	private void checkNonExistingTool(List<ProcessorToolConnection> bambooJobList,
									  Map<Pair<ObjectId, String>, List<Deployment>> existingDeployedJobs, Set<ObjectId> nonExistentToolConfig) {
		Map<ObjectId, List<ProcessorToolConnection>> collect = bambooJobList.stream()
				.collect(Collectors.groupingBy(ProcessorToolConnection::getId));
		existingDeployedJobs.keySet().forEach(key -> {
			if (!collect.containsKey(key.getLeft())) {
				nonExistentToolConfig.add(key.getLeft());
			}
		});
	}

	private Map<Pair<ObjectId, String>, List<Deployment>> getAllInformationfromDeployment(ObjectId processorId) {
		List<Deployment> allDeployments = deploymentRepository.findAll();
		return allDeployments.stream().filter(deployment -> deployment.getProcessorId().compareTo(processorId) == 0)
				.collect(Collectors
						.groupingBy(deployment -> Pair.of(deployment.getProjectToolConfigId(), deployment.getJobId())));
	}

	private void processDeployJob(BambooClient bambooClient,
								  Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs, ProcessorToolConnection bambooJobConfig,
								  ProcessorExecutionTraceLog processorExecutionTraceLog, List<Deployment> activeJobs, ObjectId processorId)
			throws MalformedURLException, ParseException {
		Map<Pair<ObjectId, String>, Set<Deployment>> deployJobsFromBamboo = bambooClient
				.getDeployJobsFromServer(bambooJobConfig);
		Set<Deployment> deployments = addNewBambooDeploysJobsToDb(deployJobsFromBamboo, existingDeployJobs);
		Set<Deployment> saveDeployments = new HashSet<>();
		deployments.stream().forEach(deployment -> {
			if (checkDeploymentConditionsNotNull(deployment)) {
				saveDeployments.add(deployment);
			}
		});
		saveDeployJob(saveDeployments,processorId);
		activeJobs.addAll(saveDeployments);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		log.info("Finished with total deployed activeJobs count: {}", activeJobs.size());
	}

	private boolean checkDeploymentConditionsNotNull(Deployment deployment) {
		if (deployment.getEnvName() == null || deployment.getStartTime() == null || deployment.getEndTime() == null
				|| deployment.getDeploymentStatus() == null) {
			log.error("deployments conditions not satisfied so that data is not saved in db {}", deployment);
			return false;
		} else {
			return true;
		}
	}

	private void saveDeployJob(Set<Deployment> deployments, ObjectId processorId) {
		if (null != deployments) {
			deployments.forEach(deployment -> {
				deployment.setProcessorId(processorId);
				deploymentRepository.save(deployment);
				log.info("Saving deploy info for jobName {}, jobId: {}, releaseNumber() : {} in DB.",
						deployment.getJobName(), deployment.getId(), deployment.getNumber());

			});

		}
	}

	private Set<Deployment> addNewBambooDeploysJobsToDb(
			Map<Pair<ObjectId, String>, Set<Deployment>> deployJobsFromBamboo,
			Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs) {
		Set<Deployment> finalDataToSave = new HashSet<>();
		deployJobsFromBamboo.forEach((key, value) -> {
			if (existingDeployJobs.containsKey(key)) {
				finalDataToSave.addAll(checkForExistingEnvironmentRelease(key, value, existingDeployJobs));
			} else {
				// directly push all the values
				finalDataToSave.addAll(value);
			}

		});
		return finalDataToSave;

	}

	private Set<Deployment> checkForExistingEnvironmentRelease(Pair<ObjectId, String> key, Set<Deployment> value,
															   Map<Pair<ObjectId, String>, List<Deployment>> existingDeployJobs) {
		Set<Deployment> deploy = new HashSet<>();
		value.forEach(deployment -> {
			List<Deployment> existingdeployments = existingDeployJobs.get(key);
			Map<String, List<Deployment>> collect = existingdeployments.stream()
					.collect(Collectors.groupingBy(Deployment::getEnvId));
			boolean present = false;
			if (collect.containsKey(deployment.getEnvId())) {
				present = checkForCombination(collect.get(deployment.getEnvId()), deployment);
			}
			if (!present) {
				deploy.add(deployment);
			}
		});

		return deploy;
	}

	private boolean checkForCombination(List<Deployment> existingdeployments, Deployment deployment) {
		LocalDateTime bambooStart;
		LocalDateTime bambooEnd;
		if (!checkRepeatedJobs(existingdeployments, deployment)
				&& !(DeploymentStatus.IN_PROGRESS.equals(deployment.getDeploymentStatus()))) {
			List<Deployment> sortedOnEnd = existingdeployments.stream()
					.sorted((c1, c2) -> DateUtil.stringToLocalDateTime(c2.getEndTime(), DateUtil.TIME_FORMAT)
							.compareTo(DateUtil.stringToLocalDateTime(c1.getEndTime(), DateUtil.TIME_FORMAT)))
					.collect(Collectors.toList());
			LocalDateTime endDb = DateUtil.stringToLocalDateTime(sortedOnEnd.get(0).getEndTime(), DateUtil.TIME_FORMAT);
			LocalDateTime startDb = DateUtil.stringToLocalDateTime(sortedOnEnd.get(0).getStartTime(),
					DateUtil.TIME_FORMAT);
			try {
				bambooStart = DateUtil.stringToLocalDateTime(deployment.getStartTime(), DateUtil.TIME_FORMAT);
				bambooEnd = DateUtil.stringToLocalDateTime(deployment.getEndTime(), DateUtil.TIME_FORMAT);
			} catch (DateTimeParseException | NumberFormatException ex) {
				log.error("Exception while checking combination with dates " + ex);
				bambooStart = LocalDateTime.now();
				bambooEnd = LocalDateTime.now();
			}
			return endDb.isBefore(bambooEnd) && (startDb.isBefore(bambooStart)) ? false : true;
		}
		return true;
	}

	private boolean checkRepeatedJobs(List<Deployment> existingdeployments, Deployment deployment) {
		boolean repeat = false;
		for (Deployment existingDeployment : existingdeployments) {
			repeat = existingDeployment.getStartTime().equalsIgnoreCase(deployment.getStartTime());
			repeat = repeat && existingDeployment.getEndTime().equalsIgnoreCase(deployment.getEndTime());
			repeat = repeat && existingDeployment.getNumber().equalsIgnoreCase(deployment.getNumber());
			repeat = repeat && existingDeployment.getDeploymentStatus().equals(deployment.getDeploymentStatus());
			if (repeat) {
				break;
			}
		}

		return repeat;

	}

	private int processBuildJob(BambooClient bambooClient, List<BambooProcessorItem> existingJobs,
								ProcessorToolConnection bambooJobConfig, ProcessorExecutionTraceLog processorExecutionTraceLog,
								List<BambooProcessorItem> activeJobs, int newBuildCount, ObjectId processorId) throws MalformedURLException, ParseException {
		Map<BambooProcessorItem, Set<Build>> buildsByJobMap = bambooClient.getJobsFromServer(bambooJobConfig);
		log.info("Fetched builds By Job map of size: {}", buildsByJobMap.size());
		addNewBambooBuildJobsToDb(buildsByJobMap.keySet(), existingJobs, processorId);
		int updatedJobCount = addNewBuildsInfoToDb(bambooClient,
				bambooJobRepository.findEnabledJobs(processorId, bambooJobConfig.getUrl()), buildsByJobMap,
				bambooJobConfig);
		saveBambooJob(bambooJobConfig,processorId);
		activeJobs.addAll(buildsByJobMap.keySet());
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setExecutionSuccess(true);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
		log.info("Finished with activeJobs count: {}", activeJobs.size());
		return newBuildCount + updatedJobCount;
	}

	private void saveBambooJob(ProcessorToolConnection bambooJobConfig, ObjectId processorId) {
		List<BambooProcessorItem> processorItems = bambooJobRepository.findByProcessorId(processorId);
		processorItems.stream().filter(pi -> bambooJobConfig.getUrl().equals(pi.getToolDetailsMap().get("instanceUrl")))
				.forEach(item -> {
					item.setActive(true);
					item.setVersion((short) 2);
					if (item.getToolConfigId() == null) {
						item.setToolConfigId(bambooJobConfig.getId());
					}
					bambooJobRepository.save(item);
				});
	}

	private String decryptPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, "708C150A5363290AAE3F579BF3746AD5");
	}

	private void deleteJobs(int newBuildCount, List<Deployment> activeDeployJobs, Set<ObjectId> nonExistentToolConfig) {
		if (newBuildCount > 0 || !activeDeployJobs.isEmpty()) {
			cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JENKINS_KPI_CACHE);
		}
		if (!nonExistentToolConfig.isEmpty()) {
			nonExistentToolConfig
					.forEach(toolObject -> deploymentRepository.deleteDeploymentByProjectToolConfigId(toolObject));
		}
	}

	/**
	 * Cleans the cache in the Custom API
	 *
	 * @param cacheEndPoint
	 *            the cache endpoint
	 * @param cacheName
	 *            the cache name
	 */
	private void cacheRestClient(String cacheEndPoint, String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(bambooConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RestClientException e) {
			log.error("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache: {} ", cacheName);
		} else {
			log.error("[BAMBOO-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache: {}", cacheName);
		}

		clearToolItemCache(bambooConfig.getCustomApiBaseUrl());
	}

	private List<ProjectBasicConfig> getSelectedProjects() {
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();
		MDC.put("TotalConfiguredProject", String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));

		List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
		if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
			return allProjects;
		}
		return CollectionUtils.emptyIfNull(allProjects).stream().filter(
						projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
				.collect(Collectors.toList());
	}

	private void clearSelectedBasicProjectConfigIds() {
		setProjectsBasicConfigIds(null);
	}

	private ProcessorExecutionTraceLog createTraceLogBamboo(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.BAMBOO);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		return processorExecutionTraceLog;
	}

}