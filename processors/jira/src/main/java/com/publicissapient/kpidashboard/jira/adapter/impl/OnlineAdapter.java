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

package com.publicissapient.kpidashboard.jira.adapter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * Default JIRA client which interacts with Java JIRA API to extract data for
 * projects based on the configurations provided
 */
@Slf4j
@Service
public class OnlineAdapter implements JiraAdapter {

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
	private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
	private static final String NO_RESULT_QUERY = "No result available for query: {}";
	private static final String EXCEPTION = "Exception";
	private static final String CONTENTS="contents";
	private static final String COMPLETED_ISSUES="completedIssues";
	private static final String PUNTED_ISSUES="puntedIssues";
	private static final String COMPLETED_ISSUES_ANOTHER_SPRINT="issuesCompletedInAnotherSprint";
	private static final String ADDED_ISSUES="issueKeysAddedDuringSprint";
	private static final String NOT_COMPLETED_ISSUES="issuesNotCompletedInCurrentSprint";
	private static final String KEY="key";
	

	private JiraProcessorConfig jiraProcessorConfig;

	private AesEncryptionService aesEncryptionService;

	private ProcessorJiraRestClient client;

	public OnlineAdapter() {
	}

	/**
	 * @param jiraProcessorConfig
	 *            jira processor configuration
	 * @param client
	 *            ProcessorJiraRestClient instance
	 * @param aesEncryptionService
	 *            aesEncryptionService
	 */
	public OnlineAdapter(JiraProcessorConfig jiraProcessorConfig, ProcessorJiraRestClient client,
			AesEncryptionService aesEncryptionService) {
		this.jiraProcessorConfig = jiraProcessorConfig;
		this.client = client;
		this.aesEncryptionService = aesEncryptionService;

	}

