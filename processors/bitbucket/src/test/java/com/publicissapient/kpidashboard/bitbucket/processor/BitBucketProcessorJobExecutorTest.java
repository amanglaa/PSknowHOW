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

package com.publicissapient.kpidashboard.bitbucket.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.bitbucket.factory.BitBucketClientFactory;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketProcessor;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.BitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.common.BasicBitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.repository.BitbucketProcessorRepository;
import com.publicissapient.kpidashboard.bitbucket.repository.BitbucketRepoRepository;
import com.publicissapient.kpidashboard.bitbucket.util.BitbucketRestOperations;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
class BitBucketProcessorJobExecutorTest {

	/** The processorid. */
	private final ObjectId PROCESSORID = new ObjectId("5e2ac020e4b098db0edf5145");

	@Mock
	private BitbucketRestOperations bitbucketRestOperations;

	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private BitBucketConfig bitBucketConfig;

	@Mock
	private BitbucketProcessorRepository bitBucketProcessorRepo;

	@Mock
	private BitbucketRepoRepository bitBucketRepository;

	@Mock
	private BitBucketClient bitBucketClient;

	@Mock
	private CommitRepository commitsRepo;

	
	@Mock
	private MergeRequestRepository mergReqRepo;
	
	@Mock
	private RestOperations restOperations;

	@Mock
	private BasicBitBucketClient basicBitBucketClient;

	@InjectMocks
	private BitBucketProcessorJobExecutor bitBucketProcessorJobExecutor;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	
	@Mock
	private BitBucketClientFactory bitBucketClientFactory;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private BitBucketServerClient bitBucketServerClient;
	
	private List<ProjectBasicConfig> projectConfigList;

	@BeforeEach
	public void setUp() {
		bitBucketProcessorJobExecutor = new BitBucketProcessorJobExecutor(taskScheduler);
		Mockito.when(bitbucketRestOperations.getTypeInstance()).thenReturn(new RestTemplate());
		basicBitBucketClient = new BasicBitBucketClient(bitBucketConfig, bitbucketRestOperations, aesEncryptionService);

		BitbucketProcessor bitBucketProcessor = new BitbucketProcessor();
		Mockito.when(bitBucketProcessorRepo.findByProcessorName(Mockito.anyString())).thenReturn(bitBucketProcessor);
		Mockito.when(bitBucketProcessorRepo.save(bitBucketProcessor)).thenReturn(bitBucketProcessor);
		
		 MockitoAnnotations.initMocks(this);    
		    projectConfigList = new ArrayList<>();
			ProjectBasicConfig p = new ProjectBasicConfig();
			p.setId(PROCESSORID);
			p.setProjectName("projectName");
			projectConfigList.add(p);
	}

	@Test
	void testGetCron() {
		Mockito.when(bitBucketConfig.getCron()).thenReturn("0 0 0/12 * * *");
		assertEquals("0 0 0/12 * * *", bitBucketConfig.getCron());
	}

	
	
	@Test
	void testExecute() throws FetchingCommitException {
		BitbucketProcessor bitbucketProcessor = BitbucketProcessor.prototype();
		bitbucketProcessor.setProcessorType(ProcessorType.SCM);
		bitbucketProcessor.setProcessorName("BitBucket");
		bitbucketProcessor.setId(PROCESSORID);
		List<BitbucketRepo> bitbucketRepos = new ArrayList<>();
		BitbucketRepo bitbucketRepo = new BitbucketRepo();
		bitbucketRepo.setProcessorId(PROCESSORID);
		bitbucketRepo.setProcessor(bitbucketProcessor);
		bitbucketRepos.add(bitbucketRepo);
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setPassword("020892BE903C15F566C09DAFEA800619");
		connectionDetail.setUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		connectionDetail.setApiEndPoint("/rest/api/1.0/");
		connectionDetail.setUsername("User");
		List<ProcessorToolConnection> connList = new ArrayList<>();
		connList.add(connectionDetail);
		List<CommitDetails> commitDetailList = new ArrayList<>();
		CommitDetails commitDetails = new CommitDetails();
		commitDetails.setBranch("master");
		commitDetails.setUrl("https://tools.publicis.sapient.com/scm/speed/speedy.git");
		commitDetailList.add(commitDetails);
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
		Mockito.when(bitBucketClient.fetchAllCommits(bitbucketRepo, true, connectionDetail))
				.thenReturn(commitDetailList);
		Mockito.when(bitBucketRepository.findByProcessorIdAndToolConfigId(any(), any())).thenReturn(bitbucketRepos);
		Mockito.when(bitBucketConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		Mockito.when(bitBucketClientFactory.getBitbucketClient(false)).thenReturn(bitBucketServerClient);
		bitBucketProcessorJobExecutor.execute(bitbucketProcessor);
	}

	@Test
	void testExecuteNewTool() throws FetchingCommitException {
		BitbucketProcessor bitbucketProcessor = BitbucketProcessor.prototype();
		bitbucketProcessor.setProcessorType(ProcessorType.SCM);
		bitbucketProcessor.setProcessorName("BitBucket");
		bitbucketProcessor.setId(PROCESSORID);
		List<BitbucketRepo> bitbucketRepos = new ArrayList<>();
		BitbucketRepo bitbucketRepo = new BitbucketRepo();
		bitbucketRepo.setProcessorId(PROCESSORID);
		bitbucketRepo.setProcessor(bitbucketProcessor);
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setPassword("020892BE903C15F566C09DAFEA800619");
		connectionDetail.setUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		connectionDetail.setApiEndPoint("/rest/api/1.0/");
		connectionDetail.setUsername("User");
		List<ProcessorToolConnection> connList = new ArrayList<>();
		connList.add(connectionDetail);
		List<CommitDetails> commitDetailList = new ArrayList<>();
		CommitDetails commitDetails = new CommitDetails();
		commitDetails.setBranch("Master");
		commitDetails.setUrl("https://tools.publicis.sapient.com/scm/speed/speedy.git");
		commitDetailList.add(commitDetails);

		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
		Mockito.when(bitBucketClient.fetchAllCommits(bitbucketRepo, true, connectionDetail))
				.thenReturn(commitDetailList);
		Mockito.when(bitBucketRepository.findByProcessorIdAndToolConfigId(any(), any())).thenReturn(bitbucketRepos);
		Mockito.when(bitBucketRepository.save(any(BitbucketRepo.class))).thenReturn(bitbucketRepo);
		Mockito.when(bitBucketConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		Mockito.when(bitBucketClientFactory.getBitbucketClient(false)).thenReturn(bitBucketServerClient);
		bitBucketProcessorJobExecutor.execute(bitbucketProcessor);
	}

	@Test
	void testGetResponse() throws Exception {
		String userName = "test";
		String password = "test";
		String url = "https://tools.publicis.sapient.com/scm.git";
		try {
			Whitebox.invokeMethod(basicBitBucketClient, "getResponse", userName, password, url);
		} catch(RestClientException e) {
			assertTrue(true);
		}
		
	}
}
