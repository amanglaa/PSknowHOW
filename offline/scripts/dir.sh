#!/bin/bash

################################################################################
# Copyright 2014 CapitalOne, LLC.
# Further development Copyright 2022 Sapient Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
################################################################################

VOLUME_ROOT_DIR=/app
PROJECT_DIR=$VOLUME_ROOT_DIR/apps
PROJECT_ROOT_SUBDIRS=(db_data logs properties offline_data central-data)
ROOT_LOGS_SUBDIRS=(ui-2.0 customapi mongodb combined_collector central-producer)
OFFLINE_DATA_SUBDIR=(excel offline_json) 
OFFLINE_DATA_JSON=(jira sonar new_relic)
OFFLINE_DATA_EXCEL=(capacity engg_maturity engg_maturity_master project_offline_data)
CENTRAL_DATA_PRODUCER=(producer-upload producer-archive)

function volume_directory()
{
   echo -e "${GREEN}[\xE2\x9C\x94] creating directory structure to map volumes ${RESET}"     
      if [ ! -d "$VOLUME_ROOT_DIR" ]; then
         mkdir $VOLUME_ROOT_DIR/
         if [ $? -ne 0 ]; then
            echo "    ${RED}[\xE2\x9D\x8C] failed to create directory $VOLUME_ROOT_DIR ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $VOLUME_ROOT_DIR directory created ${RESET}"
         fi
      fi
   
      if [ ! -d "$PROJECT_DIR" ]; then
         mkdir -p $PROJECT_DIR
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR directory created ${RESET}"
         fi
      fi
	for folder in ${PROJECT_ROOT_SUBDIRS[@]}; do
      if [ ! -d "$PROJECT_DIR/$folder" ]; then
         mkdir -p $PROJECT_DIR/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/$folder directory created ${RESET}"
         fi
      fi
   done
   for folder in ${PROOT_LOGS_SUBDIRS[@]}; do
      if [ ! -d "$PROJECT_DIR/logs/$folder" ]; then
         mkdir -p $PROJECT_DIR/logs/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/logs/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/logs/$folder directory created ${RESET}"
         fi
      fi
   done
   for folder in ${OFFLINE_DATA_SUBDIR[@]}; do
      if [ ! -d "$PROJECT_DIR/offline_data/$folder" ]; then
         mkdir -p $PROJECT_DIR/offline_data/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/offline_data/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/offline_data/$folder directory created ${RESET}"
         fi
      fi
   done
   for folder in ${OFFLINE_DATA_JSON[@]}; do
      if [ ! -d "$PROJECT_DIR/offline_data/offline_json/$folder" ]; then
         mkdir -p $PROJECT_DIR/offline_data/offline_json/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/offline_data/offline_json/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/offline_data/offline_json/$folder directory created ${RESET}"
         fi
      fi
   done
   for folder in ${OFFLINE_DATA_EXCEL[@]}; do
      if [ ! -d "$PROJECT_DIR/offline_data/excel/$folder" ]; then
         mkdir -p $PROJECT_DIR/offline_data/excel/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/offline_data/excel/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/offline_data/excel/$folder directory created ${RESET}"
         fi
      fi
   done
   for folder in ${CENTRAL_DATA_PRODUCER[@]}; do
      if [ ! -d "$PROJECT_DIR/central-data/$folder" ]; then
         mkdir -p $PROJECT_DIR/central-data/$folder
         if [ $? -ne 0 ]; then
            echo -e "    ${RED}[\xE2\x9D\x8C] failed to create directory $PROJECT_DIR/central-data/$folder ${RESET}"
            exit 1
         else
            echo -e "    ${CYAN}[\xE2\x9C\x94] $PROJECT_DIR/central-data/$folder directory created ${RESET}"
         fi
      fi
   done
}
volume_directory
