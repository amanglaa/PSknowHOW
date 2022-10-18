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

RED=`tput setaf 1`
GREEN=`tput setaf 2`
RESET=`tput sgr0`
YELLOW=`tput setaf 3`
CYAN=`tput setaf 6`
BLUE=`tput setaf 4`
MAGENTA=`tput setaf 5`
BLANK=`echo`


SCRIPT=$(dirname `readlink -f "$0"`)/scripts
PROJECT=/app/apps

sh -x ${SCRIPT}/dir.sh
echo -e "${GREEN}[\xE2\x9C\x94] successfully created directory structure ${RESET}"

sh -x ${SCRIPT}/backup.sh
echo -e "${GREEN}[\xE2\x9C\x94] successfully mongodb backup completed and old-containers down ${RESET}"

sh -x ${SCRIPT}/docker-images-up.sh
echo -e "${GREEN}[\xE2\x9C\x94] docker-images up ${RESET}"

cp -up docker-compose.yml ${PROJECT}/docker-compose.yml
cd ${PROJECT} && docker-compose up -d
echo -e "${GREEN}[\xE2\x9C\x94] docker-compose up ${RESET}"

sh -x ${SCRIPT}/ipwhitelist.sh
echo -e "${GREEN}[\xE2\x9C\x94] added system ip to docker-compose file and restarted docker-compose file ${RESET}"

sh -x ${SCRIPT}/restore.sh
echo -e "${GREEN}[\xE2\x9C\x94] successfully restored mongodb  ${RESET}"
