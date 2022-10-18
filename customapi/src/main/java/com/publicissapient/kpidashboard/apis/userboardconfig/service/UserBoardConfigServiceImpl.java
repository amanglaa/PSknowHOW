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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategoryMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.userboardconfig.Board;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpisDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class for UserBoardConfigService
 * 
 * @author narsingh9
 *
 */
@Service
@Slf4j
public class UserBoardConfigServiceImpl implements UserBoardConfigService {

	@Autowired
	private UserBoardConfigRepository userBoardConfigRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private KpiMasterRepository kpiMasterRepository;

	@Autowired
	private KpiCategoryRepository kpiCategoryRepository;

	@Autowired
	private KpiCategoryMappingRepository kpiCategoryMappingRepository;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	private static final String ITERATION = "Iteration";

	private static final String BACKLOG = "Backlog";

	private static final String KPI_MATURITY = "Kpi Maturity";

	private static final String DEFAULT_BOARD_NAME = "My KnowHow";

	/**
	 * This method return user board config if present in db else return a default
	 * configuration.
	 * 
	 * @return UserBoardConfigDTO
	 */
	public UserBoardConfigDTO getUserBoardConfig() {
		UserBoardConfig userBoardConfig = userBoardConfigRepository
				.findByUsername(authenticationService.getLoggedInUser());
		Iterable<KpiMaster> allKPIs = kpiMasterRepository.findAll();
		Map<String, KpiMaster> kpiMasterMap = StreamSupport.stream(allKPIs.spliterator(), false)
				.collect(Collectors.toMap(KpiMaster::getKpiId, Function.identity()));
		if (null == userBoardConfig) {
			UserBoardConfigDTO newUserBoardConfig = new UserBoardConfigDTO();
			List<KpiCategory> kpiCategoryList = kpiCategoryRepository.findAll();
			setUserBoardConfigBasedOnCategory(newUserBoardConfig, kpiCategoryList, kpiMasterMap);
			return newUserBoardConfig;
		}
		UserBoardConfigDTO userBoardConfigDTO = convertToUserBoardConfigDTO(userBoardConfig);
		filterKpis(userBoardConfigDTO, kpiMasterMap);
		checkKPIAddOrRemoveForExistingUser(userBoardConfigDTO, kpiMasterMap);
		return userBoardConfigDTO;

	}

	/**
	 * 
	 * @param userBoardConfigDTO
	 * @param kpiMasterMap
	 * @return
	 */
	private boolean checkKPIAddOrRemoveForExistingUser(UserBoardConfigDTO userBoardConfigDTO,
			Map<String, KpiMaster> kpiMasterMap) {
		Set<String> userKpiIdList = new HashSet<>();
		userBoardConfigDTO.getScrum().stream().forEach(
				kpiBoard -> kpiBoard.getKpis().stream().forEach(kpiList -> userKpiIdList.add(kpiList.getKpiId())));
		userBoardConfigDTO.getKanban().stream().forEach(
				kpiBoard -> kpiBoard.getKpis().stream().forEach(kpiList -> userKpiIdList.add(kpiList.getKpiId())));
		userBoardConfigDTO.getOthers().stream().forEach(
				kpiBoard -> kpiBoard.getKpis().stream().forEach(kpiList -> userKpiIdList.add(kpiList.getKpiId())));
		Set<String> kpiMasterKpiIdList = kpiMasterMap.keySet();
		return CollectionUtils.containsAny(kpiMasterKpiIdList, userKpiIdList);
	}

