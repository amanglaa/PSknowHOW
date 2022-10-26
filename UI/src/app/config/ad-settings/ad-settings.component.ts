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

import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { RsaEncryptionService } from '../../services/rsa.encryption.service';


@Component({
  selector: 'app-ad-settings',
  templateUrl: './ad-settings.component.html',
  styleUrls: ['./ad-settings.component.css']
})
export class AdSettingsComponent implements OnInit {
  adSettingsForm: UntypedFormGroup;
  adSettingsFormObj: any;
  // standardLoginForm: UntypedFormGroup;
  // standardLoginFormObj: any;
  // pingAuthenticationForm: UntypedFormGroup;
  // pingAuthenticationFormObj: any;
  submitted = false;
  loginSettingsTypes = [{
    name: 'standardLogin',
    label: 'KnowHOW Local Authentication'
  },
  {
    name: 'adLogin',
    label: 'AD Authentication'
  }];

  selectedTypes: any[] = [];
  disableSave: boolean;

  constructor(private formBuilder: UntypedFormBuilder, private http: HttpService, private messenger: MessageService, private rsa: RsaEncryptionService) { }

  ngOnInit(): void {
    this.getAuthSettings();
  }

  // get AD configuration
  getAuthSettings() {
    this.initializeFields();
    this.adSettingsForm = this.formBuilder.group(this.adSettingsFormObj);
    // this.standardLoginForm = this.formBuilder.group(this.standardLoginFormObj);
    // this.pingAuthenticationForm = this.formBuilder.group(this.pingAuthenticationFormObj);
    this.http.getAuthConfig().subscribe(response => {
      if (response && response.success) {
        if (response && response.data && response.data.authTypeStatus) {
          if (response.data.authTypeStatus.standardLogin) {
            this.selectedTypes.push({
              name: 'standardLogin',
              label: 'KnowHOW Local Authentication'
            });
          }

          if (response.data.authTypeStatus.adLogin) {
            this.selectedTypes.push({
              name: 'adLogin',
              label: 'AD Authentication'
            });
          }
        }
        if (response.success && response.data && response.data.adServerDetail) {
          for (const obj in response.data.adServerDetail) {
            if (obj !== 'password') {
              if (this.adSettingsForm && this.adSettingsForm.controls[obj]) {
                this.adSettingsForm.controls[obj].setValue(response.data.adServerDetail[obj]);
              }
            }
          }
        }
      }
    });
  }

  typeNotSelected(formName) {
    if (!this.selectedTypes.map((type) => type.name).includes(formName)) {
      return true;
    } else {
      return false;
    }
  }

  checkValues() {
    if (!this.selectedTypes.map((type) => type.name).length) {
      this.disableSave = true;
    } else {
      this.disableSave = false;
    }
  }

  // convenience getter for easy access to form fields
  get adForm() {
    return this.adSettingsForm.controls;
  }

  // get standardLogin() {
  //   return this.standardLoginForm.controls;
  // }

  // get pingForm() {
  //   return this.pingForm.controls;
  // }


  initializeFields() {
    this.adSettingsFormObj = {
      username: ['', Validators.required],
      password: ['', Validators.required],
      host: ['', Validators.required],
      port: [null, Validators.required],
      rootDn: ['', Validators.required],
      domain: ['', Validators.required]
    };

    // this.standardLoginFormObj = {
    //   username: ['', Validators.required],
    //   password: ['', Validators.required]
    // };

    // this.pingAuthenticationFormObj = {
    //   field1: ['', Validators.required],
    //   field2: ['', Validators.required]
    // };
  }

  submit() {
    this.submitted = true;
    const submitData = {};
    submitData['authTypeStatus'] = {};
    this.selectedTypes.forEach((item) => {
      submitData['authTypeStatus'][item.name] = true;
    });

    if (this.selectedTypes.map((type) => 'adLogin').length) {
      // return if form is invalid
      if (this.adSettingsForm.invalid) {
        return;
      } else {
        submitData['adServerDetail'] = {};
        for (const obj in this.adForm) {
          if (obj === 'password') {
            submitData['adServerDetail'][obj] = this.rsa.encrypt(this.adForm[obj].value);
          } else {
            submitData['adServerDetail'][obj] = this.adForm[obj].value;
          }
        }
      }
    }

    this.http.setAuthConfig(submitData).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Saved successfully!! Custom API restart is required for changes to take effect.',
          detail: ''
        });
      } else {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred!'
        });
      }
    }, err => {
      this.messenger.add({
        severity: 'error',
        summary: err.error.message
      });
    });
  }
}
