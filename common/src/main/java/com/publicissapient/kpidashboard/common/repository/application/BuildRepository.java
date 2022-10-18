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

package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

/**
 * Repository for {@link Build} data.
 */
@Repository
public interface BuildRepository
		extends CrudRepository<Build, ObjectId>, QuerydslPredicateExecutor<Build>, BuildRepositoryCustom {

	/**
	 * Finds the {@link Build} with the given number for a specific
	 * {@link ProcessorItem}.
	 *
	 * @param processorItemId
	 *            processor item id
	 * @param number
	 *            build number
	 * @return a {@link Build}
	 */
	Build findByProcessorItemIdAndNumber(ObjectId processorItemId, String number);

	/**
	 * Finds the list of {@link Build} for a specific {@link ProcessorItem}.
	 * 
	 * @param processorItemId
	 *            processor item id
	 * @return a list {@link Build}
	 */
	List<Build> findByProcessorItemIdAndBuildJob(ObjectId processorItemId, String buildJob);

	/**
	 * delete all documents with matching ids
	 * @param processorItemIds processor item id
	 */
	void deleteByProcessorItemIdIn(List<ObjectId> processorItemIds);

}
