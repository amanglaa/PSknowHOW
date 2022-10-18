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

package com.publicissapient.kpidashboard.azurerepo.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoProcessor;
import com.publicissapient.kpidashboard.azurerepo.processor.service.AzureRepoClient;
import com.publicissapient.kpidashboard.azurerepo.processor.service.impl.common.BasicAzureRepoClient;
import com.publicissapient.kpidashboard.azurerepo.repository.AzureRepoProcessorRepository;
import com.publicissapient.kpidashboard.azurerepo.repository.AzureRepoRepository;
import com.publicissapient.kpidashboard.azurerepo.util.AzureRepoRestOperations;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
public class AzureRepoProcessorJobExecutorTest {

	/** The processorid. */
	
	private final ObjectId processorId = new ObjectId("5eeb595c0cccfd093afd2b43");

	@Mock
	private AzureRepoRestOperations azurerepoRestOperations;

	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private AzureRepoConfig azureRepoConfig;

	@Mock
	private ProcessorItemRepository<ProcessorItem> processorItemRepository;

	@Mock
	private ProjectToolConfigRepository toolConfigRepository;

	@Mock
	private AzureRepoProcessorRepository azureRepoProcessorRepo;

	@Mock
	private AzureRepoRepository azureRepoRepository;

	@Mock
	private AzureRepoClient azureRepoClient;

	@Mock
	private CommitRepository commitsRepo;

	@Mock
	private RestOperations restOperations;

	@Mock
	private BasicAzureRepoClient basicAzureRepoClient;

	@InjectMocks
	private AzureRepoProcessorJobExecutor azureRepoProcessorJobExecutor;

	@Mock
	private AesEncryptionService aesEncryptionService;
	
	@Mock
	private AzureRepoProcessor azureRepoProcessor;
	
	@Mock
	private ConnectionRepository connectionsRepository;
	
	@Mock
	private  ProcessorToolConnectionService processorToolConnectionService;
	
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	
	@Mock 
	private MergeRequestRepository mergReqRepo;
	
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@BeforeEach
	public void setUp() throws Exception {
		azureRepoProcessorJobExecutor = new AzureRepoProcessorJobExecutor(taskScheduler, azureRepoProcessorRepo,
				azureRepoConfig, toolConfigRepository, azureRepoRepository, azureRepoClient, processorItemRepository,
				commitsRepo, connectionsRepository, processorToolConnectionService, projectConfigRepository,
				mergReqRepo, processorExecutionTraceLogService);
		Mockito.when(azurerepoRestOperations.getTypeInstance()).thenReturn(new RestTemplate());
		basicAzureRepoClient = new BasicAzureRepoClient(azureRepoConfig, azurerepoRestOperations, aesEncryptionService);

		AzureRepoProcessor azureRepoProcessor = new AzureRepoProcessor();
	}
	
	@Test
	public void testGetCron() {
		Mockito.when(azureRepoConfig.getCron()).thenReturn("0 0 0/12 * * *");
		
		String actual = azureRepoProcessorJobExecutor.getCron();
		Assert.assertEquals("0 0 0/12 * * *", actual);
	}

	@Test
	public void testExecute()
			throws FetchingCommitException, JsonParseException, JsonMappingException, IOException, Exception {
		AzureRepoProcessor azurerepoProcessor = AzureRepoProcessor.prototype();
		azurerepoProcessor.setProcessorType(ProcessorType.SCM);
		azurerepoProcessor.setProcessorName("AzureRepository");
		
		azurerepoProcessor.setId(processorId);

		
		String filePath = "src/test/resources/com/processoritem.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<AzureRepoModel> azurerepoRepos = Arrays.asList(objectMapper.readValue(file, AzureRepoModel[].class));

		
		String filePath2 = "src/test/resources/com/commitdetails.json";
		File file2 = new File(filePath2);
		ObjectMapper objectMapper2 = new ObjectMapper();
		objectMapper2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<CommitDetails> commitDetailList = Arrays.asList(objectMapper2.readValue(file2, CommitDetails[].class));
		ProcessorToolConnection azureRepoProcessorInfo = new ProcessorToolConnection();
		azureRepoProcessorInfo.setApiVersion("5.6");
		azureRepoProcessorInfo.setBranch("master");
		azureRepoProcessorInfo.setUrl("https://dev.azure.com/ankbhard/KnowHOW");
		azureRepoProcessorInfo.setPat("9cZJr0+Z5bKUKUDRd0llzQrif1teMof18n93nRX6OsERvNiAOOawZ");
		azureRepoProcessorInfo.setRepoSlug("knowHow");
		
		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig basicConfig = new ProjectBasicConfig();
		basicConfig.setId(new ObjectId("60b7dbb489c5974a407e923b"));
		projectConfigList.add(basicConfig);
		
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		
		Mockito.when(azureRepoRepository.findActiveRepos(processorId)).thenReturn(azurerepoRepos);
		
		boolean actualexecutionstatus = azureRepoProcessorJobExecutor.execute(azurerepoProcessor);
		
		boolean expectedexecutionstatus = true;
		Assert.assertEquals("Expected result is ", expectedexecutionstatus, actualexecutionstatus);
	}

	@Test
	public void testAddProcessorItems() throws Exception {// NOSONAR

		List<ProcessorItem> processorItems = new ArrayList<>();

		Processor processor = new Processor();
		processor.setProcessorName("AzureRepository");
		
		processor.setId(processorId);

		List<ObjectId> processorIds = new ArrayList<>(0);
		processorIds.add(processor.getId());

		ProcessorItem processorItem = new ProcessorItem();
		
		processorItem.setProcessorId(processorId);
		processorItems.add(processorItem);
		

		String filePath3 = "src/test/resources/com/toolconfigs.json";
		File file3 = new File(filePath3);
		ObjectMapper objectMapper3 = new ObjectMapper();
		objectMapper3.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<ProjectToolConfig> toolConfigs = Arrays.asList(objectMapper3.readValue(file3, ProjectToolConfig[].class));
		Connection connection = new Connection();
		connection.setBaseUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		connection.setAccessToken(
				"8PTAhVLdwUoQk7gUnPfiPXWmOiUojMSJVyl/vIKBJ01X80SocKq1rzKsK9u1QQisyuYXOmeRkZhNTHq648pscw==");
		Mockito.when(processorItemRepository.findByProcessorIdIn(processorIds)).thenReturn(processorItems);
		Mockito.when(connectionsRepository.findById(toolConfigs.get(0).getConnectionId())).thenReturn(Optional.of(connection));
		Whitebox.invokeMethod(azureRepoProcessorJobExecutor, "addProcessorItems", processor, toolConfigs);
	}


	

	@Test
   	public void testGetProcessor() {
		azureRepoProcessor = azureRepoProcessorJobExecutor.getProcessor();
		assertEquals("Processor name : ", "AzureRepository", azureRepoProcessor.getProcessorName());
	}

	@Test
	public void getProcessorRepositoryTest() {
		assertEquals(azureRepoProcessorRepo, azureRepoProcessorJobExecutor.getProcessorRepository());
	}
}