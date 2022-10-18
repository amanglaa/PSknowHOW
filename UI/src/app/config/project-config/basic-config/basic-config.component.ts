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

import { ChangeDetectionStrategy, Component, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, FormControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { TextEncryptionService } from '../../../services/text.encryption.service';
declare const require: any;

@Component({
  selector: 'app-basic-config',
  // changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './basic-config.component.html',
  styleUrls: ['./basic-config.component.css']
})
export class BasicConfigComponent implements OnInit {
  basicConfFormObj: any;
  dataLoading: boolean;
  projectTypeOptions: any = [];
  suggestions: any = [];
  submitted = false;
  selectedType = false;
  selectedProject: any;
  disableSave: boolean = false;
  ifSuperUser: boolean = false;
  configuredTools: any = [];
  loading: boolean = false;
  formData: any;
  getFieldsResponse: any;
  public form: FormGroup = this.formBuilder.group({});
  blocked: boolean = true;

  constructor(private formBuilder: FormBuilder, private sharedService: SharedService, private http: HttpService, private messenger: MessageService, private getAuthorizationService: GetAuthorizationService, private aesEncryption: TextEncryptionService) {
    this.projectTypeOptions = [
      { name: 'Scrum', value: false },
      { name: 'Kanban', value: true }
    ];
  }

  ngOnInit(): void {
    this.getFields();
    this.ifSuperUser = this.getAuthorizationService.checkIfSuperUser();
    this.selectedProject = this.sharedService.getSelectedProject();
    this.sharedService.setSelectedFieldMapping(null);
  }

  getFields() {
    // api call to get formData
    this.blocked = true;
    let formFieldData = JSON.parse(localStorage.getItem('hierarchyData'));
    this.formData = JSON.parse(JSON.stringify(formFieldData));
    this.getFieldsResponse = JSON.parse(JSON.stringify(formFieldData));
    this.formData.unshift(
      {
        'level': 0,
        'hierarchyLevelId': 'kanban',
        'hierarchyLevelName': 'Project Methodology',
        'inputType': 'switch',
        'value': false,
        'required': true
      });

    this.formData.push(
      {
        'level': this.formData.length,
        'hierarchyLevelId': 'projectName',
        'hierarchyLevelName': 'Project Name',
        'inputType': 'text',
        'value': '',
        'required': true
      }
    );

    this.formData.forEach(control => {
      this.form.addControl(
        control.hierarchyLevelId,
        this.formBuilder.control(control.value, [Validators.required, this.stringValidator])
      );
    });
    this.blocked = false;

  }

  search(event, field) {
    let filtered: any[] = [];
    let query = event.query;
    for (let i = 0; i < field.suggestions.length; i++) {
      let country = field.suggestions[i];
      if (country.name.toLowerCase().indexOf(query.toLowerCase()) == 0) {
        filtered.push(country);
      }
    }

    field.filteredSuggestions = filtered;
  }

  onSubmit() {
    let formValue = this.form.getRawValue();
    let submitData = {};
    submitData['projectName'] = formValue['projectName'];
    submitData['kanban'] = formValue['kanban'];
    submitData['hierarchy'] = [];

    this.getFieldsResponse.forEach(element => {
      submitData['hierarchy'].push({
        hierarchyLevel: {
          level: element.level,
          hierarchyLevelId: element.hierarchyLevelId,
          hierarchyLevelName: element.hierarchyLevelName
        },
        value: formValue[element.hierarchyLevelId].name ? formValue[element.hierarchyLevelId].name : formValue[element.hierarchyLevelId]
      });
    });

    this.blocked = true;
    this.http.addBasicConfig(submitData).subscribe(response => {
      if (response && response.serviceResponse && response.serviceResponse.success) {
        this.selectedProject = {};
        this.selectedProject['id'] = response.serviceResponse.data['id'];
        this.selectedProject['name'] = response.serviceResponse.data['projectName'];
        this.selectedProject['Type'] = response.serviceResponse.data['kanban'] ? 'Kanban' : 'Scrum';
        response.serviceResponse.data['hierarchy'].forEach(element => {
          this.selectedProject[element.hierarchyLevel.hierarchyLevelName] = element.value;
        });

        this.sharedService.setSelectedProject(this.selectedProject);
        if (!this.ifSuperUser) {
          if (response['projectsAccess']) {
            localStorage.setItem('projectsAccess', JSON.stringify(response['projectsAccess']));
            const authorities = response['projectsAccess'].map(projAcc => projAcc.role);
            localStorage.setItem('authorities', this.aesEncryption.convertText(JSON.stringify(authorities), 'encrypt'));
          }
        }
        this.form.reset();
        this.messenger.add({
          severity: 'success',
          summary: 'Basic config submitted!!',
          detail: ''
        });
      } else {
        this.messenger.add({
          severity: 'error',
          summary: response.serviceResponse.message && response.serviceResponse.message.length ? response.serviceResponse.message : 'Some error occurred. Please try again later.'
        });
      }
      this.blocked = false;
      this.getFields();
    });
  }


  stringValidator(control: AbstractControl): { [key: string]: boolean } | null {
    if ((typeof control.value === 'string' || control.value instanceof String) && control.value !== null && (control.value && (control.value.indexOf('###') !== -1 || control.value.indexOf('~') !== -1 || control.value.indexOf('`') !== -1 || control.value.indexOf('!') !== -1))) {
      return { 'stringValidator': true }
    }
    return null;
  }

  // edit() {
  //   this.submitted = true;
  //   // return if form is invalid
  //   if (this.basicConfForm.invalid) {
  //     return;
  //   }

  //   const submitData = {};
  //   submitData['id'] = this.selectedProject.id;
  //   for (const obj in this.basicConf) {
  //     submitData[obj] = this.basicConf[obj].value;
  //   }
  //   submitData['isKanban'] = this.selectedType;
  //   this.http.editBasicConfig(this.selectedProject.id, submitData).subscribe(response => {
  //     if (response && response.serviceResponse && response.serviceResponse.success) {
  //       this.selectedProject = response.serviceResponse.data;
  //       this.sharedService.setSelectedProject(this.selectedProject);
  //       this.messenger.add({
  //         severity: 'success',
  //         summary: 'Basic config updated!!',
  //         detail: ''
  //       });
  //     } else {
  //       this.messenger.add({
  //         severity: 'error',
  //         summary: 'Some error occurred. Please try again later.'
  //       });
  //     }
  //   });
  // }

}
