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
import { SharedService } from '../../services/shared.service';
import { DomSanitizer } from '@angular/platform-browser';
import { MenuItem } from 'primeng/api';
import { environment } from '../../../environments/environment';
import { GetAuthService } from '../../services/getauth.service';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { first } from 'rxjs/operators';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
declare var $: any;

interface CapacitySubmissionReq {
    projectNodeId: string,
    projectName: string,
    sprintNodeId?: string,
    capacity?: string,
    startDate?: string,
    endDate?: string,
    totalTestCases?: string,
    executedTestCase?: string,
    passedTestCase?: string,
    sprintId?: string,
    sprintName?: string,
    executionDate?: string,
    kanban: boolean;
}

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {

    error = '';
    message = '';
    uploadedFile: File;
    logoImage: any;
    invalid: boolean;
    isUploadFile = true;
    items: MenuItem[];
    selectedView: string;
    kanban: boolean;
    baseUrl = environment.baseUrl;  // Servers Env
    projects: any;
    selectedProject: any;
    dropdownSettingsProject = {};
    filter_kpiRequest = <any>'';
    selectedFilterData = <any>{};
    selectedFilterCount = 0;
    filterData = <any>[];
    masterData = <any>{};
    filterApplyData = <any>{};
    currentSelectionLabel = '';
    filterType = 'Default';
    startDate: any;
    endDate: any;
    executionDate: any;
    reqObj: CapacitySubmissionReq;
    isCapacitySaveDisabled: boolean = true;
    isTestExecutionSaveDisabled: boolean = true;
    capacityErrorMessage: string = '';
    testExecutionErrorMessage: string = '';
    isCheckBoxChecked: boolean;
    todayDate: any;
    loader: boolean = false;
    executionDateGroup: any;
    isSuperAdmin: boolean = false;
    filterForm: UntypedFormGroup;
    projectListArr: Array<object> = [];
    sprintListArr: Array<object> = [];
    selectedFilterArray: Array<any> = [];
    trendLineValueList: any[];
    filteredSprints: any = [];
    sprintDetails: any;
    projectDetails: any;
    selectedProjectBaseConfigId: string;
    popupForm: UntypedFormGroup;

    statusMessage = {
        200: 'Data Saved Successfully!!',
        201: 'Invalid file.',
        202: 'Invalid file type.',
        203: 'Error in saving file on the disk.',
        404: 'Hierarchy Level is  not present in system.',
        500: 'Some Error Occurred. Please try again after sometime.',
        501: 'Emm master is not uploaded please upload it first.'
    };
    cols: any;
    sprintsData: any;
    tabHeaders = ["Scrum", "Kanban"];
    tabContentHeaders = {'upload_tep':'Test Execution Percentage Table', 'upload_Sprint_Capacity':'Capacity Table'};
    selectedHeader: string;
    showPopuup:boolean = false;
    noData:boolean = false;
    capacityScrumData: any;
    capacityKanbanData: any;
    testExecutionScrumData: any;
    testExecutionKanbanData: any;
    selectedSprintDetails: any;
    selectedSprintId: any;
    selectedSprintName: any;
    tableLoader: boolean = true;
    currentDate = new Date();
    constructor(private http_service: HttpService, private messageService: MessageService, private getAuth: GetAuthService, private sharedService: SharedService, private sanitizer: DomSanitizer, private getAuthorisation: GetAuthorizationService) {
    }

    ngOnInit() {
        this.cols = {
            testExecutionScrumKeys: [
                {
                    header: 'Sprint Name',
                    field: "sprintName"
                },
                {
                    header: 'Sprint Status',
                    field: "sprintState"
                },
                {
                    header: 'Total Test Cases',
                    field: "totalTestCases"
                },
                {
                    header: 'Executed Test Cases',
                    field: "executedTestCase"
                },
                {
                    header: 'Passed Test Case',
                    field: "passedTestCase"
                }
            ],
            testExecutionKanbanKeys: [
                {
                    header: 'Execution Date',
                    field: "executionDate"
                },
                {
                    header: 'Total Test Cases',
                    field: "totalTestCases"
                },
                {
                    header: 'Executed Test Cases',
                    field: "executedTestCase"
                },
                {
                    header: 'Passed Test Case',
                    field: "passedTestCase"
                }
            ],
            capacityScrumKeys: [
                {
                    header: 'Sprint Name',
                    field: "sprintName"
                },
                {
                    header: 'Sprint Status',
                    field: "sprintState"
                },
                {
                    header: 'Team Capacity (in Hrs)',
                    field: "capacity"
                }
            ],
            capacityKanbanKeys: [
                {
                    header: 'Start Date',
                    field: "startDate"
                },
                {
                    header: 'End Date',
                    field: "endDate"
                },
                {
                    header: 'Team Capacity (in Hrs)',
                    field: "capacity"
                }
            ]
            };
        this.isSuperAdmin = this.getAuthorisation.checkIfSuperUser();
        this.items = [
            {
                label: 'Test Execution Percentage',
                icon: 'pi pi-pw pi-test-execution',
                command: (event) => {
                    this.switchView(event);
                },
                expanded: !this.isSuperAdmin
            },
            {
                label: 'Capacity',
                icon: 'pi pi-pw pi-capacity',
                command: (event) => {
                    this.switchView(event);
                }
            }
        ];
        this.dropdownSettingsProject = {
            // singleSelection: false,
            text: 'Select Project',
            selectAllText: 'Select All',
            unSelectAllText: 'UnSelect All',
            enableSearchFilter: true,
            classes: 'multi-select-custom-class'
        };

        // this.selectedView = 'emm_upload';
        this.selectedView = 'logo_upload';

        if (this.isSuperAdmin) {
            this.items.unshift(
                {
                    label: 'Upload Logo',
                    icon: 'pi pi-image',
                    command: (event) => {
                        this.switchView(event);
                    },
                    expanded: true
                }
            );
            this.selectedView = 'logo_upload';
        } else {
            this.handleTepSelect('upload_tep');
            document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button')?.classList?.add('btn-active');
            document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button')?.classList?.remove('btn-active');
        }
        this.kanban = false;
        this.getUploadedImage();
        this.startDate = '';
        this.endDate = '';
        this.executionDate = '';
        this.setFormControlValues();
    }
    setFormControlValues() {
        this.filterForm = new UntypedFormGroup({
            selectedProjectValue: new UntypedFormControl()
        });
        if (this.selectedView === 'upload_tep') {
            if (this.kanban) {
                this.popupForm = new UntypedFormGroup({
                    executionDate: new UntypedFormControl(),
                    totalTestCases: new UntypedFormControl(),
                    executedTestCase: new UntypedFormControl(),
                    passedTestCase: new UntypedFormControl()
                });
            } else {
                this.popupForm = new UntypedFormGroup({
                    totalTestCases: new UntypedFormControl(),
                    executedTestCase: new UntypedFormControl(),
                    passedTestCase: new UntypedFormControl()
                });
            }
        } else if (this.selectedView === 'upload_Sprint_Capacity') {
            this.popupForm = new UntypedFormGroup({
                capacity: new UntypedFormControl()
            });
        }
    }
    /* when "test execution percentage" is selected */
    handleTepSelect(tab) {
        this.selectedView = tab;
        this.todayDate = new Date();
        this.executionDate = '';
        this.testExecutionErrorMessage = '';
        this.kanban = false;
        this.isTestExecutionSaveDisabled = true;
        this.loader = true;
        this.setFormControlValues();
        this.selectedProjectBaseConfigId = '';
        this.getFilterDataOnLoad();
    }
    addActiveToTab(){
        document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button')?.classList?.add('btn-active');
        document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button')?.classList?.remove('btn-active');
    }
    switchView(event) {
        // this.highlightSideBarTab(event);
        switch (event.item.label) {
            case 'Upload Logo': {
                this.selectedView = 'logo_upload';
            }
                break;
            case 'Test Execution Percentage': {
                this.handleTepSelect('upload_tep');
                this.addActiveToTab();
            }
                break;
            case 'Capacity': {
                this.selectedView = 'upload_Sprint_Capacity';
                this.addActiveToTab();
                this.startDate = '';
                this.endDate = '';
                this.capacityErrorMessage = '';
                this.kanban = false;
                this.isCapacitySaveDisabled = true;
                this.loader = true;
                this.selectedProjectBaseConfigId = '';
                if (this.selectedView === 'upload_Sprint_Capacity') {
                    this.filterForm = new UntypedFormGroup({
                        selectedProjectValue: new UntypedFormControl()
                    });
                    this.popupForm = new UntypedFormGroup({
                        capacity: new UntypedFormControl()
                    });
                }
                this.getFilterDataOnLoad();
            }
                break;
        }
    }


    /*Rendering the image */
    getUploadedImage() {
        this.http_service.getUploadedImage().pipe(first())
            .subscribe(
                data => {
                    if (data['image']) {
                        this.logoImage = 'data:image/png;base64,' + data['image'];
                        const blob: Blob = new Blob([this.logoImage], { type: 'image/png' });
                        blob['objectURL'] = this.sanitizer.bypassSecurityTrustUrl((window.URL.createObjectURL(blob)));
                        this.uploadedFile = new File([blob], 'logo.png', { type: 'image/png' });
                    }
                });
    }


    /*After selection of file get the byte array and dimension of file*/
    onSelectImage(event) {
        return new Promise((resolve) => {
            let isImageFit = true;
            this.error = undefined;
            /*convert image to  byte array*/
            if (event.target.files[0]) {
                const reader = new FileReader();
                reader.readAsDataURL(event.target.files[0]);
                reader.onload = (evt: any) => { // when file has loaded
                    this.logoImage = reader.result;
                    const img = new Image();
                    img.src = this.logoImage;
                    img.onload = () => {
                        if (img.width > 250 || img.height > 100) {
                            this.error = 'Image is too big(' + img.width + ' x ' + img.height + '). The maximum dimensions are 250 x 100 pixels';
                            isImageFit = false;
                            resolve(isImageFit);
                        }else{
                            resolve(isImageFit);
                        }
                    };
                };
            }
            
        });
    }

    /*check validation of file*/
    validate() {
        this.invalid = false;
        this.message = undefined;
        this.error = undefined;


        /*Validate the size*/
        const imagesize = this.uploadedFile.size / 1024;

        if (imagesize > 100) {
            this.invalid = true;
            this.error = 'File should not be more than 100 KB';
        }

        /*Validate the format*/
        const mimeType = this.uploadedFile.name;
        if (mimeType.match(/\.(jpe?g|png|gif|jpeg|JPEG|JPG|GIF|PNG)$/i) == null) {
            this.error = 'Only JPG, PNG and GIF files are allowed.';
            this.invalid = true;
        }
    }


    /*Upload the file*/
    async onUpload(event) {

        this.uploadedFile = event.target.files[0];

        /*validate the file */
        this.validate();
        if (this.invalid) {
            return;
        }
        /*conversion of image to byte array */
        let isImageFit = await this.onSelectImage(event);
        
        /*call service to upload */
        if(isImageFit){
            this.http_service.uploadImage(this.uploadedFile).pipe(first())
                .subscribe(
                    data => {
                        if (data['status'] && data['status'] === 500) {
                            this.error = data['statusText'];
                        } else {
                            this.message = data['message'];
                            this.sharedService.setLogoImage(this.uploadedFile);
    
                        }
                    });
        }
    }


    /*call service to delete*/
    onDelete() {
        this.isUploadFile = false;
        this.http_service.deleteImage().pipe(first())
            .subscribe(
                data => {
                    this.isUploadFile = true;
                    if (data) {
                        this.message = 'File deleted successfully';
                        this.logoImage = undefined;
                        this.error = undefined;
                        this.sharedService.setLogoImage(undefined);
                    }
                });
    }

    // called when user switches the "Scrum/Kanban" switch
    kanbanActivation(type) {
        let scrumTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button');
        let kanbanTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button');
        if(type === 'scrum') {
            scrumTarget?.classList?.add('btn-active');
            kanbanTarget?.classList?.remove('btn-active');
        } else {
            scrumTarget?.classList?.remove('btn-active');
            kanbanTarget?.classList?.add('btn-active');
        }
        this.kanban = type === 'scrum' ? false: true;
        this.startDate = '';
        this.endDate = '';
        this.executionDate = '';
        this.capacityErrorMessage = '';
        this.testExecutionErrorMessage = '';
        this.isCapacitySaveDisabled = true;
        this.isTestExecutionSaveDisabled = true;
        this.loader = true;
        this.tableLoader = true;
        this.noData = false;
        this.testExecutionKanbanData = [];
        this.testExecutionScrumData = [];
        this.capacityKanbanData = [];
        this.capacityScrumData = [];
        this.projectDetails = {};
        this.selectedProjectBaseConfigId = '';
        this.getFilterDataOnLoad();
    }

    resetProjectSelection(){
        this.projectListArr = [];
        this.trendLineValueList = [];
        this.filterForm?.get('selectedProjectValue').setValue('');

    }

    // gets data for filters on load
    getFilterDataOnLoad() {

        if (this.filter_kpiRequest && this.filter_kpiRequest !== '') {
            this.filter_kpiRequest.unsubscribe();
        }

        this.selectedFilterData = {};
        this.selectedFilterCount = 0;

        this.selectedFilterData.kanban = this.kanban;
        this.selectedFilterData['sprintIncluded'] = ['CLOSED', 'ACTIVE', 'FUTURE'];
        console.log(this.selectedFilterData);
        this.filter_kpiRequest = this.http_service.getFilterData(this.selectedFilterData)
            .subscribe(filterData => {
                if (filterData[0] !== 'error') {
                    this.filterData = filterData['data'];
                    if(this.filterData && this.filterData.length > 0){
                        this.projectListArr = this.sortAlphabetically(this.filterData.filter(x => x.labelName.toLowerCase() == 'project'));
                        this.projectListArr = this.makeUniqueArrayList(this.projectListArr);
                        let defaultSelection = this.selectedProjectBaseConfigId ? false : true;
                        this.checkDefaultFilterSelection(defaultSelection);
                        if (Object.keys(filterData).length !== 0) {
                            // this.getMasterData();
                        } else {
                            this.resetProjectSelection();
                            // show error message
                            this.messageService.add({ severity: 'error', summary: 'Projects not found.' });
                        }
                    } else {
                        this.resetProjectSelection();
                    }


                } else {
                    this.resetProjectSelection();
                    // show error message
                    this.messageService.add({ severity: 'error', summary: 'Error in fetching filter data. Please try after some time.' });
                }
                this.loader = false;
            });
    }

    // this function is called when checkbox or canceled is clicked  in filter
    filterSelectedData(e, data, currentSelection, hitApply, currentSelectionLabel, dataArray?, isFilterSelectDeselectInDropDown?) {

        // Remove previous checked filters
        this.filterData.forEach(filter => {
            if (!!filter.filterData && filter.level >= currentSelection && filter.filterData.length == 1) {
                filter.filterData[0].isSelected = false;
            }
        });

        // Remove previous selected elements
        dataArray.forEach(obj => {
            if (obj.nodeId !== data.nodeId) {
                obj.isSelected = false;
            }
        });

        this.currentSelectionLabel = currentSelectionLabel;
        this.selectedFilterData['currentSelection'] = currentSelection;
        this.selectedFilterData['currentSelectionLabel'] = currentSelectionLabel;
        if (typeof (isFilterSelectDeselectInDropDown) !== 'undefined') {
            this.isCheckBoxChecked = isFilterSelectDeselectInDropDown;
        } else {
            this.isCheckBoxChecked = (!!e && !!e.target && !!e.target.checked) ? true : false;
        }
        this.selectedFilterData.filterDataList = <any>[];
        this.selectedFilterData.filterDataList = JSON.parse(JSON.stringify(this.filterData));


        for (let i = 0; i < this.selectedFilterData.filterDataList.length; i++) {
            if (this.selectedFilterData.filterDataList[i].level === currentSelection) {
                for (let j = 0; j < this.selectedFilterData.filterDataList[i].filterData.length; j++) {
                    if (JSON.stringify(this.selectedFilterData.filterDataList[i].filterData[j]) === JSON.stringify(data)) {
                        if (!(e.target !== undefined && e.target.checked)) {
                            this.filterData[i].filterData[j].isSelected = false;
                            this.selectedFilterData.filterDataList[i].filterData[j].isSelected = false;
                        }
                    }

                }
            } else {

                for (let j = 0; j < this.selectedFilterData.filterDataList[i].filterData.length; j++) {
                    const obj = this.selectedFilterData.filterDataList[i].filterData[j];
                    if (obj.isSelected === false || this.selectedFilterData.filterDataList[i].level > currentSelection) {

                        this.selectedFilterData.filterDataList[i].filterData.splice(j, 1);
                        j = -1;
                    }
                }
            }
        }

        this.selectedFilterData.kanban = this.kanban;
        if (this.selectedFilterData) {
            this.http_service.getFilterData(this.selectedFilterData)
                .subscribe(filterData => {
                    this.renderSpecificFilters(filterData, hitApply);
                });
        }
        this.enableDisableSubmitButton();

    }

    renderSpecificFilters(filterData, hitApply) {
        filterData.forEach(filter => {
            if (!!filter.filterData && filter.filterData.length == 1 &&
                (filter.level < this.selectedFilterData['currentSelection'])) {
                filter.filterData[0].isSelected = true;
            }
        });
        this.filterData = filterData;
        this.selectedFilterData.filterDataList = this.filterData;
        this.enableDisableSubmitButton();
        this.checkdisabled();
        if (hitApply && this.selectedFilterCount !== 0) {
            // this.applyChanges(false);
        } else if (hitApply && this.selectedFilterCount === 0) {
            this.filterApplyData = {};
        }
    }

    checkdisabled() {
        this.selectedFilterCount = 0;
        for (const index in this.filterData) {
            for (const filterObjectIndex in this.filterData[index].filterData) {
                if (this.filterData[index].filterData[filterObjectIndex].isSelected) {
                    this.selectedFilterCount++;
                }
            }
        }
    }

    // called on the click of the Submit button when creating capacity per sprint(hrs)
    submitCapacity() {
        this.reqObj['capacity'] = this.popupForm?.get('capacity').value;
        this.http_service.saveCapacity(this.reqObj)
            .subscribe(response => {
                if (response.success) {
                    this.selectedFilterData = {};
                    this.startDate = '';
                    this.endDate = '';
                    this.capacityErrorMessage = '';
                    this.isCapacitySaveDisabled = true;
                    this.setFormValuesEmpty();
                    this.messageService.add({ severity: 'success', summary: 'Capacity saved.', detail: '' });
                    this.getFilterDataOnLoad();
                } else if (!response.success && !!response.message && response.message === 'Unauthorized') {
                    this.messageService.add({ severity: 'error', summary: 'You are not authorized.' });
                } else {
                    this.messageService.add({ severity: 'error', summary: 'Error in saving scenario. Please try after some time.' });
                }
                this.showPopuup = false;
                this.isCapacitySaveDisabled = true;
                this.capacityErrorMessage = '';
            });
    }

    setFormValuesEmpty() {
        if (this.filterForm && this.filterForm.controls) {
            Object.keys(this.filterForm?.controls).forEach(key => {
                if (this.filterForm.get(key) && key !== 'selectedProjectValue') {
                    this.filterForm?.get(key)?.setValue('');
                }
            });
        }
        if (this.popupForm && this.popupForm.controls) {
            Object.keys(this.popupForm?.controls).forEach(key => {
                if (this.popupForm.get(key)) {
                    this.popupForm?.get(key)?.setValue('');
                }
            });
        }
        if (this.reqObj) {
            for (let capReqField in this.reqObj) {
                this.reqObj[capReqField] = '';
            }
        }
    }
    AddOrUpdateData(data) {
        this.showPopuup = true;
        this.executionDate = data?.executionDate ?  data?.executionDate : '';
        this.selectedSprintName = data?.sprintName;
        this.selectedSprintId = this.selectedView === 'upload_tep' ? data?.sprintId : data?.sprintNodeId;
        this.startDate = data?.startDate;
        this.endDate = data?.endDate;
        this.reqObj = {
            projectNodeId: data?.projectNodeId,
            projectName: data?.projectName,
            kanban: this.kanban
        }
        if (!this.kanban) {
            if(this.selectedView === 'upload_tep') {
                this.reqObj['sprintId'] = this.selectedSprintId;
            } else {
                this.reqObj['sprintNodeId'] = this.selectedSprintId;
            }
        } else {
            this.selectedView === 'upload_tep' ? this.reqObj['executionDate'] = this.executionDate : '';
        }
        if (this.selectedView === 'upload_tep') {
            this.popupForm = new UntypedFormGroup({
                totalTestCases: new UntypedFormControl(data?.totalTestCases ? data?.totalTestCases : ''),
                executedTestCase: new UntypedFormControl(data?.executedTestCase ? data?.executedTestCase : ''),
                passedTestCase: new UntypedFormControl(data?.passedTestCase ? data?.passedTestCase : '')
            });

            this.reqObj['totalTestCases'] = data?.totalTestCases;
            this.reqObj['executedTestCase'] = data?.executedTestCase;
            this.reqObj['passedTestCase'] = data?.passedTestCase;

        } else if (this.selectedView === 'upload_Sprint_Capacity') {
            this.popupForm = new UntypedFormGroup({
                capacity: new UntypedFormControl(data?.capacity ? data?.capacity : '')
            });
            this.reqObj["capacity"] = data?.capacity ? data?.capacity : '';;
            if(this.kanban) {
                this.reqObj["startDate"] = data?.startDate;
                this.reqObj["endDate"] = data?.endDate;
            }
        }
        this.enableDisableSubmitButton();
    }
    submitTestExecution() {
        this.reqObj['totalTestCases'] = this.popupForm?.get('totalTestCases').value;
        this.reqObj['executedTestCase'] = this.popupForm?.get('executedTestCase').value;
        this.reqObj['passedTestCase'] = this.popupForm?.get('passedTestCase').value;
        this.http_service.saveTestExecutionPercent(this.reqObj)
            .subscribe(response => {
                if (response.success) {
                    this.selectedFilterData = {};
                    this.setFormValuesEmpty();
                    this.testExecutionErrorMessage = '';
                    this.isTestExecutionSaveDisabled = true;

                    this.messageService.add({ severity: 'success', summary: 'Test Execution Percentage saved.', detail: '' });
                    this.getFilterDataOnLoad();
                } else if (!response.success && !!response.message && response.message === 'Unauthorized') {
                    this.messageService.add({ severity: 'error', summary: 'You are not authorized.' });
                } else {
                    this.messageService.add({ severity: 'error', summary: 'Error in saving test execution percentage. Please try after some time.' });
                }
                this.showPopuup = false;
                this.isTestExecutionSaveDisabled = true;
                this.testExecutionErrorMessage = '';
            });
    }

    enableDisableCapacitySubmitButton() {
        if (this.popupForm.get('capacity')?.value && this.popupForm.get('capacity')?.value === 'Enter Value') {
            this.isCapacitySaveDisabled = true;
            this.capacityErrorMessage = 'Please enter Capacity';
            return;
        }
        if (!(!!this.popupForm.get('capacity')?.value)) {
            this.isCapacitySaveDisabled = true;
            if (parseInt(this.popupForm.get('capacity')?.value) === 0) {
                this.capacityErrorMessage = 'Capacity Should not be 0';
            } else {
                this.capacityErrorMessage = 'Please enter Capacity';
            }
            return;
        }
        this.isCapacitySaveDisabled = false;
        this.capacityErrorMessage = '';

    }
    enableDisableTestExecutionSubmitButton() {
        if (!(!!this.popupForm?.get('totalTestCases').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('totalTestCases').value) === 0) {
                this.testExecutionErrorMessage = 'Total Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage = 'Please enter total test cases, executed test cases and passed test cases';
            }
            return;

        }
        if (!(!!this.popupForm?.get('executedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('executedTestCase').value) === 0) {
                this.testExecutionErrorMessage = 'Executed Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage = 'Please enter total test cases, executed test cases and passed test cases';
            }
            return;
        }
        if (!(!!this.popupForm?.get('passedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('passedTestCase').value) === 0) {
                this.testExecutionErrorMessage = 'Passed Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage ='Please enter total test cases, executed test cases and passed test cases';
            }
            return;
        }
        if (parseFloat(this.popupForm?.get('totalTestCases').value) < parseFloat(this.popupForm?.get('executedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Executed Test Cases should not be greater than Total Test Cases';
            return;
        }
        if (parseFloat(this.popupForm?.get('executedTestCase').value) < parseFloat(this.popupForm?.get('passedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Passed Test Cases should not be greater than Executed Test Cases';
            return;
        }
        this.isTestExecutionSaveDisabled = false;
        this.testExecutionErrorMessage = '';
    }


    enterNumericValue(event) {
        if (!!event && !!event.preventDefault && event.key === '.' || event.key === 'e' || event.key === '-' || event.key === '+') {
            event.preventDefault();
            return;
        }
        this.enableDisableSubmitButton();
    }

    numericInputUpDown(event: any) {
        if (parseInt(event.target.value) < 0) {
            setTimeout(() => {
                this[event.target.name] = '';
                event.target.value = '';
                this.enableDisableSubmitButton();
            }, 0);
        } else {
            this.enableDisableSubmitButton();
        }
    }

    enableDisableSubmitButton() {
        if (this.selectedView === 'upload_Sprint_Capacity') {
            this.enableDisableCapacitySubmitButton();
        } else if (this.selectedView === 'upload_tep') {
            this.enableDisableTestExecutionSubmitButton();
        }

    }

    // called when user switches between Default and Additional filters
    selectFilterType(type, event) {
        this.filterType = type;
        $('.ui-menuitem-link.ui-corner-all').removeClass('selected');
        $(event.originalEvent.target).closest('a').addClass('selected');
    }
    checkDefaultFilterSelection(flag) {
        if(flag) {
            this.trendLineValueList = [...this.projectListArr];
            this.filterForm?.get('selectedProjectValue').setValue(this.trendLineValueList?.[0]['nodeId']);
            this.handleIterationFilters('project');
        } else {
            this.getProjectBasedData();
        }
    }
    getSprintsBasedOnState(state, sprints) {
        return sprints?.filter(x => x['sprintState']?.toLowerCase() === state);
    }
    getFirstOrLatestSprint(sprints, type) {
        if(type === 'latest') {
            return sprints?.reduce((a, b) => {
                return new Date(a.sprintStartDate) > new Date(b.sprintStartDate) ? a : b;
            });
        } else {
            return sprints?.reduce((a, b) => {
                return new Date(a.sprintStartDate) < new Date(b.sprintStartDate) ? a : b;
            });
        };
    }
    handleIterationFilters(level) {
        if (this.filterForm?.get('selectedProjectValue')?.value != '') {
            this.tableLoader = true;
            this.noData = false;
            this.selectedSprintDetails = {};
            this.testExecutionScrumData = [];
            this.testExecutionKanbanData = [];
            this.capacityScrumData = [];
            this.capacityKanbanData = [];
            if (level?.toLowerCase() == 'project') {
                let selectedProject = this.filterForm?.get('selectedProjectValue')?.value;
                this.projectDetails = { ...this.trendLineValueList.find(i => i.nodeId === selectedProject) };
                this.selectedProjectBaseConfigId = this.projectDetails?.basicProjectConfigId;
                this.getProjectBasedData();
            }
        }
    }
    getProjectBasedData(){
        if(this.selectedProjectBaseConfigId) {
            this.selectedView === 'upload_Sprint_Capacity' ? this.getCapacityData(this.selectedProjectBaseConfigId) : this.getTestExecutionData(this.selectedProjectBaseConfigId);
        }
    }
    sortAlphabetically(objArray) {
        objArray?.sort((a, b) => a.nodeName.localeCompare(b.nodeName));
        return objArray;
    }
    makeUniqueArrayList(arr) {
        let uniqueArray = [];
        for (let i = 0; i < arr?.length; i++) {
            let idx = uniqueArray?.findIndex(x => x.nodeId == arr[i]?.nodeId);
            if (idx == -1) {
                uniqueArray = [...uniqueArray, arr[i]];
                uniqueArray[uniqueArray?.length - 1]['path'] = [uniqueArray[uniqueArray?.length - 1]['path']];
                uniqueArray[uniqueArray?.length - 1]['parentId'] = [uniqueArray[uniqueArray?.length - 1]['parentId']];
            } else {
                uniqueArray[idx].path = [...uniqueArray[idx]?.path, arr[i]?.path];
                uniqueArray[idx].parentId = [...uniqueArray[idx]?.parentId, arr[i]?.parentId];
            }

        }
        return uniqueArray;
    }
    getCapacityData(projectId){
        this.http_service.getCapacityData(projectId).subscribe((response)=> {
            if(response && response?.success && response?.data) {
                if (this.kanban) {
                    this.capacityKanbanData = response?.data;
                    if (this.capacityKanbanData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                } else {
                    this.capacityScrumData = response?.data;
                    if (this.capacityScrumData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                }
                this.tableLoader = false;
            } else {
                this.tableLoader = false;
                this.noData = true;
            }
        });
    }
    getTestExecutionData(projectId){
        this.http_service.getTestExecutionData(projectId).subscribe((response)=> {
            if(response && response?.success && response?.data) {
                if (this.kanban) {
                    this.testExecutionKanbanData = response?.data;
                    if (this.testExecutionKanbanData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                } else {
                    this.testExecutionScrumData = response?.data;
                    if (this.testExecutionScrumData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                }
                this.tableLoader = false;
            } else {
                this.tableLoader = false;
                this.noData = true;
            }
        });
    }
}
