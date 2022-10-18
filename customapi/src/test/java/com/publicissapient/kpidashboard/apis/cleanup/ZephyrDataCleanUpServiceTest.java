package com.publicissapient.kpidashboard.apis.cleanup;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ZephyrDataCleanUpServiceTest {

    @InjectMocks
    private ZephyrDataCleanUpService zephyrDataCleanUpService;

    @Mock
    private ProjectToolConfigRepository projectToolConfigRepository;

    @Mock
    private TestCaseDetailsRepository testCaseDetailsRepository;

    @Mock
    private CacheService cacheService;

    @Test
    public void getToolCategory() {
        String actualResult = zephyrDataCleanUpService.getToolCategory();
        assertEquals(ProcessorType.ZEPHYR.toString(), actualResult);
    }

    @Test
    public void cleanZephyrData() {
        ProjectToolConfig projectToolConfig = new ProjectToolConfig();
        projectToolConfig.setId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
        projectToolConfig.setBasicProjectConfigId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));
        when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
        doNothing().when(testCaseDetailsRepository).deleteByBasicProjectConfigId(anyString());
        zephyrDataCleanUpService.clean("5e9db8f1e4b0caefbfa8e0c7");
        verify(testCaseDetailsRepository, times(1)).deleteByBasicProjectConfigId("5e9db8f1e4b0caefbfa8e0c7");
    }
}
