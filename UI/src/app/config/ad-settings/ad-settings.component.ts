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
  styleUrls: []
})
export class AdSettingsComponent implements OnInit {
  adSettingsForm: UntypedFormGroup;
  adSettingsFormObj: any;
  submitted = false;

  constructor(private formBuilder: UntypedFormBuilder, private http: HttpService, private messenger: MessageService, private rsa: RsaEncryptionService) { }

  ngOnInit(): void {
    this.getADConfig();
  }

  // get AD configuration
  getADConfig() {
    this.initializeFields();
    this.adSettingsForm = this.formBuilder.group(this.adSettingsFormObj);
    this.http.getADConfig().subscribe(Response => {
      if (Response && Response.success) {
        if (Response.data) {
          for (const obj in Response.data) {
            if (obj !== 'password') {
              if (this.adSettingsForm && this.adSettingsForm.controls[obj]) {
                this.adSettingsForm.controls[obj].setValue(Response.data[obj]);
              }
            }
          }
        }
      }
    });
  }

  // convenience getter for easy access to form fields
  get adForm() {
    return this.adSettingsForm.controls;
  }

  initializeFields() {
    this.adSettingsFormObj = {
      username: ['', Validators.required],
      password: ['', Validators.required],
      host: ['', Validators.required],
      port: [null, Validators.required],
      rootDn: ['', Validators.required],
      domain: ['', Validators.required]
    };
  }

  submit() {
    this.submitted = true;
    // return if form is invalid
    if (this.adSettingsForm.invalid) {
      return;
    }

    const submitData = {};
    for (const obj in this.adForm) {
      if (obj === 'password') {
        submitData[obj] = this.rsa.encrypt(this.adForm[obj].value);
      } else {
        submitData[obj] = this.adForm[obj].value;
      }
    }

    this.http.setADConfig(submitData).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Saved successfully!! Custom API restart is required for changes to take effect.',
          detail: ''
        });
      } else {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
      }
    });
  }
}
