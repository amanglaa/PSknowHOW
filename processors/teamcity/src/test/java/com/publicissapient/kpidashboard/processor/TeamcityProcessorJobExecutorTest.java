package com.publicissapient.kpidashboard.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.factory.TeamcityClientFactory;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityJob;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityProcessor;
import com.publicissapient.kpidashboard.teamcity.processor.TeamcityProcessorJobExecutor;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.repository.TeamcityJobRepository;

@SuppressWarnings("javadoc")
@ExtendWith(SpringExtension.class)
public class TeamcityProcessorJobExecutorTest {
	private static final String CUSTOM_API_BASE_URL = "http://localhost:9090/";
	private static final String METRICS1 = "nloc";
	private static final String EXCEPTION = "rest client exception";
	private static final String PLAIN_TEXT_PASSWORD = "Test@123";
	@Mock
	private TeamcityConfig teamcityConfig;
	@Mock
	private TeamcityClientFactory teamcityClientFactory;
	@Mock
	private TeamcityClient teamcityClient;
	@InjectMocks
	TeamcityProcessorJobExecutor jobExecutor;
	@Mock
	private TeamcityJobRepository teamcityJobRepository;
	@Mock
	TeamcityJob teamcityJob ;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	AesEncryptionService aesEncryptionService;

	
	private List<TeamcityJob> listTeamcityJob = new ArrayList<>();
	private List<ProcessorToolConnection> connList = new ArrayList<>();
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		ProcessorToolConnection processorToolConnection = new ProcessorToolConnection();
		processorToolConnection.setUrl("http://does:matter@jenkins.com");
		processorToolConnection.setUsername("does");
		processorToolConnection.setPassword("matter");
		processorToolConnection.setJobName("jobName");

		connList.add(processorToolConnection);
		when(teamcityJobRepository.findByProcessorIdIn(any())).thenReturn(listTeamcityJob);
		when(processorToolConnectionService.findByTool(anyString())).thenReturn(connList);
		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);

	}
	@Test
	public void processOneServer43() throws Exception {
		when(teamcityConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			when(teamcityClientFactory.getTeamcityClient(anyString())).thenReturn(teamcityClient);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}
	private TeamcityProcessor processorWithOneServer() {
		return TeamcityProcessor.buildProcessor();
	}
}
