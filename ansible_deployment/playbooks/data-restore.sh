#!/bin/sh
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

mongo=`docker ps | grep mongodb | awk '{ print $1}'`

#mv /app/apps/db_data/ /app/apps/db_data2
#docker restart $mongo
docker stop customapi jira-processor nonjira-processor azure-processor nonazureboards-processor 
sleep 130
echo -e " ${GREEN}[\xE2\x9C\x94] droping db ...${RESET}"
echo ""
docker exec -it $mongo mongo localhost:27017/kpidashboard --username admin --password reset@123 --authenticationDatabase "admin" --eval "printjson (db.dropDatabase())"
sleep 120
echo -e " ${GREEN}[\xE2\x9C\x94] copy dump to mongo container ...${RESET}"
echo ""
docker exec -t $mongo /bin/bash -c "rm -rf /tmp/kpidashboardbackups" 
docker cp /var/backupsnew/kpidashboard $mongo:/tmp/kpidashboardbackups

echo -e " ${GREEN}[\xE2\x9C\x94] restoring dump to mongo container ...${RESET}"
echo ""

docker exec -t $mongo mongorestore --username devadmin --password admin@123 --db kpidashboard /tmp/kpidashboardbackups
sleep 40
docker restart mongodb 
docker start customapi jira-processor nonjira-processor azure-processor nonazureboards-processor
docker restart ui
