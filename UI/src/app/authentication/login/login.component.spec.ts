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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from '../login/login.component';
import { RegisterComponent } from '../register/register.component';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { SharedService } from '../../services/shared.service';
import { RsaEncryptionService } from '../../services/rsa.encryption.service';
import { TextEncryptionService } from '../../services/text.encryption.service';
import { MyprofileComponent } from '../../config/profile/myprofile/myprofile.component';

describe('LoginComponent', () => {

  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpreq;
  let httpService;
  let encryption;
  let aesEncryption;
  const fakeLogin = {
    "instance_owner": "kbakshi@sapient.com",
    "user_email": "test@gmail.com",
    "projectsAccess": [],
    "user_name": "SUPERADMIN",
    "account_name": "XYZ",
    "X-Authentication-Token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTVVBFUkFETUlOIiwiZGV0YWlscyI6IlNUQU5EQVJEIiwicm9sZXMiOlsiUk9MRV9TVVBFUkFETUlOIl0sImV4cCI6MTY2NTE2ODU2MH0.Oi-U3qdd5OuZUazy9OBOSAjWh7trjGDkZiMMJ51qmig8Jy59aB4N5tAPWEbVSrZn2Z4hNgKkLzR6BG2pAudhvA",
    "project_name": "XYZ",
    "authorities": [
      "ROLE_SUPERADMIN"
    ]
  }
  const fakeInvalidLogin = { 'timestamp': 1567511436517, 'status': 401, 'error': 'Unauthorized', 'message': 'Authentication Failed: Login Failed: The username or password entered is incorrect', 'path': '/api/login' };

  beforeEach(waitForAsync(() => {

    const routes: Routes = [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'authentication/login', component: LoginComponent },
      { path: 'dashboard/Config/Profile', component: MyprofileComponent },

    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        HttpClientTestingModule, // no need to import http client module
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [LoginComponent,
        RegisterComponent, DashboardComponent, MyprofileComponent],
      providers: [{ provide: APP_CONFIG, useValue: AppConfig },
        HttpService, SharedService, RsaEncryptionService, TextEncryptionService
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]

    })
      .compileComponents();

      fixture = TestBed.createComponent(LoginComponent);
      component = fixture.componentInstance;
      httpService = TestBed.get(HttpService);
      encryption = TestBed.get(RsaEncryptionService);
      httpMock = TestBed.get(HttpTestingController);
      aesEncryption = TestBed.get(TextEncryptionService);
      fixture.detectChanges();
  }));


  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('remember me with saved value', waitForAsync(() => {
    fixture.detectChanges();
    localStorage.setItem('SpeedyUser', 'fakeUser');
    localStorage.setItem('SpeedyPassword', aesEncryption.convertText('fakeUserPswd', 'encrypt'));
    component.rememberMe();
    expect(component.rememberMeCheckbox).toBeTruthy();
  }));

  it('invalid form should not call login', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    component.onSubmit('standard');
    expect(component.loginForm.invalid).toBeTruthy();
  }));

  xit('valid form with correct username pswd', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('User@123');
    component.onSubmit('standard');
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.flush(fakeLogin);
    expect(component.loginForm.valid).toBeTruthy();

  }));

  // 0 status
  it('Internal server error login requests', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('User@123');
    component.onSubmit('standard');
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.error('');
    expect(component.error).toBe('Internal Server Error');
  }));


  // 404 status
  it('Unauthorized login requests', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('User@123');
    component.onSubmit('standard');
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.error(fakeInvalidLogin, fakeInvalidLogin);
    expect(component.error).toBe(fakeInvalidLogin.message);

  }));

});