	/**
	 * Gets all issues from JIRA
	 *
	 * @param startDateTimeByIssueType
	 *            map of start dataTime of issue types
	 * @param userTimeZone
	 *            user timezone
	 * @param pageStart
	 *            page start
	 * @param dataExist
	 *            dataExist on db or not
	 *
	 * @return list of issues
	 */
	@Override
	public SearchResult getIssues(ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart,
			boolean dataExist) {
		SearchResult searchResult = null;

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			String query = StringUtils.EMPTY;
			try {
				Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

				startDateTimeByIssueType.forEach((type, localDateTime) -> {
					ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
					String dateTimeStr = zonedDateTime.format(formatter);
					startDateTimeStrByIssueType.put(type, dateTimeStr);

				});

				query = (projectConfig.getJira().isQueryEnabled()
						? JiraProcessorUtil.processJql(projectConfig.getJira().getBoardQuery(),
								startDateTimeStrByIssueType, dataExist)
						: JiraProcessorUtil.createJql(projectConfig.getJira().getProjectKey(),
								startDateTimeStrByIssueType));
				log.info("jql= " + query);
				Instant start = Instant.now();

				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query,
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				Instant finish = Instant.now();
				long timeElapsed = Duration.between(start, finish).toMillis();
				log.info("Time taken to fetch the data is {} milliseconds", timeElapsed);
				if (searchResult != null) {
					log.info("Processing issues {} - {} out of {}", pageStart,
							Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal());
				}
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.info(NO_RESULT_QUERY, query);
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
				}
			}

		}

		return searchResult;
	}

	/**
	 * Returns all versions for a project, which are visible for the currently
	 * logged in user
	 *
	 * @param projectKey
	 *            the project key
	 * @return List of version
	 */
	@Override
	public List<Version> getVersions(String projectKey) {
		List<Version> rt = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Project> promisedRs = client.getProjectClient().getProject(projectKey);

				Project jiraProject = promisedRs.claim();
				Iterable<Version> version = jiraProject.getVersions();
				if (version != null) {
					rt = Lists.newArrayList(version.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
			}
		}

		return rt;
	}

	private void exceptionBlockProcess(RestClientException e) {
		if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
			log.error(ERROR_MSG_401);
		} else {
			log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
		}
		log.debug(EXCEPTION, e);
	}

	@Override
	public List<Field> getField() {
		List<Field> fieldList = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Iterable<Field>> promisedRs = client.getMetadataClient().getFields();

				Iterable<Field> fieldIt = promisedRs.claim();
				if (fieldIt != null) {
					fieldList = Lists.newArrayList(fieldIt.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
			}
		}

		return fieldList;
	}

	@Override
	public List<IssueType> getIssueType() {
		List<IssueType> issueTypeList = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Iterable<IssueType>> promisedRs = client.getMetadataClient().getIssueTypes();

				Iterable<IssueType> fieldIt = promisedRs.claim();
				if (fieldIt != null) {
					issueTypeList = Lists.newArrayList(fieldIt.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
			}
		}

		return issueTypeList;
	}

	@Override
	public List<Status> getStatus() {
		List<Status> statusList = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Iterable<Status>> promisedRs = client.getMetadataClient().getStatuses();

				Iterable<Status> fieldIt = promisedRs.claim();
				if (fieldIt != null) {
					statusList = Lists.newArrayList(fieldIt.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
			}
		}

		return statusList;
	}

	@Override
	public List<IssuelinksType> getIssueLinkTypes() {
		List<IssuelinksType> statusList = new ArrayList<>();

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			try {
				Promise<Iterable<IssuelinksType>> promisedRs = client.getMetadataClient().getIssueLinkTypes();

				Iterable<IssuelinksType> fieldIt = promisedRs.claim();
				if (fieldIt != null) {
					statusList = Lists.newArrayList(fieldIt.iterator());
				}
			} catch (RestClientException e) {
				exceptionBlockProcess(e);
			}
		}

		return statusList;
	}

	/**
	 * Gets page size from feature settings
	 *
	 * @return pageSize
	 */
	@Override
	public int getPageSize() {
		return jiraProcessorConfig.getPageSize();
	}

	/**
	 * Gets the timeZone of user who is logged in jira
	 *
	 * @param projectConfig
	 *            user provided project configuration
	 * @return String of UserTimeZone
	 */
	@Override
	public String getUserTimeZone(ProjectConfFieldMapping projectConfig) {
		String userTimeZone = StringUtils.EMPTY;
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();

			if (null != jiraToolConfig) {
				Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
				String username = connectionOptional.map(Connection::getUsername).orElse(null);
				URL url = getUrl(projectConfig, username);
				URLConnection connection;

				connection = url.openConnection();
				userTimeZone = getUserTimeZone(getDataFromServer(projectConfig, (HttpURLConnection) connection));
			}
		} catch (RestClientException rce) {
			log.error("Client exception when loading statuses", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading statuses", mfe);
		} catch (IOException ioe) {
			log.error("IOException", ioe);
		}

		return userTimeZone;
	}

	@SuppressWarnings("unchecked")
	private String getUserTimeZone(String timezoneObj) {
		String userTimeZone = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(timezoneObj)) {
			try {
				Object obj = new JSONParser().parse(timezoneObj);
				JSONArray userInfoList = new JSONArray();
				userInfoList.add(obj);
				for (Object userInfo : userInfoList) {
					JSONArray jsonUserInfo = (JSONArray) userInfo;
					for (Object timeZone : jsonUserInfo) {
						JSONObject timeZoneObj = (JSONObject) timeZone;
						userTimeZone = (String) timeZoneObj.get("timeZone");
					}
				}

			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return userTimeZone;
	}

	private String getDataFromServer(ProjectConfFieldMapping projectConfig, HttpURLConnection connection)
			throws IOException {
		HttpURLConnection request = connection;
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();

		String username = connectionOptional.map(Connection::getUsername).orElse(null);
		String password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
		request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
		request.connect();
		StringBuilder sb = new StringBuilder();
		try (InputStream in = (InputStream) request.getContent();
				BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));) {
			int cp;
			while ((cp = inReader.read()) != -1) {
				sb.append((char) cp);
			}
		} catch (IOException ie) {
			log.error("Read exception when connecting to server {}", ie);
		}
		return sb.toString();
	}

	/**
	 * Checks if Jira credentails are empty
	 *
	 * @param projectConfigList
	 *            user provided project configuration
	 * @return True if credentials are not configured else False
	 */
	@SuppressWarnings("unused")
	private boolean isJiraCredentialsNotEmpty(ProjectConfFieldMapping projectConfigList) {
		Optional<Connection> connectionOptional = projectConfigList.getJira().getConnection();
		String username = connectionOptional.map(Connection::getUsername).orElse(null);
		String password = connectionOptional.map(Connection::getPassword).orElse(null);
		return StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password);
	}

	/**
	 * Gets Url constructed using user provided details
	 *
	 * @param projectConfig
	 *            user provided project configuration
	 * @param jiraUserName
	 *            jira credentials username
	 * @return URL
	 * @throws MalformedURLException
	 *             when URL not constructed properly
	 */
	private URL getUrl(ProjectConfFieldMapping projectConfig, String jiraUserName) throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		String serverURL = jiraProcessorConfig.getJiraServerGetUserApi();
		if (isCloudEnv) {
			serverURL = jiraProcessorConfig.getJiraCloudGetUserApi();
		}

		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		String apiEndPoint = connectionOptional.map(Connection::getApiEndPoint).orElse("");

		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + apiEndPoint
				+ (apiEndPoint.endsWith("/") ? "" : "/") + serverURL + jiraUserName);

	}

	private String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}

	private String encodeCredentialsToBase64(String username, String password) {
		String cred = username + ":" + password;
		return Base64.getEncoder().encodeToString(cred.getBytes());
	}

	/**
	 * Gets the sprint report based on sprint id and board id
	 *
	 * @param projectConfig
	 * @param sprintId
	 * @param boardId
	 * @param sprintDetails
	 */
	@Override
	public void getSprintReport(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
			SprintDetails sprintDetails) {
		log.info("Start Calling sprint report api. Sprint Id : {} , Board Id : {}",sprintId,boardId);
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();

			if (null != jiraToolConfig) {
				URL url = getSprintReportUrl(projectConfig, sprintId,boardId);
				URLConnection connection;

				connection = url.openConnection();
				getSprintReport(getDataFromServer(projectConfig, (HttpURLConnection) connection),sprintDetails);	
			}
		log.info("End sprint report api. Sprint Id : {} , Board Id : {}",sprintId,boardId);
		} catch (RestClientException rce) {
			log.error("Client exception when loading sprint report", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading sprint report", mfe);
		} catch (IOException ioe) {
			log.error("IOException", ioe);
		}
	}

	private URL getSprintReportUrl(ProjectConfFieldMapping projectConfig, String sprintId, String boardId)
			throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		String serverURL = jiraProcessorConfig.getJiraServerSprintReportApi();
		if (isCloudEnv) {
			serverURL = jiraProcessorConfig.getJiraCloudSprintReportApi();
		}
		serverURL = serverURL.replace("{rapidViewId}",boardId).replace("{sprintId}",sprintId);
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/")  + serverURL);

	}
	
	private void getSprintReport(String sprintReportObj,SprintDetails sprintDetails) {
		if (StringUtils.isNotBlank(sprintReportObj)) {
			JSONArray completedIssuesJson = new JSONArray();
			JSONArray notCompletedIssuesJson = new JSONArray();
			JSONArray puntedIssuesJson = new JSONArray();
			JSONArray completedIssuesAnotherSprintJson = new JSONArray();
			JSONObject addedIssuesJson = new JSONObject();
	
			List<String> completedIssues = new ArrayList<>();
			List<String> notCompletedIssues = new ArrayList<>();
			List<String> puntedIssues = new ArrayList<>();
			List<String> completedIssuesAnotherSprint = new ArrayList<>();
			List<String> addedIssues = new ArrayList<>();
			List<String> totalIssues =new ArrayList<>();
			
			try {
				JSONObject obj = (JSONObject)new JSONParser().parse(sprintReportObj);
				if(null!=obj) {
					JSONObject contentObj=(JSONObject)obj.get(CONTENTS);
					completedIssuesJson=(JSONArray)contentObj.get(COMPLETED_ISSUES);
					notCompletedIssuesJson=(JSONArray)contentObj.get(NOT_COMPLETED_ISSUES);
					puntedIssuesJson=(JSONArray)contentObj.get(PUNTED_ISSUES);
					completedIssuesAnotherSprintJson=(JSONArray)contentObj.get(COMPLETED_ISSUES_ANOTHER_SPRINT);
					addedIssuesJson=(JSONObject)contentObj.get(ADDED_ISSUES);
				}
				setIssues(completedIssuesJson, completedIssues, totalIssues);
				
				setIssues(notCompletedIssuesJson, notCompletedIssues, totalIssues);
				
				setPuntedCompletedAnotherSprint(puntedIssuesJson, puntedIssues);
				
				setPuntedCompletedAnotherSprint(completedIssuesAnotherSprintJson, completedIssuesAnotherSprint);
				
				addedIssues = setAddedIssues(addedIssuesJson, addedIssues);
				
				sprintDetails.setCompletedIssues(completedIssues);
				sprintDetails.setNotCompletedIssues(notCompletedIssues);
				sprintDetails.setCompletedIssuesAnotherSprint(completedIssuesAnotherSprint);
				sprintDetails.setPuntedIssues(puntedIssues);
				sprintDetails.setAddedIssues(addedIssues);
				sprintDetails.setTotalIssues(totalIssues);

			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
	}

	/**
	 * @param addedIssuesJson
	 * @param addedIssues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> setAddedIssues(JSONObject addedIssuesJson, List<String> addedIssues) {
		Set<String> keys = addedIssuesJson.keySet();
		if(CollectionUtils.isNotEmpty(keys)) {
			addedIssues=keys.stream().collect(Collectors.toList());
		}
		return addedIssues;
	}

	/**
	 * @param puntedIssuesJson
	 * @param puntedIssues
	 */
	@SuppressWarnings("unchecked")
	private void setPuntedCompletedAnotherSprint(JSONArray puntedIssuesJson, List<String> puntedIssues) {
		puntedIssuesJson.forEach(puntedObj->{
			JSONObject punObj = (JSONObject) puntedObj;
			if(null!=punObj) {
				puntedIssues.add((String)punObj.get(KEY));
			}
		});
	}

	/**
	 * @param completedIssuesJson
	 * @param completedIssues
	 * @param totalIssues
	 */
	@SuppressWarnings("unchecked")
	private void setIssues(JSONArray issuesJson, List<String> issues,
			List<String> totalIssues) {
		issuesJson.forEach(jsonObj->{
			JSONObject obj = (JSONObject) jsonObj;
			if(null!=obj) {
				issues.add((String)obj.get(KEY));
				totalIssues.add((String)obj.get(KEY));
			}
		});
	}

}
