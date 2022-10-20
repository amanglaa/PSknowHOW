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

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RsaEncryptionService } from 'src/app/services/rsa.encryption.service';

@Injectable({
  providedIn: 'root'
})
export class TestConnectionService {

  constructor(private http: HttpClient, private rsa: RsaEncryptionService) { }

  /** get: test JIRA connection */
  testJira(baseUrl, apiEndPoint, username, password, vault): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: password ? this.rsa.encrypt(password) : '',
      vault: vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/jira', postData
      , { headers }
    );
  }

  testZephyr(baseUrl, username, password, apiEndPoint, accessToken, cloudEnv, vault): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: password ? this.rsa.encrypt(password) : '',
      apiEndPoint: apiEndPoint,
      accessToken: accessToken,
      cloudEnv: cloudEnv,
      vault: vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/zephyr', postData
      , { headers }
    );
  }

  testAzureBoards(baseUrl, username, pat): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: this.rsa.encrypt(pat)
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azureboard', postData
      , { headers }
    );
  }

    testGitLab(baseUrl, accessToken): Observable<any> {
      const postData = {
        baseUrl: baseUrl,
        accessToken: this.rsa.encrypt(accessToken)
      };
      let headers: HttpHeaders = new HttpHeaders();
      headers = headers.append('requestArea', 'thirdParty');
      return this.http.post(environment.baseUrl + '/api/testconnection/gitlab', postData
        , { headers }
      );
    }

  testBitbucket(baseUrl, username, password, apiEndPoint, cloudEnv, vault): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: password ? this.rsa.encrypt(password) : '',
      apiEndPoint: apiEndPoint,
      cloudEnv: cloudEnv,
      vault: vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/bitbucket', postData
      , { headers }
    );
  }

  testSonar(baseUrl, username, password, accesstoken, cloudEnv, vault): Observable<any> {

    let postData = {};

    if (cloudEnv) {

      postData = {
        baseUrl: baseUrl,
        accessToken: this.rsa.encrypt(accesstoken),
        cloudEnv: true,
        vault: vault
      };
    } else {
      postData = {
        baseUrl: baseUrl,
        username: username,
        password: password ? this.rsa.encrypt(password) : '',
        cloudEnv: false,
        vault: vault
      };
    }

    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/sonar', postData
      , { headers }
    );
  }

  testJenkins(baseUrl, username, apiKey): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      apiKey: this.rsa.encrypt(apiKey)
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/jenkins', postData
      , { headers }
    );
  }

  testNewRelic(apiEndPoint, apiKey, apiKeyFieldName): Observable<any> {
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'external');
    headers = headers.append(apiKeyFieldName, apiKey);
    return this.http.get(`${apiEndPoint}Select * from Metric`, {
      headers
    });
  }

  testBamboo(baseUrl, username, password, vault): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: password ? this.rsa.encrypt(password) : '',
      vault: vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/bamboo', postData
      , { headers }
    );
  }

  testTeamCity(baseUrl, username, password, vault): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: password ? this.rsa.encrypt(password) : '',
      vault: vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/teamcity', postData
      , { headers }
    );
  }

  testAzurePipeline(baseUrl, username, pat): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: this.rsa.encrypt(pat)
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azurepipeline', postData
      , { headers }
    );
  }

  testAzureRepository(baseUrl, username, pat): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      password: this.rsa.encrypt(pat)
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azurerepo', postData
      , { headers }
    );

  }

  testGithub(baseUrl, username, accessToken): Observable<any> {
    const postData = {
      baseUrl: baseUrl,
      username: username,
      accessToken: this.rsa.encrypt(accessToken),
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/github', postData
      , { headers }
    );
  }



}
