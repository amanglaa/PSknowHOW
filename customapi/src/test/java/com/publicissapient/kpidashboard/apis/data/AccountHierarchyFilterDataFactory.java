/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Slf4j
public class AccountHierarchyFilterDataFactory {

	private static final String FILE_PATH_ACCOUNT_HIERARCHIES_FILTER_DATA = "/json/default/account_hierarchy_filter_data.json";
	private List<AccountHierarchyData> accountHierarchyDataList;
	private ObjectMapper mapper;

	private AccountHierarchyFilterDataFactory() {
	}

	public static AccountHierarchyFilterDataFactory newInstance(String filePath) {

		AccountHierarchyFilterDataFactory factory = new AccountHierarchyFilterDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static AccountHierarchyFilterDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ACCOUNT_HIERARCHIES_FILTER_DATA : filePath;

			accountHierarchyDataList = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<AccountHierarchyData>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading account hierarchies from file = " + filePath, e);
		}
	}

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		}
	}

    public List<AccountHierarchyData> getAccountHierarchyDataList() {
        return accountHierarchyDataList;
    }
}
