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

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraprojectmetadata.JiraIssueMetadata;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private static final String CONTENTS = "contents";
    private static final String COMPLETED_ISSUES = "completedIssues";
    private static final String PUNTED_ISSUES = "puntedIssues";
    private static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
    private static final String ADDED_ISSUES = "issueKeysAddedDuringSprint";
    private static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";
    private static final String KEY = "key";
    private static final String ENTITY_DATA = "entityData";
    private static final String PRIORITYID = "priorityId";
    private static final String STATUSID = "statusId";

    private static final String TYPEID = "typeId";

    private JiraProcessorConfig jiraProcessorConfig;
    private AesEncryptionService aesEncryptionService;
    private ProcessorJiraRestClient client;

    public OnlineAdapter() {
    }

    /**
     * @param jiraProcessorConfig  jira processor configuration
     * @param client               ProcessorJiraRestClient instance
     * @param aesEncryptionService aesEncryptionService
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
     * @param startDateTimeByIssueType map of start dataTime of issue types
     * @param userTimeZone             user timezone
     * @param pageStart                page start
     * @param dataExist                dataExist on db or not
     * @return list of issues
     */
    @Override
    public SearchResult getIssues(BoardDetails boardDetails, ProjectConfFieldMapping projectConfig,
                                  String startDateTimeByIssueType, String userTimeZone, int pageStart,
                                  boolean dataExist) throws InterruptedException{
        SearchResult searchResult = null;

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else {
            String query = StringUtils.EMPTY;
            try {
                query = "updatedDate>='" + startDateTimeByIssueType + "' order by updatedDate desc";

                log.info("jql= " + query);
                Instant start = Instant.now();

                Promise<SearchResult> promisedRs = client.getCustomIssueClient().searchBoardIssue(boardDetails.getBoardId(), query,
                        jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
                searchResult = promisedRs.claim();
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start, finish).toMillis();
                log.info("Time taken to fetch the data is {} milliseconds", timeElapsed);
                if (searchResult != null) {
                    log.info("Processing issues {} - {} out of {}", pageStart,
                            Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal());
                }
                log.info("Fetch jira board issues Api call delay started for project {}",projectConfig.getProjectName());
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                log.info("Fetch jira board issues Api call delay ended for project {}",projectConfig.getProjectName());
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
     * Gets all issues from JIRA
     *
     * @param startDateTimeByIssueType map of start dataTime of issue types
     * @param userTimeZone             user timezone
     * @param pageStart                page start
     * @param dataExist                dataExist on db or not
     * @return list of issues
     */
    @Override
    public SearchResult getIssues(ProjectConfFieldMapping projectConfig,
                                  Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart,
                                  boolean dataExist) throws InterruptedException{
        SearchResult searchResult = null;

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else if (StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey()) ||
                StringUtils.isEmpty(projectConfig.getProjectToolConfig().getBoardQuery())) {
            log.info("Either Project key is empty or boardQuery not provided. key {} boardquery {}"
                    , projectConfig.getProjectToolConfig().getProjectKey(), projectConfig.getProjectToolConfig().getBoardQuery());
        } else {
            StringBuilder query = new StringBuilder("project in (")
                    .append(projectConfig.getProjectToolConfig().getProjectKey()).append(") AND ");
            try {
                Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

                startDateTimeByIssueType.forEach((type, localDateTime) -> {
                    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                    String dateTimeStr = zonedDateTime.format(formatter);
                    startDateTimeStrByIssueType.put(type, dateTimeStr);

                });

                query.append(JiraProcessorUtil.processJql(projectConfig.getJira().getBoardQuery(),
                        startDateTimeStrByIssueType, dataExist));
                log.info("jql= " + query.toString());
                Instant start = Instant.now();

                Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
                        jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
                searchResult = promisedRs.claim();
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start, finish).toMillis();
                log.info("Time taken to fetch the data is {} milliseconds", timeElapsed);
                if (searchResult != null) {
                    log.info("Processing issues {} - {} out of {}", pageStart,
                            Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal());
                }
                log.info("Fetch jira board issues Api call delay started for project {}",projectConfig.getProjectName());
                TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                log.info("Fetch jira board issues Api call delay ended for project {}",projectConfig.getProjectName());
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

    public List<Issue> getEpicIssuesQuery(List<String> epicKeyList, ProjectConfFieldMapping projectConfFieldMapping) throws InterruptedException{
        List<Issue> issueList = new ArrayList<>();
        SearchResult searchResult = null;
        try {
            if (CollectionUtils.isNotEmpty(epicKeyList)) {
                String query = "key in (" + String.join(",", epicKeyList) + ")";
                int pageStart = 0;
                int totalEpic = 0;
                int fetchedEpic = 0;
                do {
                    Promise<SearchResult> promise = client.getSearchClient().searchJql(query,
                            jiraProcessorConfig.getPageSize(), pageStart, null);
                    searchResult = promise.claim();
                    if (searchResult != null) {
                        if (totalEpic == 0) {
                            totalEpic = searchResult.getTotal();
                        }
                        searchResult.getIssues().forEach(issue -> {
                            issueList.add(issue);
                        });
                        fetchedEpic += searchResult.getMaxResults();
                        pageStart += searchResult.getMaxResults();
                    }
                    log.info("epic Api call delay started for project {}",projectConfFieldMapping.getProjectName());
                    TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                    log.info("epic Api call delay ended for project {}",projectConfFieldMapping.getProjectName());
                } while (totalEpic < fetchedEpic);
            } else {
                log.info("No Epic Found to fetch");
            }
        } catch (RestClientException e) {
            log.error("error fetching epic", e.getCause());
        }
        return issueList;
    }

    /**
     * Returns all versions for a project, which are visible for the currently
     * logged in user
     *
     * @param projectKey the project key
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
     * @param projectConfig user provided project configuration
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
     * @param projectConfigList user provided project configuration
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
     * @param projectConfig user provided project configuration
     * @param jiraUserName  jira credentials username
     * @return URL
     * @throws MalformedURLException when URL not constructed properly
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
     * @param projectConfig   projectConfig
     * @param sprintId        sprintId
     * @param boardId         boardId
     * @param sprint          sprint
     * @param dbSprintDetails old dbSprintDetails
     */
    @Override
    public void getSprintReport(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
                                SprintDetails sprint, SprintDetails dbSprintDetails) {
        log.info("Start Calling sprint report api. Sprint Id : {} , Board Id : {}", sprintId, boardId);
        try {
            JiraToolConfig jiraToolConfig = projectConfig.getJira();

            if (null != jiraToolConfig) {
                URL url = getSprintReportUrl(projectConfig, sprintId, boardId);
                URLConnection connection;

                connection = url.openConnection();
                getReport(getDataFromServer(projectConfig, (HttpURLConnection) connection), sprint, projectConfig, dbSprintDetails, boardId);
            }
            log.info("End sprint report api. Sprint Id : {} , Board Id : {}", sprintId, boardId);
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
        serverURL = serverURL.replace("{rapidViewId}", boardId).replace("{sprintId}", sprintId);
        String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
        return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);

    }

    private void getReport(String sprintReportObj, SprintDetails sprint, ProjectConfFieldMapping projectConfig,
                           SprintDetails dbSprintDetails, String boardId) {
        if (StringUtils.isNotBlank(sprintReportObj)) {
            JSONArray completedIssuesJson = new JSONArray();
            JSONArray notCompletedIssuesJson = new JSONArray();
            JSONArray puntedIssuesJson = new JSONArray();
            JSONArray completedIssuesAnotherSprintJson = new JSONArray();
            JSONObject addedIssuesJson = new JSONObject();
            JSONObject entityDataJson = new JSONObject();

            boolean otherBoardExist = findIfOtherBoardExist(sprint);
            Set<SprintIssue> completedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getCompletedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> notCompletedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getNotCompletedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> puntedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getPuntedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> completedIssuesAnotherSprint = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getCompletedIssuesAnotherSprint(), boardId, otherBoardExist);
            Set<SprintIssue> totalIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getTotalIssues(), boardId, otherBoardExist);
            Set<String> addedIssues = initializeAddedIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getAddedIssues(), totalIssues, puntedIssues, otherBoardExist);
            try {
                JSONObject obj = (JSONObject) new JSONParser().parse(sprintReportObj);
                if (null != obj) {
                    JSONObject contentObj = (JSONObject) obj.get(CONTENTS);
                    completedIssuesJson = (JSONArray) contentObj.get(COMPLETED_ISSUES);
                    notCompletedIssuesJson = (JSONArray) contentObj.get(NOT_COMPLETED_ISSUES);
                    puntedIssuesJson = (JSONArray) contentObj.get(PUNTED_ISSUES);
                    completedIssuesAnotherSprintJson = (JSONArray) contentObj.get(COMPLETED_ISSUES_ANOTHER_SPRINT);
                    addedIssuesJson = (JSONObject) contentObj.get(ADDED_ISSUES);
                    entityDataJson = (JSONObject) contentObj.get(ENTITY_DATA);
                }

                populateMetaData(entityDataJson, projectConfig);

                setIssues(completedIssuesJson, completedIssues, totalIssues, projectConfig, boardId);

                setIssues(notCompletedIssuesJson, notCompletedIssues, totalIssues, projectConfig, boardId);

                setPuntedCompletedAnotherSprint(puntedIssuesJson, puntedIssues, projectConfig, boardId);

                setPuntedCompletedAnotherSprint(completedIssuesAnotherSprintJson, completedIssuesAnotherSprint,
                        projectConfig, boardId);

                addedIssues = setAddedIssues(addedIssuesJson, addedIssues);

                sprint.setCompletedIssues(completedIssues);
                sprint.setNotCompletedIssues(notCompletedIssues);
                sprint.setCompletedIssuesAnotherSprint(completedIssuesAnotherSprint);
                sprint.setPuntedIssues(puntedIssues);
                sprint.setAddedIssues(addedIssues);
                sprint.setTotalIssues(totalIssues);

            } catch (ParseException pe) {
                log.error("Parser exception when parsing statuses", pe);
            }
        }
    }

    private Set<SprintIssue> initializeIssues(Set<SprintIssue> sprintIssues, String boardId, boolean otherBoardExist) {
        if (otherBoardExist) {
            return CollectionUtils.emptyIfNull(sprintIssues).stream().filter(issue -> null != issue.getOriginBoardId() &&
                            !issue.getOriginBoardId().equalsIgnoreCase(boardId))
                    .collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }


    private Set<String> initializeAddedIssues(Set<String> addedIssue, Set<SprintIssue> totalIssues,
                                              Set<SprintIssue> puntedIssues, boolean otherBoardExist) {
        if (otherBoardExist) {
            if (null == addedIssue) {
                addedIssue = new HashSet<>();
            }
            Set<String> keySet = CollectionUtils.emptyIfNull(totalIssues).stream().map(issue -> issue.getNumber())
                    .collect(Collectors.toSet());
            keySet.addAll(CollectionUtils.emptyIfNull(puntedIssues).stream().map(issue -> issue.getNumber())
                    .collect(Collectors.toSet()));
            addedIssue.retainAll(keySet);
            return addedIssue;
        } else {
            return new HashSet<>();
        }
    }

    private boolean findIfOtherBoardExist(SprintDetails sprint) {
        boolean exist = false;
        if (null != sprint && sprint.getOriginBoardId().size() > 1) {
            exist = true;
        }
        return exist;
    }

    private void populateMetaData(JSONObject entityDataJson, ProjectConfFieldMapping projectConfig) {
        JiraIssueMetadata jiraIssueMetadata = new JiraIssueMetadata();
        if (Objects.nonNull(entityDataJson)) {
            jiraIssueMetadata.setIssueTypeMap(getMetaDataMap((JSONObject) entityDataJson.get("types"), "typeName"));
            jiraIssueMetadata.setStatusMap(getMetaDataMap((JSONObject) entityDataJson.get("statuses"), "statusName"));
            jiraIssueMetadata
                    .setPriorityMap(getMetaDataMap((JSONObject) entityDataJson.get("priorities"), "priorityName"));
            projectConfig.setJiraIssueMetadata(jiraIssueMetadata);
        }
    }

    private Map<String, String> getMetaDataMap(JSONObject object, String fieldName) {
        Map<String, String> map = new HashMap<>();
        if (null != object) {
            object.keySet().forEach(key -> {
                JSONObject innerObj = (JSONObject) object.get(key);
                Object fieldObject = innerObj.get(fieldName);
                if (null != fieldObject) {
                    map.put(key.toString(), fieldObject.toString());
                }
            });
        }
        return map;
    }

    /**
     * @param addedIssuesJson addedIssuesJson
     * @param addedIssues     addedIssues
     * @return added issues
     */
    @SuppressWarnings("unchecked")
    private Set<String> setAddedIssues(JSONObject addedIssuesJson, Set<String> addedIssues) {
        Set<String> keys = addedIssuesJson.keySet();
        if (CollectionUtils.isNotEmpty(keys)) {
            addedIssues = keys.stream().collect(Collectors.toSet());
        }
        return addedIssues;
    }

    /**
     * @param puntedIssuesJson
     * @param puntedIssues
     */
    @SuppressWarnings("unchecked")
    private void setPuntedCompletedAnotherSprint(JSONArray puntedIssuesJson, Set<SprintIssue> puntedIssues
            , ProjectConfFieldMapping projectConfig, String boardId) {
        puntedIssuesJson.forEach(puntedObj -> {
            JSONObject punObj = (JSONObject) puntedObj;
            if (null != punObj) {
                SprintIssue issue = getSprintIssue(punObj, projectConfig, boardId);
                puntedIssues.remove(issue);
                puntedIssues.add(issue);
            }
        });
    }

    private SprintIssue getSprintIssue(JSONObject obj, ProjectConfFieldMapping projectConfig,
                                       String boardId) {
        SprintIssue issue = new SprintIssue();
        issue.setNumber(obj.get(KEY).toString());
        issue.setOriginBoardId(boardId);
        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
        boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
        if (isCloudEnv) {
            issue.setPriority(getOptionalString(obj, "priorityName"));
            issue.setStatus(getOptionalString(obj, "statusName"));
            issue.setTypeName(getOptionalString(obj, "typeName"));
        } else {
            issue.setPriority(getName(projectConfig, PRIORITYID, obj));
            issue.setStatus(getName(projectConfig, STATUSID, obj));
            issue.setTypeName(getName(projectConfig, TYPEID, obj));
        }
        setEstimateStatistics(issue, obj, projectConfig);
        setTimeTrackingStatistics(issue, obj);
        return issue;
    }

    private void setTimeTrackingStatistics(SprintIssue issue, JSONObject obj) {
        Object timeEstimateFieldId = getStatisticsFieldId((JSONObject) obj.get("trackingStatistic"),
                "statFieldId");
        if (null != timeEstimateFieldId) {
            Object timeTrackingObject = getStatistics((JSONObject) obj.get("trackingStatistic"),
                    "statFieldValue", "value");
            issue.setRemainingEstimate(timeTrackingObject == null ? null : Double.valueOf(timeTrackingObject.toString()));
        }
    }

    private void setEstimateStatistics(SprintIssue issue, JSONObject obj, ProjectConfFieldMapping projectConfig) {
        Object currentEstimateFieldId = getStatisticsFieldId((JSONObject) obj.get("currentEstimateStatistic"),
                "statFieldId");
        if (null != currentEstimateFieldId) {
            Object estimateObject = getStatistics((JSONObject) obj.get("currentEstimateStatistic"),
                    "statFieldValue", "value");
            String storyPointCustomField = StringUtils.defaultIfBlank(projectConfig.getFieldMapping().getJiraStoryPointsCustomField(), "");
            if (storyPointCustomField.equalsIgnoreCase(currentEstimateFieldId.toString())) {
                issue.setStoryPoints(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
            } else {
                issue.setOriginalEstimate(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
            }
        }
    }

    private String getName(ProjectConfFieldMapping projectConfig, String entityDataKey, JSONObject jsonObject) {
        String name = null;
        Object obj = jsonObject.get(entityDataKey);
        if (null != obj) {
            JiraIssueMetadata metadata = projectConfig.getJiraIssueMetadata();
            switch (entityDataKey) {
                case PRIORITYID:
                    name = metadata.getPriorityMap().getOrDefault(obj.toString(), null);
                    break;
                case STATUSID:
                    name = metadata.getStatusMap().getOrDefault(obj.toString(), null);
                    break;
                case TYPEID:
                    name = metadata.getIssueTypeMap().getOrDefault(obj.toString(), null);
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    /**
     * @param projectConfig projectConfig
     * @param issues        issues
     * @param projectConfig projectConfig
     * @param totalIssues   totalIssues
     */
    @SuppressWarnings("unchecked")
    private void setIssues(JSONArray issuesJson, Set<SprintIssue> issues,
                           Set<SprintIssue> totalIssues, ProjectConfFieldMapping projectConfig,
                           String boardId) {
        issuesJson.forEach(jsonObj -> {
            JSONObject obj = (JSONObject) jsonObj;
            if (null != obj) {
                SprintIssue issue = getSprintIssue(obj, projectConfig, boardId);
                issues.remove(issue);
                issues.add(issue);
                totalIssues.remove(issue);
                totalIssues.add(issue);
            }
        });
    }

    private Object getStatistics(JSONObject object, String objectName, String fieldName) {
        Object resultObj = null;
        if (null != object) {
            JSONObject innerObj = (JSONObject) object.get(objectName);
            if (null != innerObj) {
                resultObj = innerObj.get(fieldName);
            }
        }
        return resultObj;
    }

    private Object getStatisticsFieldId(JSONObject object, String fieldName) {
        Object resultObj = null;
        if (null != object) {
            resultObj = object.get(fieldName);
        }
        return resultObj;
    }


    public List<Issue> getEpic(ProjectConfFieldMapping projectConfig, String boardId) throws InterruptedException {
        List<String> epicList = new ArrayList<>();
        try {
            JiraToolConfig jiraToolConfig = projectConfig.getJira();
            if (null != jiraToolConfig) {
                boolean isLast = false;
                int startIndex = 0;
                do {
                    URL url = getEpicUrl(projectConfig, boardId, startIndex);
                    URLConnection connection;
                    connection = url.openConnection();
                    String jsonResponse = getDataFromServer(projectConfig, (HttpURLConnection) connection);
                    isLast = populateData(jsonResponse, epicList);
                    startIndex = epicList.size();
                    TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                } while (!isLast);
            }
        } catch (RestClientException rce) {
            log.error("Client exception when loading sprint report", rce);
            throw rce;
        } catch (MalformedURLException mfe) {
            log.error("Malformed url for loading sprint report", mfe);
        } catch (IOException ioe) {
            log.error("IOException", ioe);
        }
        return getEpicIssuesQuery(epicList, projectConfig);
    }

    private boolean populateData(String sprintReportObj, List<String> epicList) {
        boolean isLast = true;
        if (StringUtils.isNotBlank(sprintReportObj)) {
            JSONArray valuesJson = new JSONArray();
            try {
                JSONObject obj = (JSONObject) new JSONParser().parse(sprintReportObj);
                if (null != obj) {
                    valuesJson = (JSONArray) obj.get("values");
                }
                getEpic(valuesJson, epicList);
                isLast = Boolean.valueOf(obj.get("isLast").toString());
            } catch (ParseException pe) {
                log.error("Parser exception when parsing statuses", pe);
            }
        }
        return isLast;
    }

    private void getEpic(JSONArray valuesJson, List<String> epicList) {
        valuesJson.forEach(values -> {
            JSONObject sprintJson = (JSONObject) values;
            if (null != sprintJson) {
                epicList.add(sprintJson.get(KEY).toString());
            }
        });
    }

    private URL getEpicUrl(ProjectConfFieldMapping projectConfig, String boardId, int startIndex)
            throws MalformedURLException {

        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
        String serverURL = jiraProcessorConfig.getJiraEpicApi();

        serverURL = serverURL.replace("{startAtIndex}", String.valueOf(startIndex)).replace("{boardId}", boardId);
        String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
        return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);
    }

    private String getOptionalString(final JSONObject jsonObject, final String attributeName) {
        final Object res = jsonObject.get(attributeName);
        if (res == null) {
            return null;
        }
        return res.toString();
    }
}
