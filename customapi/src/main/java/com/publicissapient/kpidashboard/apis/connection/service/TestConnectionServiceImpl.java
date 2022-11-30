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

package com.publicissapient.kpidashboard.apis.connection.service;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.service.RsaEncryptionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TestConnectionServiceImpl implements TestConnectionService {

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private RsaEncryptionService rsaEncryptionService;

	@Autowired
	private RestTemplate restTemplate;

	private static final String SPACE = " ";
	private static final String ENCODED_SPACE = "%20";
	private static final String URL_SAPERATOR = "/";
	private static final int GITHUB_RATE_LIMIT_PER_HOUR = 60;
	private static final String VALID_MSG = "Valid Credentials ";
	private static final String INVALID_MSG = "Invalid Credentials ";


	@Override
	public ServiceResponse validateConnection(Connection connection, String toolName) {
		String apiUrl = "";
		String password = getPassword(connection, toolName);
		int statusCode = 0;
		switch (toolName) {
		case Constant.TOOL_BITBUCKET:
			apiUrl = createBitBucketUrl(connection);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_AZURE:
		case Constant.TOOL_AZUREREPO:
		case Constant.TOOL_AZUREPIPELINE:
			apiUrl = createAzureApiUrl(connection.getBaseUrl(), toolName);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_GITHUB:
			apiUrl = createGitHubTestConnectionUrl(connection);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_SONAR:
			if (connection.isCloudEnv()) {
				apiUrl = createCloudApiUrl(connection.getBaseUrl(), toolName);
				if (checkDetailsForTool(apiUrl, password)) {
					statusCode = validateTestConn(connection, apiUrl, password, toolName);
				}
			} else {
				apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
				if (!connection.isCloudEnv() && connection.isAccessTokenEnabled()){
					if (checkDetailsForTool(apiUrl, password)) {
						statusCode = validateTestConn(connection, apiUrl, password, toolName);
					}
				}else {
					statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
				}
			}
			break;
		case Constant.TOOL_ZEPHYR:
			if (connection.isCloudEnv()) {
				apiUrl = createCloudApiUrl(connection.getBaseUrl(), toolName);
				if (checkDetailsForTool(apiUrl, password)) {
					statusCode = validateTestConn(connection, apiUrl, password, toolName);
				}
			} else {
				apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
				statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			}
			break;
		case Constant.TOOL_JIRA:
		case Constant.TOOL_TEAMCITY:
		case Constant.TOOL_BAMBOO:
		case Constant.TOOL_JENKINS:
			apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
			statusCode = testConnectionDetails(connection, apiUrl, password, toolName);
			break;
		case Constant.TOOL_GITLAB:
			apiUrl = createApiUrl(connection.getBaseUrl(), toolName);
			if (checkDetailsForTool(apiUrl, password)) {
				String decryptedPswd = rsaEncryptionService.decrypt(password, customApiConfig.getRsaPrivateKey());
				statusCode = validateTestConn(connection, apiUrl, decryptedPswd, toolName);
			}
			break;
		default:
			return new ServiceResponse(false, "Invalid Toolname", HttpStatus.NOT_FOUND);
		}

		if (statusCode == HttpStatus.OK.value()) {
			return new ServiceResponse(true, VALID_MSG, statusCode);
		}

		if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
			return new ServiceResponse(false, INVALID_MSG, statusCode);
		}

		return new ServiceResponse(false, "Password/API token missing", HttpStatus.NOT_FOUND);
	}

	private boolean testConnection(Connection connection, String toolName, String apiUrl,
								   String password, boolean isSonarWithAccessToken) {
		boolean isValidConnection;
		HttpStatus status = null;
		if(isSonarWithAccessToken){
			getApiResponseWithBasicAuth(password, "", apiUrl, toolName);
		}else {
			getApiResponseWithBasicAuth(connection.getUsername(), password, apiUrl, toolName);
		}
		isValidConnection = status.is2xxSuccessful();
		return isValidConnection;
	}
	
	private boolean checkDetails(String apiUrl, String password, Connection connection) {
		boolean b = false;
		if (apiUrl != null && isUrlValid(apiUrl) && StringUtils.isNotEmpty(password)
				&& StringUtils.isNotEmpty(connection.getUsername())) {
			b = true;
		}
		return b;
	}

	private int testConnectionDetails(Connection connection, String apiUrl, String password, String toolName) {
		int status = 0;
		if (checkDetails(apiUrl, password, connection)) {
			status = validateTestConn(connection, apiUrl, password, toolName);
		}
		return status;
	}

	private int validateTestConn(Connection connection, String apiUrl, String password, String toolName) {
		boolean isValid;
		int statusCode;
		if (toolName.equals(Constant.TOOL_GITHUB)) {
			isValid = testConnectionForGitHub(apiUrl, connection.getUsername(), password);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else if ((toolName.equals(Constant.TOOL_ZEPHYR) && connection.isCloudEnv())
				|| toolName.equals(Constant.TOOL_GITLAB)) {
			isValid = testConnectionForTools(apiUrl, password);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else if (toolName.equals(Constant.TOOL_SONAR)) {
			if(connection.isCloudEnv()) {
				String accessToken = rsaEncryptionService.decrypt(password, customApiConfig.getRsaPrivateKey());
				isValid = testConnectionForTools(apiUrl, accessToken);
			} else if (!connection.isCloudEnv() && connection.isAccessTokenEnabled()) {
				String accessToken = rsaEncryptionService.decrypt(password, customApiConfig.getRsaPrivateKey());
				isValid = testConnectionForTools(apiUrl, accessToken);
			}else{
				isValid = testConnection(connection, toolName, apiUrl, password, false);
			}
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		} else {
			isValid = testConnection(connection, toolName, apiUrl, password, false);
			statusCode = isValid ? HttpStatus.OK.value() : HttpStatus.UNAUTHORIZED.value();
		}
		return statusCode;
	}

	private boolean testConnectionForGitHub(String apiUrl, String username, String password) {

		HttpHeaders httpHeaders = createHeadersWithAuthentication(username, password);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			HttpHeaders headers = result.getHeaders();
			List<String> rateLimits = headers.get("X-RateLimit-Limit");
			return CollectionUtils.isNotEmpty(rateLimits)
					&& Integer.valueOf(rateLimits.get(0)) > GITHUB_RATE_LIMIT_PER_HOUR;
		} else {
			return false;
		}

	}

	private boolean testConnectionForTools(String apiUrl, String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> result = restTemplate.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity,
					String.class);
			return result.getStatusCode().is2xxSuccessful();
		} catch (HttpClientErrorException e) {
			log.error(INVALID_MSG);
			return e.getStatusCode().is5xxServerError();
		}

	}

	private String createGitHubTestConnectionUrl(Connection connection) {

		return connection.getBaseUrl() + URL_SAPERATOR + "users" + URL_SAPERATOR + connection.getUsername();
	}

	private String createAzureApiUrl(String baseUrl, String toolName) {
		String resultUrl = "";

		String baseUrlWithoutTrailingSlash = StringUtils.removeEnd(baseUrl, URL_SAPERATOR);
		String baseUrlWithEncodedSpace = StringUtils.replace(baseUrlWithoutTrailingSlash, SPACE, ENCODED_SPACE);
		String apiEndPoint = "";
		switch (toolName) {
		case Constant.TOOL_AZURE:
			apiEndPoint = customApiConfig.getAzureBoardApi();
			break;
		case Constant.TOOL_AZUREREPO:
			apiEndPoint = customApiConfig.getAzureRepoApi();
			break;
		case Constant.TOOL_AZUREPIPELINE:
			apiEndPoint = customApiConfig.getAzurePipelineApi();
			break;

		default:
			log.info("Tool name is invalid or empty");
			break;
		}

		if (StringUtils.isNotEmpty(apiEndPoint)) {
			resultUrl = baseUrlWithEncodedSpace + URL_SAPERATOR + apiEndPoint;
		}

		return resultUrl;
	}

	private boolean checkDetailsForTool(String apiUrl, String password) {
		boolean b = false;
		if (apiUrl != null && isUrlValid(apiUrl) && StringUtils.isNotEmpty(password)) {
			b = true;
		}
		return b;
	}

	/**
	 * Create API URL using base URL and API path for bitbucket
	 * 
	 * @param baseUrl
	 * @param apiEndPoint
	 * @return apiURL
	 */
	private String createBitBucketUrl(Connection connection) {
		URI uri = URI.create(connection.getBaseUrl().replace(SPACE, ENCODED_SPACE));
		if(connection.isCloudEnv()) {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/")
			+ "/workspaces/";
		}else {
			return uri.getScheme() + "://" + uri.getHost() + StringUtils.removeEnd(connection.getApiEndPoint(), "/")
				+ "/projects/";
		}
	}

	/**
	 * Create HTTP header with basic Authentication
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private HttpHeaders createHeadersWithAuthentication(String username, String password) {
		String decryptedPswd = rsaEncryptionService.decrypt(password, customApiConfig.getRsaPrivateKey());
		String plainCreds = username + ":" + decryptedPswd;
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCreds.getBytes());
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}

	/**
	 * Make API call to validate Credentials
	 * 
	 * @param username
	 * @param password
	 * @param apiUrl
	 * @return API response
	 */
	private HttpStatus getApiResponseWithBasicAuth(String username, String password, String apiUrl, String toolName) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders httpHeaders;
		ResponseEntity<?> responseEntity;
		httpHeaders = createHeadersWithAuthentication(username, password);
		HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
		try {
			responseEntity = rest.exchange(URI.create(apiUrl), HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Invalid login credentials");
			return e.getStatusCode();
		}

		Object responseBody = responseEntity.getBody();
		if (toolName.equalsIgnoreCase(Constant.TOOL_SONAR) && responseBody != null
				&& responseBody.toString().contains("false")) {
			return HttpStatus.UNAUTHORIZED;

		}
		if (toolName.equalsIgnoreCase(Constant.TOOL_BITBUCKET)
				&& responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return HttpStatus.OK;
		}

		return responseEntity.getStatusCode();
	}

	/**
	 * Create API URL using base URL and API path
	 * 
	 * @param baseUrl
	 * @param toolName
	 * @return apiURL
	 */
	private String createApiUrl(String baseUrl, String toolName) {
		String apiPath = getApiPath(toolName);
		if (StringUtils.isNotEmpty(baseUrl) && StringUtils.isNotEmpty(apiPath)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(apiPath) : baseUrl.concat("/").concat(apiPath);
		}
		return null;
	}

	private String createCloudApiUrl(String baseUrl, String toolName) {
		String apiPath = "healthcheck";
		String endpoint = "api/favorites/search";
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl)
				&& StringUtils.isNotEmpty(apiPath)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(apiPath) : baseUrl.concat("/").concat(apiPath);
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && StringUtils.isNotEmpty(baseUrl)
				&& StringUtils.isNotEmpty(endpoint)) {
			return baseUrl.endsWith("/") ? baseUrl.concat(endpoint) : baseUrl.concat("/").concat(endpoint);
		}
		return null;
	}

	/**
	 * this method returns API path from configuration
	 * 
	 * @param toolName
	 * @return apiPath
	 */
	private String getApiPath(String toolName) {
		switch (toolName) {
		case Constant.TOOL_JIRA:
			return customApiConfig.getJiraTestConnection();
		case Constant.TOOL_SONAR:
			return customApiConfig.getSonarTestConnection();
		case Constant.TOOL_TEAMCITY:
			return customApiConfig.getTeamcityTestConnection();
		case Constant.TOOL_BAMBOO:
			return customApiConfig.getBambooTestConnection();
		case Constant.TOOL_JENKINS:
			return customApiConfig.getJenkinsTestConnection();
		case Constant.TOOL_GITLAB:
			return customApiConfig.getGitlabTestConnection();
		case Constant.TOOL_BITBUCKET:
			return customApiConfig.getBitbucketTestConnection();
		case Constant.TOOL_ZEPHYR:
			return customApiConfig.getZephyrTestConnection();
		default:
			return null;
		}
	}

	/**
	 * checks if input URL is valid or not
	 * 
	 * @param url
	 * @return
	 */
	private boolean isUrlValid(String url) {
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(url);
	}

	private String getPassword(Connection connection, String toolName) {
		if (Constant.TOOL_GITHUB.equalsIgnoreCase(toolName)) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_ZEPHYR.equalsIgnoreCase(toolName) && connection.isCloudEnv()) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_GITLAB.equalsIgnoreCase(toolName)) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) && connection.isCloudEnv()) {
			return connection.getAccessToken();
		}
		if (Constant.TOOL_SONAR.equalsIgnoreCase(toolName) &&
				StringUtils.isNotEmpty(connection.getAccessToken())) {
			return connection.getAccessToken();
		}
		return connection.getPassword() != null ? connection.getPassword() : connection.getApiKey();
	}

}