	/**
	 * 
	 * @param newUserBoardConfig
	 * @param kpiCategoryList
	 * @param kpiMasterMap
	 */
	private void setUserBoardConfigBasedOnCategory(UserBoardConfigDTO newUserBoardConfig,
			List<KpiCategory> kpiCategoryList, Map<String, KpiMaster> kpiMasterMap) {
		AtomicReference<Integer> kpiCategoryBoardId = new AtomicReference<>(1);
		newUserBoardConfig.setUsername(authenticationService.getLoggedInUser());
		List<BoardDTO> scrumBoards = new ArrayList<>();
		List<String> defaultKpiCategory = new ArrayList<>();
		defaultKpiCategory.add(ITERATION);
		defaultKpiCategory.add(BACKLOG);
		defaultKpiCategory.add(KPI_MATURITY);
		setDefaultBoardInfoFromKpiMaster(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), false,
				defaultKpiCategory, scrumBoards);
		if (CollectionUtils.isNotEmpty(kpiCategoryList)) {
			setAsPerCategoryMappingBoardInfo(kpiCategoryBoardId, kpiCategoryList, kpiMasterMap, scrumBoards, false);
		}
		setBoardInfoAsPerDefaultKpiCategory(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), ITERATION,
				scrumBoards , false);
		newUserBoardConfig.setScrum(scrumBoards);

		List<BoardDTO> kanbanBoards = new ArrayList<>();
		setDefaultBoardInfoFromKpiMaster(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), true,
				defaultKpiCategory, kanbanBoards);
		if (CollectionUtils.isNotEmpty(kpiCategoryList)) {
			setAsPerCategoryMappingBoardInfo(kpiCategoryBoardId, kpiCategoryList, kpiMasterMap, kanbanBoards, true);
		}
		setBoardInfoAsPerDefaultKpiCategory(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), ITERATION,
				kanbanBoards , true);
		newUserBoardConfig.setKanban(kanbanBoards);

		List<BoardDTO> otherBoards = new ArrayList<>();
		setBoardInfoAsPerDefaultKpiCategory(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), BACKLOG,
				otherBoards, false);
		setBoardInfoAsPerDefaultKpiCategory(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), KPI_MATURITY,
				otherBoards, false);
		newUserBoardConfig.setOthers(otherBoards);
	}

	/**
	 *
	 * @param kpiCategoryBoardId
	 * @param kpiCategoryList
	 * @param kpiMasterMap
	 * @param boardDTOList
	 * @param kanban
	 */
	private void setAsPerCategoryMappingBoardInfo(AtomicReference<Integer> kpiCategoryBoardId,
			List<KpiCategory> kpiCategoryList, Map<String, KpiMaster> kpiMasterMap, List<BoardDTO> boardDTOList,
			boolean kanban) {
		List<KpiCategoryMapping> kpiCategoryMappingList = kpiCategoryMappingRepository.findAll();
		if (CollectionUtils.isNotEmpty(kpiCategoryMappingList)) {
			Map<String, List<KpiCategoryMapping>> kpiIdWiseCategory = kpiCategoryMappingList.stream()
					.collect(Collectors.groupingBy(KpiCategoryMapping::getCategoryId, Collectors.toList()));
			kpiCategoryList.stream().forEach(kpiCategory ->
				setBoardInfoAsPerKpiCategory(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), kpiCategory,
						kpiIdWiseCategory.get(kpiCategory.getCategoryId()), kpiMasterMap, boardDTOList, kanban));
		}
	}

	/**
	 *
	 * @param kpiCategoryBoardId
	 * @param kpiCategory
	 * @param kpiCategoryMappingList
	 * @param kpiMasterMap
	 * @param asPerCategoryBoardList
	 * @param kanban
	 */
	private void setBoardInfoAsPerKpiCategory(Integer kpiCategoryBoardId, KpiCategory kpiCategory,
			List<KpiCategoryMapping> kpiCategoryMappingList, Map<String, KpiMaster> kpiMasterMap,
			List<BoardDTO> asPerCategoryBoardList, boolean kanban) {
		BoardDTO asPerCategoryBoard = new BoardDTO();
		asPerCategoryBoard.setBoardId(kpiCategoryBoardId);
		asPerCategoryBoard.setBoardName(kpiCategory.getCategoryName());
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiCategoryMappingList.stream().filter(kpiCategoryMapping -> kpiCategoryMapping.isKanban() == kanban)
				.sorted(Comparator.comparing(KpiCategoryMapping::getKpiOrder))
				.forEach(kpiCategoryMapping -> setKpiUserBoardCategoryWise(boardKpisList, kpiCategoryMapping,
						kpiMasterMap.get(kpiCategoryMapping.getKpiId())));
		asPerCategoryBoard.setKpis(boardKpisList);
		asPerCategoryBoardList.add(asPerCategoryBoard);
	}

	/**
	 *
	 * @param boardId
	 * @param boardName
	 * @param asPerCategoryBoardList
	 * @param kanban
	 */
	private void setBoardInfoAsPerDefaultKpiCategory(int boardId, String boardName,
			List<BoardDTO> asPerCategoryBoardList, boolean kanban) {
		BoardDTO asPerCategoryBoard = new BoardDTO();
		asPerCategoryBoard.setBoardId(boardId);
		asPerCategoryBoard.setBoardName(boardName);
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiMasterRepository.findByKpiCategoryAndKanban(boardName, kanban).stream()
				.sorted(Comparator.comparing(KpiMaster::getDefaultOrder)).forEach(kpiMaster ->
					setKpiUserBoardDefaultFromKpiMaster(boardKpisList, kpiMaster));
		asPerCategoryBoard.setKpis(boardKpisList);
		asPerCategoryBoardList.add(asPerCategoryBoard);
	}

	/**
	 *
	 * @param boardId
	 * @param kanban
	 * @param kpiCategory
	 * @param defaultBoardList
	 */
	private void setDefaultBoardInfoFromKpiMaster(int boardId, boolean kanban, List<String> kpiCategory,
			List<BoardDTO> defaultBoardList) {
		BoardDTO defaultBoard = new BoardDTO();
		defaultBoard.setBoardId(boardId);
		defaultBoard.setBoardName(DEFAULT_BOARD_NAME);
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(kanban, kpiCategory).stream()
				.sorted(Comparator.comparing(KpiMaster::getDefaultOrder))
				.forEach(kpiMaster -> setKpiUserBoardDefaultFromKpiMaster(boardKpisList, kpiMaster));
		defaultBoard.setKpis(boardKpisList);
		defaultBoardList.add(defaultBoard);
	}

	/**
	 *
	 * @param boardKpisList
	 * @param kpiMaster
	 */
	private void setKpiUserBoardDefaultFromKpiMaster(List<BoardKpisDTO> boardKpisList, KpiMaster kpiMaster) {
		BoardKpisDTO boardKpis = new BoardKpisDTO();
		boardKpis.setKpiId(kpiMaster.getKpiId());
		boardKpis.setKpiName(kpiMaster.getKpiName());
		boardKpis.setShown(true);
		boardKpis.setIsEnabled(true);
		boardKpis.setOrder(kpiMaster.getDefaultOrder());
		boardKpis.setKpiDetail(kpiMaster);
		boardKpisList.add(boardKpis);
	}

	/**
	 *
	 * @param boardKpisList
	 * @param kpiCategoryMapping
	 * @param kpiMaster
	 */
	private void setKpiUserBoardCategoryWise(List<BoardKpisDTO> boardKpisList, KpiCategoryMapping kpiCategoryMapping,
			KpiMaster kpiMaster) {
		if (Objects.nonNull(kpiMaster)) {
			BoardKpisDTO boardKpis = new BoardKpisDTO();
			boardKpis.setKpiId(kpiCategoryMapping.getKpiId());
			boardKpis.setKpiName(kpiMaster.getKpiName());
			boardKpis.setShown(true);
			boardKpis.setIsEnabled(true);
			boardKpis.setOrder(kpiCategoryMapping.getKpiOrder());
			boardKpis.setKpiDetail(kpiMaster);
			boardKpisList.add(boardKpis);
		} else {
			log.error("[UserBoardConfig]. No kpi Data found for {}" , kpiCategoryMapping.getKpiId());

		}
	}

	/**
	 * This method convert userboardconfig to its dto
	 * 
	 * @param userBoardConfig
	 *            userBoardConfig
	 * @return UserBoardConfigDTO
	 */
	private UserBoardConfigDTO convertToUserBoardConfigDTO(UserBoardConfig userBoardConfig) {
		UserBoardConfigDTO userBoardConfigDTO = null;
		if (null != userBoardConfig) {
			ModelMapper mapper = new ModelMapper();
			userBoardConfigDTO = mapper.map(userBoardConfig, UserBoardConfigDTO.class);
		}
		return userBoardConfigDTO;
	}

	private void filterKpis(UserBoardConfigDTO userBoardConfigDTO, Map<String, KpiMaster> kpiDetailMap) {
		if (userBoardConfigDTO != null) {
			addKpiDetails(userBoardConfigDTO.getScrum(), kpiDetailMap);
			addKpiDetails(userBoardConfigDTO.getKanban(), kpiDetailMap);
			addKpiDetails(userBoardConfigDTO.getOthers(), kpiDetailMap);
		}
	}

	private void addKpiDetails(List<BoardDTO> boardList, Map<String, KpiMaster> kpiDetailMap) {
		CollectionUtils.emptyIfNull(boardList).stream().forEach(board -> {
			List<BoardKpisDTO> boardKpiDtoList = new ArrayList<>();
			board.getKpis().stream().forEach(boardKpisDTO -> {
				KpiMaster kpiMaster = kpiDetailMap.get(boardKpisDTO.getKpiId());
				if (null != kpiMaster) {
					boardKpisDTO.setKpiName(kpiMaster.getKpiName());
					boardKpisDTO.setKpiDetail(kpiMaster);
					boardKpiDtoList.add(boardKpisDTO);
				}
			});
			board.setKpis(boardKpiDtoList);
		});
	}

	/**
	 * This method save user board config
	 * 
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return UserBoardConfigDTO
	 */
	@Override
	public UserBoardConfigDTO saveUserBoardConfig(UserBoardConfigDTO userBoardConfigDTO) {
		UserBoardConfig boardConfig = null;
		UserBoardConfig userBoardConfig = convertDTOToUserBoardConfig(userBoardConfigDTO);
		if (userBoardConfig != null && authenticationService.getLoggedInUser().equals(userBoardConfig.getUsername())) {
			boardConfig = userBoardConfigRepository.findByUsername(authenticationService.getLoggedInUser());
			if (null != boardConfig) {
				boardConfig.setScrum(userBoardConfig.getScrum());
				boardConfig.setKanban(userBoardConfig.getKanban());
				boardConfig.setOthers(userBoardConfig.getOthers());
			} else {
				boardConfig = userBoardConfig;
			}
			boardConfig = userBoardConfigRepository.save(boardConfig);

			if (authorizedProjectsService.ifSuperAdminUser()) {
				List<UserBoardConfig> userBoardConfigs = userBoardConfigRepository.findAll();
				updateKpiDetails(userBoardConfigs, boardConfig);
				userBoardConfigRepository.saveAll(userBoardConfigs);
			}
		}
		return convertToUserBoardConfigDTO(boardConfig);
	}

	/**
	 * delete user from user_board_config
	 *
	 * @param userName
	 */
	@Override
	public void deleteUser(String userName) {
		log.info("UserBoardConfigServiceImpl::deleteUser start");
		userBoardConfigRepository.deleteByUsername(userName);
		log.info(userName + " deleted Successfully from user_board_config");
	}

	private void updateKpiDetails(List<UserBoardConfig> userBoardConfigs, UserBoardConfig finalBoardConfig) {
		List<Board> scrum = finalBoardConfig.getScrum();
		List<Board> kanban = finalBoardConfig.getKanban();
		List<Board> others = finalBoardConfig.getOthers();
		Map<String, Boolean> kpiWiseIsShownFlag = new HashMap<>();
		CollectionUtils.emptyIfNull(scrum).stream().flatMap(boardDTO -> boardDTO.getKpis().stream())
				.forEach(boardKpis -> kpiWiseIsShownFlag.put(boardKpis.getKpiId(), boardKpis.isShown()));
		CollectionUtils.emptyIfNull(kanban).stream().flatMap(boardDTO -> boardDTO.getKpis().stream())
				.forEach(boardKpis -> kpiWiseIsShownFlag.put(boardKpis.getKpiId(), boardKpis.isShown()));
		CollectionUtils.emptyIfNull(others).stream().flatMap(boardDTO -> boardDTO.getKpis().stream())
				.forEach(boardKpis -> kpiWiseIsShownFlag.put(boardKpis.getKpiId(), boardKpis.isShown()));

		for (UserBoardConfig boardConfig : userBoardConfigs) {
			CollectionUtils.emptyIfNull(boardConfig.getScrum()).stream()
					.flatMap(boardDTO -> boardDTO.getKpis().stream()).forEach(boardKpisDTO -> boardKpisDTO
							.setShown(kpiWiseIsShownFlag.getOrDefault(boardKpisDTO.getKpiId(), true)));
			CollectionUtils.emptyIfNull(boardConfig.getKanban()).stream()
					.flatMap(boardDTO -> boardDTO.getKpis().stream()).forEach(boardKpisDTO -> boardKpisDTO
							.setShown(kpiWiseIsShownFlag.getOrDefault(boardKpisDTO.getKpiId(), true)));
			CollectionUtils.emptyIfNull(boardConfig.getOthers()).stream()
					.flatMap(boardDTO -> boardDTO.getKpis().stream()).forEach(boardKpisDTO -> boardKpisDTO
							.setShown(kpiWiseIsShownFlag.getOrDefault(boardKpisDTO.getKpiId(), true)));
		}
	}

	/**
	 * This method convert userBoardConfigDTO to its userBoardConfig K
	 * 
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return UserBoardConfig
	 */
	private UserBoardConfig convertDTOToUserBoardConfig(UserBoardConfigDTO userBoardConfigDTO) {
		UserBoardConfig userBoardConfig = null;
		if (null != userBoardConfigDTO) {
			ModelMapper mapper = new ModelMapper();
			userBoardConfig = mapper.map(userBoardConfigDTO, UserBoardConfig.class);
		}
		return userBoardConfig;
	}

}
