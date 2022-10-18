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
package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.data.KpiMasterDataFactory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.common.model.userboardconfig.Board;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpis;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;

/**
 * @author yasbano
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserBoardConfigServiceImplTest {

	@InjectMocks
	private UserBoardConfigServiceImpl userBoardConfigServiceImpl;

	@Mock
	private UserBoardConfigRepository userBoardConfigRepository;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private KpiMasterRepository kpiMasterRepository;

	@Mock
	private UserAuthorizedProjectsService userAuthorizedProjectsService;

	@Mock
	private KpiCategoryRepository kpiCategoryRepository;

	@Mock
	private KpiCategoryMappingRepository kpiCategoryMappingRepository;


	@Test
	public void testSaveUserBoardConfig() {
		String username = "user";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO( getData(username, true));
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		when(userBoardConfigRepository.findByUsername(username)).thenReturn(getData(username, true));
		when(userBoardConfigRepository.save(getData(username, true))).thenReturn(getData(username, true));
		UserBoardConfigDTO response = userBoardConfigServiceImpl.saveUserBoardConfig(userBoardConfigDTO);
		assertNotNull(response);
		assertEquals(response.getUsername(), username);
	}

	@Test
	public void testSaveUserBoardConfig_userNotLoggedIn() {
		String username = "user1";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username,true));
		when(authenticationService.getLoggedInUser()).thenReturn("invalid");
		assertNull(userBoardConfigServiceImpl.saveUserBoardConfig(userBoardConfigDTO));
	}

	@Test
	public void testSaveUserBoardConfig_DTOIsNull() {
		assertNull(userBoardConfigServiceImpl.saveUserBoardConfig(null));
	}

	@Test
	public void testSaveUserBoardConfig_userBoardConfigNull() {
		String username = "user";
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(getData(username, true));
		when(authenticationService.getLoggedInUser()).thenReturn(username);
		when(userBoardConfigRepository.findByUsername(username)).thenReturn(null);
		assertNull(userBoardConfigServiceImpl.saveUserBoardConfig(userBoardConfigDTO));
	}

	@Test
	public void testGetUserBoardConfig_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		doReturn(getData(username, true)).when(userBoardConfigRepository).findByUsername(ArgumentMatchers.anyString());
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getUserBoardConfig();
		assertNotNull(userBoardConfigDTO);
		assertEquals(userBoardConfigDTO.getUsername(), username);
	}

	@Test
	public void testGetUserBoardConfig_DefaultUserBoardConfig_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		doReturn(null).when(userBoardConfigRepository).findByUsername(ArgumentMatchers.anyString());
		KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
		Iterable<KpiMaster> kpiMasters = kpiMasterDataFactory.getKpiList();
		when(kpiMasterRepository.findAll()).thenReturn(kpiMasters);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getUserBoardConfig();
		assertNotNull(userBoardConfigDTO);
	}

	@Test
	public void testGetUserBoardConfig_NoUserBoardConfigFound_success() {
		String username = "testuser";
		doReturn(username).when(authenticationService).getLoggedInUser();
		doReturn(null).when(userBoardConfigRepository).findByUsername(ArgumentMatchers.anyString());
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.getUserBoardConfig();
		assertNotNull(userBoardConfigDTO);
	}

	@Test
	public void testSaveUserBoardConfig_whenUserInUserBoardConfigIsNotSuperAdminAndIsShownFlagIsFalse_thenReturnIsShownFlagIsFalse() {
		String username = "ADMIN";
		UserBoardConfig data = getData(username, true);
		doReturn(username).when(authenticationService).getLoggedInUser();
		doReturn(false).when(userAuthorizedProjectsService).ifSuperAdminUser();
		doReturn(data).when(userBoardConfigRepository).findByUsername(username);
		when(userBoardConfigRepository.save(data)).thenReturn(data);
		UserBoardConfigDTO userBoardConfigDTO1 = convertToUserBoardConfigDTO(data);

		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.saveUserBoardConfig(userBoardConfigDTO1);

		boolean shown = userBoardConfigDTO.getScrum().get(0).getKpis().get(0).isShown();
		assertFalse(shown);
	}

	@Test
	public void testSaveUserBoardConfig_whenUserInUserBoardConfigIsSuperAdminAndIsShownFlagIsFalse_thenReturnIsShownFlagIsFalse() {
		String username = "SUPERADMIN";
		UserBoardConfig data = getData(username, true);
		doReturn(username).when(authenticationService).getLoggedInUser();
		doReturn(true).when(userAuthorizedProjectsService).ifSuperAdminUser();
		doReturn(data).when(userBoardConfigRepository).findByUsername(username);
		when(userBoardConfigRepository.save(data)).thenReturn(data);
		UserBoardConfigDTO userBoardConfigDTO1 = convertToUserBoardConfigDTO(data);

		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigServiceImpl.saveUserBoardConfig(userBoardConfigDTO1);

		boolean shown = userBoardConfigDTO.getScrum().get(0).getKpis().get(0).isShown();
		assertFalse(shown);
	}

	UserBoardConfig getData(String username, boolean shown) {
		UserBoardConfig data = new UserBoardConfig();
		data.setUsername(username);
		BoardKpis kpi = new BoardKpis();
		BoardKpis kpi1 = new BoardKpis();
		createKpi(kpi, "kpi55", true, !shown, "speed", 1);
		createKpi(kpi1, "kpi56", true, shown, "speed", 2);
		List<BoardKpis> list = new ArrayList<>();
		list.add(kpi);
		list.add(kpi1);
		Board board = new Board();
		board.setBoardName("STATUS");
		board.setKpis(list);
		List<Board> boardList = new ArrayList<>();
		boardList.add(board);
		data.setScrum(boardList);
		data.setKanban(boardList);
		data.setOthers(boardList);
		return data;
	}

	private void createKpi(BoardKpis kpi, String id, boolean b, boolean shown, String name, int order) {
		kpi.setKpiId(id);
		kpi.setIsEnabled(b);
		kpi.setShown(shown);
		kpi.setKpiName(name);
		kpi.setOrder(order);
	}

	private UserBoardConfigDTO convertToUserBoardConfigDTO(UserBoardConfig userBoardConfig) {
		UserBoardConfigDTO userBoardConfigDTO = null;
		if (null != userBoardConfig) {
			ModelMapper mapper = new ModelMapper();
			userBoardConfigDTO = mapper.map(userBoardConfig, UserBoardConfigDTO.class);
		}
		return userBoardConfigDTO;
	}

}
