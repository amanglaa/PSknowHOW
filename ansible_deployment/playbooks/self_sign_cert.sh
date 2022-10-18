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
openssl req -newkey rsa:4096 \
            -x509 \
            -sha256 \
            -days 3650 \
            -nodes \
            -out /tmp/selfcert.crt \
            -keyout /tmp/selfcert.key \
            -subj "/C=IN/ST=HR/L=ggn/O=Security/OU=IT Department/CN=$1"

ui=`sudo docker ps | grep ui | awk '{ print $1}'`
sudo docker exec -t $ui /bin/bash -c " cp /etc/ssl/certs/speedy2.crt /tmp "
sudo docker exec -t $ui /bin/bash -c " cp /etc/ssl/certs/speedy2.key /tmp "
sudo docker cp /tmp/selfcert.crt $ui:/etc/ssl/certs/speedy2.crt
sudo docker cp /tmp/selfcert.key $ui:/etc/ssl/certs/speedy2.key
sudo docker restart ui

