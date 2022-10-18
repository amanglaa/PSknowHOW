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

cd UI/
path="$(pwd)"
echo $path
npm install
npm install -g @angular/cli@11.0.2
#npm install --save-dev @angular-devkit/build-angular
ng build --configuration=docker

mkdir -p /$path/build
cp -r dist/dashboard/* build/
chmod -R 775 /$path/build
cd $path/build
ls -lrt
tar -czvf ui2.tar *
mv ui2.tar ../files
rm -rf $path/build
echo "finish"
