package com.publicissapient.kpidashboard.apis.capacity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.*;

import com.publicissapient.kpidashboard.apis.data.CapacityKpiDataDataFactory;
import com.publicissapient.kpidashboard.apis.data.KanbanCapacityDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.collections.Lists;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CapacityMasterServiceImplTest {

	private MockMvc mockMvc;

	@InjectMocks
	private CapacityMasterServiceImpl capacityMasterServiceImpl;

	@Mock
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Mock
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private ProjectBasicConfigService projectBasicConfigService;

	@Mock
	private SprintDetailsService sprintDetailsService;

	CapacityMaster scrumCapacityMaster;

	CapacityMaster kanbanCapacity;

	KanbanCapacity kanbanDbData;
	List<KanbanCapacity> kanbanCapacityList;
	List<CapacityKpiData> capacityKpiDataList;
	private List<SprintDetails> sprintDetailsList;

	/**
	 * initialize values to be used in testing
	 */
	@Before
	public void setUp() {
		kanbanCapacityList = KanbanCapacityDataFactory.newInstance().getKanbanCapacityDataList();
		capacityKpiDataList = CapacityKpiDataDataFactory.newInstance().getCapacityKpiDataList();
		sprintDetailsList = SprintDetailsDataFactory.newInstance().getSprintDetails();
		mockMvc = MockMvcBuilders.standaloneSetup(capacityMasterServiceImpl).build();
		scrumCapacityMaster = new CapacityMaster();
		scrumCapacityMaster.setProjectName("Scrum Project");
		scrumCapacityMaster.setProjectNodeId("Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityMaster.setSprintNodeId("38296_Scrum Project_6335363749794a18e8a4479b");
		scrumCapacityMaster.setKanban(false);
		scrumCapacityMaster.setCapacity(500.0);

		kanbanCapacity = new CapacityMaster();
		kanbanCapacity.setProjectName("Kanban Project");
		kanbanCapacity.setProjectNodeId("Kanban Project_6335368249794a18e8a4479f");
		kanbanCapacity.setStartDate("2021-01-31");
		kanbanCapacity.setEndDate("2020-02-02");
		kanbanCapacity.setKanban(true);
		kanbanCapacity.setCapacity(500.0);

		kanbanDbData = new KanbanCapacity();
		kanbanDbData.setProjectName("health project");
		kanbanDbData.setProjectId("Kanban Project_6335368249794a18e8a4479f");
		kanbanDbData.setCapacity(200.0);
	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_scrum_success() {
		Map<String, String> map = new HashMap<>();
		map.put("projectId", scrumCapacityMaster.getProjectNodeId());
		assertNotNull(capacityMasterServiceImpl.processCapacityData(scrumCapacityMaster));
	}

	/**
	 * scrum capacity data not saving due to missing fields
	 */
	@Test
	public void testProcessCapacityData_scrum_failure() {
		Map<String, String> map = new HashMap<>();
		map.put("projectId", scrumCapacityMaster.getProjectNodeId());
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_kanban_success() {
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data saving
	 */
	@Test
	public void testProcessCapacityData_kanban_alreadyExist_success() {
		when(kanbanCapacityRepository.findByFilterMapAndDate(Mockito.any(), Mockito.any()))
				.thenReturn(Lists.newArrayList(kanbanDbData));
		assertNotNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	/**
	 * scrum capacity data not saving due to missing fields
	 */
	@Test
	public void testProcessCapacityData_kanban_failure() {
		kanbanCapacity = new CapacityMaster();
		assertNull(capacityMasterServiceImpl.processCapacityData(kanbanCapacity));
	}

	@Test
	public void getCapacities_ScrumSuccess() {
		ProjectBasicConfig project = createScrumProject();
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335363749794a18e8a4479b");
		assertEquals(0, capacities.size());
	}

	@Test
	public void getCapacities_ScrumWithNoSavedData() {
		ProjectBasicConfig project = createScrumProject();
		when(customApiConfig.getSprintCountForFilters()).thenReturn(5);
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);

		when(sprintDetailsService.getSprintDetails(anyString())).thenReturn(sprintDetailsList);

		when(capacityKpiDataRepository.findBySprintIDIn(anyList()))
				.thenReturn(new ArrayList<>());

		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335363749794a18e8a4479b");

		assertEquals(5, capacities.size());
	}


	@Test
	public void getCapacities_KanbanSuccess() {
		ProjectBasicConfig project = createKanbanProject();
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanCapacityRepository.findByBasicProjectConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(kanbanCapacityList);
		when(customApiConfig.getNumberOfPastWeeksForKanbanCapacity()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureWeeksForKanbanCapacity()).thenReturn(2);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335368249794a18e8a4479f");

		assertEquals(5, capacities.size());
	}

	@Test
	public void getCapacities_KanbanWithNoDataSaved() {
		ProjectBasicConfig project = createKanbanProject();
		when(projectBasicConfigService.getProjectBasicConfigs(anyString())).thenReturn(project);
		when(kanbanCapacityRepository.findByBasicProjectConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(new ArrayList<>());
		when(customApiConfig.getNumberOfPastWeeksForKanbanCapacity()).thenReturn(2);
		when(customApiConfig.getNumberOfFutureWeeksForKanbanCapacity()).thenReturn(2);
		List<CapacityMaster> capacities = capacityMasterServiceImpl.getCapacities("6335368249794a18e8a4479f");

		assertEquals(5, capacities.size());
	}

	private ProjectBasicConfig createScrumProject() {

		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("6335363749794a18e8a4479b"));

		project.setIsKanban(false);
		return project;
	}

	private ProjectBasicConfig createKanbanProject() {
		ProjectBasicConfig project = new ProjectBasicConfig();
		project.setId(new ObjectId("6335368249794a18e8a4479f"));

		project.setIsKanban(true);
		return project;
	}
}
