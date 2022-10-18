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

/*************
Kpi maturity Dashboard
This is used to show maturity of kpi in form of d3.chart (sunburst chart).
@author anuj
**************/

import { Component, OnInit, OnDestroy } from '@angular/core';
import * as d3v3 from 'd3-3';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { HelperService } from '../../services/helper.service';
import { Router } from '@angular/router';


@Component({
    selector: 'app-maturity',
    templateUrl: './maturity.component.html',
    styleUrls: ['./maturity.component.css']
})
export class MaturityComponent implements OnInit, OnDestroy {

    masterData = <any>{};
    filterData = <any>[];
    sonarKpiData = <any>{};
    jenkinsKpiData = <any>{};
    zypherKpiData = <any>{};
    jiraKpiData = <any>{};
    bitBucketKpiData = <any>{};
    selectedFilterCount = 0;
    filterRequestData = {};
    filterkeys = <any>[];
    filterApplyData = <any>{};
    kpiListSonar = <any>{};
    kpiJenkins = <any>{};
    kpiZypher = <any>{};
    kpiJira = <any>{};
    kpiBitBucket = <any>{};
    checkinsPerDay = <any>[];
    gaugemap = {};
    defectCount = <any>[];
    loaderJenkins = false;
    loaderJira = false;
    loaderSonar = false;
    loaderZypher = false;
    loaderBitBucket = false;
    loaderMaturity = false;
    maturityValue = <any>{};
    subscription = <any>{};
    jiraKpiRequest = <any>'';
    sonarKpiRequest = <any>'';
    zypherKpiRequest = <any>'';
    jenkinsKpiRequest = <any>'';
    bitBucketKpiRequest = <any>'';
    selectedtype;

    constructor(private service: SharedService, private httpService: HttpService, private helperService: HelperService, private router: Router) {

        this.subscription = this.service.passDataToDashboard.subscribe((sharedobject) => {
            this.receiveSharedData(sharedobject);
        });

        this.service.onTypeRefresh.subscribe((sharedobject) => {
            this.loaderJira = false;
            this.loaderSonar = false;
            this.loaderZypher = false;
            this.loaderBitBucket = false;
            this.loaderJenkins = false;
            this.selectedtype = this.service.getSelectedType();

            if (this.selectedtype === 'Scrum' && this.router.url === '/dashboard/Maturity') {
                if (this.service.getFilterObject()) {
                    this.receiveSharedData(this.service.getFilterObject());
                }
            }
        });

    }

    ngOnDestroy() {
    }
    receiveSharedData($event) {
        if(this.service.getSelectedTab() === 'Maturity'){
            this.masterData = $event.masterData;
            this.filterData = $event.filterData;
            this.filterApplyData = $event.filterApplyData;
            this.loaderMaturity = true;
            let kpiIdsForCurrentBoard = this.service.getMasterData()['kpiList'].filter(kpi => kpi.calculateMaturity).map(kpi => kpi.kpiId);
            this.drawAreaChart(null, null);
            this.groupJenkinsKpi(kpiIdsForCurrentBoard);
            this.groupZypherKpi(kpiIdsForCurrentBoard);
            this.groupBitBucketKpi(kpiIdsForCurrentBoard);
            this.groupSonarKpi(kpiIdsForCurrentBoard);
            this.groupJiraKpi(kpiIdsForCurrentBoard);
        }
    }

    ngOnInit() {
        this.service.selectTab('Maturity');

        this.selectedtype = this.service.getSelectedType();

        if (this.selectedtype === 'Scrum') {
            if (this.service.getFilterObject()) {
                this.receiveSharedData(this.service.getFilterObject());
            }
        }
    }



    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupSonarKpi(kpiIdsForCurrentBoard) {
        this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        this.postSonarKpi(this.kpiListSonar, 'sonar');
    }

    // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
    groupJenkinsKpi(kpiIdsForCurrentBoard) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupZypherKpi(kpiIdsForCurrentBoard) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        this.postZypherKpi(this.kpiZypher, 'zypher');
    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
    groupJiraKpi(kpiIdsForCurrentBoard) {

        this.jiraKpiData = {};
        // creating a set of unique group Ids
        const groupIdSet = new Set();
        this.masterData.kpiList.forEach((obj) => {
            if (!obj.kanban && obj.kpiSource === 'Jira') {
                groupIdSet.add(obj.groupId);
            }
        });

        // sending requests after grouping the the KPIs according to group Id
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
                if (this.kpiJira?.kpiList?.length > 0) {
                this.postJiraKpi(this.kpiJira, 'jira');
                }
            }
        });



    }

    // Used for grouping all BitBucket kpi of scrum from master data and calling BitBucket kpi.
    groupBitBucketKpi(kpiIdsForCurrentBoard) {
        this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
    }


    postSonarKpi(postData, source): void {
        this.loaderSonar = true;
        if (this.sonarKpiRequest && this.sonarKpiRequest !== '') {
            this.sonarKpiRequest.unsubscribe();
        }
        this.sonarKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.loaderSonar = false;
                this.loaderMaturity = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    this.sonarKpiData = getData;
                    const newObject = {};
                    for (const obj in this.sonarKpiData) {
                        newObject[this.sonarKpiData[obj].kpiId] = this.sonarKpiData[obj];
                        this.maturityValue[this.sonarKpiData[obj].kpiId] = this.sonarKpiData[obj];

                    }
                    this.sonarKpiData = newObject;

                    // if (this.sonarKpiData && this.sonarKpiData.kpi41 && this.sonarKpiData.kpi39) {
                    //     this.drawAreaChart(this.sonarKpiData.kpi41.value, this.sonarKpiData.kpi39.value);
                    // } else {
                    //     this.drawAreaChart(null, null);
                    // }

                }
            });

    }
    postJenkinsKpi(postData, source): void {
        this.loaderJenkins = true;
        if (this.jenkinsKpiRequest && this.jenkinsKpiRequest !== '') {
            this.jenkinsKpiRequest.unsubscribe();
        }
        this.jenkinsKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.loaderJenkins = false;
                if (!(getData !== null && getData[0] === 'error')) {

                    this.jenkinsKpiData = getData;
                    const newObject = {};
                    for (const obj in this.jenkinsKpiData) {
                        newObject[this.jenkinsKpiData[obj].kpiId] = this.jenkinsKpiData[obj];
                        this.maturityValue[this.jenkinsKpiData[obj].kpiId] = this.jenkinsKpiData[obj];
                    }
                    this.loaderMaturity = false;
                    this.jenkinsKpiData =newObject
                    // if (this.jenkinsKpiData && this.jenkinsKpiData.kpi41 && this.jenkinsKpiData.kpi39) {
                    //     this.drawAreaChart(this.jenkinsKpiData.kpi41.value, this.jenkinsKpiData.kpi39.value);
                    // } else {
                    //     this.drawAreaChart(null, null);
                    // }

                }
            });
    }
    postZypherKpi(postData, source): void {
        this.loaderZypher = true;
        if (this.zypherKpiRequest && this.zypherKpiRequest !== '') {
            this.zypherKpiRequest.unsubscribe();
        }
        this.zypherKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.loaderZypher = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    this.zypherKpiData = getData;
                    const newObject = {};
                    for (const obj in this.zypherKpiData) {
                        newObject[this.zypherKpiData[obj].kpiId] = this.zypherKpiData[obj];
                        this.maturityValue[this.zypherKpiData[obj].kpiId] = this.zypherKpiData[obj];

                    }
                    this.loaderMaturity = false;
                    /*if (this.zypherKpiData && this.zypherKpiData.kpi16 && this.zypherKpiData.kpi42) {
                        this.drawAreaChart(this.zypherKpiData.kpi16.value, this.zypherKpiData.kpi42.value);
                    } else {
                        this.drawAreaChart(null, null);
                    }*/
                    this.zypherKpiData = newObject;
                }
            });
    }
    postJiraKpi(postData, source): void {
        // this.loaderJira = true;
        // if (this.jiraKpiRequest && this.jiraKpiRequest !== '') {
        //     this.jiraKpiRequest.unsubscribe();
        // }
        this.jiraKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                // this.loaderJira = false;
                this.loaderMaturity = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    // this.jiraKpiData = getData;
                    this.jiraKpiData = { ...this.jiraKpiData, ...getData };
                    const newObject = {};
                    for (const obj in this.jiraKpiData) {
                        newObject[this.jiraKpiData[obj].kpiId] = this.jiraKpiData[obj];
                        this.maturityValue[this.jiraKpiData[obj].kpiId] = this.jiraKpiData[obj];
                    }
                    this.jiraKpiData = newObject;
                    if (this.jiraKpiData && this.jiraKpiData.kpi41 && this.jiraKpiData.kpi39) {
                        this.drawAreaChart(this.jiraKpiData.kpi41.value, this.jiraKpiData.kpi39.value);
                    } else {
                        this.drawAreaChart(null, null);
                    }
                }
            });
    }

    postBitBucketKpi(postData, source): void {
        this.loaderBitBucket = true;
        if (this.bitBucketKpiRequest && this.bitBucketKpiRequest !== '') {

            this.bitBucketKpiRequest.unsubscribe();
        }
        this.bitBucketKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.loaderBitBucket = false;
                this.loaderMaturity = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    this.bitBucketKpiData = getData;
                    const newObject = {};
                    for (const obj in this.bitBucketKpiData) {
                        newObject[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                        this.maturityValue[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                    }
                    this.bitBucketKpiData = newObject;

                    // if (this.bitBucketKpiData && this.bitBucketKpiData.kpi41 && this.bitBucketKpiData.kpi39) {
                    //     this.drawAreaChart(this.bitBucketKpiData.kpi41.value, this.bitBucketKpiData.kpi39.value);
                    // } else {
                    //     this.drawAreaChart(null, null);
                    // }

                }
            });
    }

    drawAreaChart(totalDefectCount, sprintVelocity) {
        d3v3.select('svg').remove();
        d3v3.select('.tooltip').remove();


        const startRotation = this.loaderMaturity;
        let root;
        if (startRotation) {

            root = {
                'textLines': ['..'],
                'groups': [{
                    'textLines': ['..'],
                    'bars': [{
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }]
                }, {
                    'textLines': ['..'],
                    'bars': [{
                        'textLines': ['..'],
                        'value': 0
                    }]
                }, {
                    'textLines': ['..'],
                    'bars': [{
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }, {
                        'textLines': ['..'],
                        'value': 0
                    }]
                }]
            };
        } else {
            // on loading show data;

            root = {
                'textLines': ['KPI', 'Maturity Wheel'],
                'groups': [{
                    'textLines': [''],
                    'bars': [{
                        'textLines': ['Mean Time', 'To Merge'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi84) || undefinedCheck(this.maturityValue.kpi84.trendValueList) || undefinedCheck(this.maturityValue.kpi84.trendValueList[0]) ? -1 : this.maturityValue.kpi84.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi84) ? 'undefined' : this.maturityValue.kpi84.maturityRange
                    },
                    {
                      "textLines": ["Number of", "Check Ins  Merge"," Requests"],
                      "value": getMaturityValue(undefinedCheck(this.maturityValue.kpi11) || undefinedCheck(this.maturityValue.kpi11.trendValueList) || undefinedCheck(this.maturityValue.kpi11.trendValueList[0]) || undefinedCheck(this.maturityValue.kpi11.trendValueList[0].value) ? -1 : this.maturityValue.kpi11.trendValueList[0].value[0].maturity),
                      "maturityRange": undefinedCheck(this.maturityValue.kpi11) ? 'undefined' : this.maturityValue.kpi11.maturityRange
                     },
                    {
                        'textLines': ['Code Build', 'Time'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi8) || undefinedCheck(this.maturityValue.kpi8.trendValueList) || undefinedCheck(this.maturityValue.kpi8.trendValueList[0]) ? -1 : this.maturityValue.kpi8.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi8) ? 'undefined' : this.maturityValue.kpi8.maturityRange
                    }, {
                        'textLines': ['Sonar', 'Tech Debt'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi27) || undefinedCheck(this.maturityValue.kpi27.trendValueList) || undefinedCheck(this.maturityValue.kpi27.trendValueList[0]) || getMaturityValue(this.maturityValue.kpi27.trendValueList[0].value[0]) ? -1 : this.maturityValue.kpi27.trendValueList[0].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi27) ? 'undefined' : this.maturityValue.kpi27.maturityRange
                    }, {
                        'textLines': ['First Time', 'Pass Rate'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi82) || undefinedCheck(this.maturityValue.kpi82.trendValueList)  || undefinedCheck(this.maturityValue.kpi82.trendValueList[0]) ? -1 : this.maturityValue.kpi82.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi82) ? 'undefined' : this.maturityValue.kpi82.maturityRange
                    }, {
                        'textLines': ['Intake', 'to', 'DOR'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[1]) ? -1 : this.maturityValue.kpi3.trendValueList[1].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(5, 11)
                    }, {
                        'textLines': ['DoR', 'to', 'DoD'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[2]) ? -1 : this.maturityValue.kpi3.trendValueList[2].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(10, 15)
                    }, {
                        'textLines': ['DoD', 'to', 'Live'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[3]) ? -1 : this.maturityValue.kpi3.trendValueList[3].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(15, 20)
                    },
                    {
                        'textLines': ['Average', 'Resolution', 'Time'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi83) || undefinedCheck(this.maturityValue.kpi83.trendValueList) || undefinedCheck(this.maturityValue.kpi83.trendValueList[0]) || getMaturityValue(this.maturityValue.kpi83.trendValueList[0].value[0]) ? -1 : this.maturityValue.kpi83.trendValueList[0].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi83) ? 'undefined' : this.maturityValue.kpi83.maturityRange
                    }]
                }, {
                    'textLines': [''],
                    'bars': [{
                        'textLines': ['Defect', 'Injection', 'Rate'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi14) || undefinedCheck(this.maturityValue.kpi14.trendValueList) || undefinedCheck(this.maturityValue.kpi14.trendValueList[0]) ? -1 : this.maturityValue.kpi14.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi14) ? 'undefined' : this.maturityValue.kpi14.maturityRange
                    }, {
                        'textLines': ['Defect', 'Seepage', 'Rate'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi35) || undefinedCheck(this.maturityValue.kpi35.trendValueList) || undefinedCheck(this.maturityValue.kpi35.trendValueList[0]) ? -1 : this.maturityValue.kpi35.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi35) ? 'undefined' : this.maturityValue.kpi35.maturityRange
                    }, {
                        'textLines': ['Defect', 'Removal', 'Efficiency'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi34) || undefinedCheck(this.maturityValue.kpi34.trendValueList) || undefinedCheck(this.maturityValue.kpi34.trendValueList[0]) ? -1 : this.maturityValue.kpi34.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi34) ? 'undefined' : this.maturityValue.kpi34.maturityRange
                    }, {
                        'textLines': ['Defect', 'Rejection', 'Rate'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi37) || undefinedCheck(this.maturityValue.kpi37.trendValueList) || undefinedCheck(this.maturityValue.kpi37.trendValueList[0]) ? -1 : this.maturityValue.kpi37.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi37) ? 'undefined' : this.maturityValue.kpi37.maturityRange
                    }, {
                      'textLines': ['Test Execution', 'and', 'pass percentage'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi70) || undefinedCheck(this.maturityValue.kpi70.trendValueList) || undefinedCheck(this.maturityValue.kpi70.trendValueList[0]) ? -1 : this.maturityValue.kpi70.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi70) ? 'undefined' : this.maturityValue.kpi70.maturityRange
                    }, {
                        'textLines': ['Unit', 'Test', 'Coverage'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi17) || undefinedCheck(this.maturityValue.kpi17.trendValueList) || undefinedCheck(this.maturityValue.kpi17.trendValueList[0]) || getMaturityValue(this.maturityValue.kpi17.trendValueList[0].value[0]) ? -1 : this.maturityValue.kpi17.trendValueList[0].value[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi17) ? 'undefined' : this.maturityValue.kpi17.maturityRange
                    }, {
                        'textLines': ['Regression', 'Automation', 'Coverage'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi42) || undefinedCheck(this.maturityValue.kpi42.trendValueList) || undefinedCheck(this.maturityValue.kpi42.trendValueList[0]) ? -1 : this.maturityValue.kpi42.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi42) ? 'undefined' : this.maturityValue.kpi42.maturityRange
                    }, {
                        'textLines': ['In-sprint', 'Automation', 'Coverage'],
                      'value': getMaturityValue(undefinedCheck(this.maturityValue.kpi16) || undefinedCheck(this.maturityValue.kpi16.trendValueList) || undefinedCheck(this.maturityValue.kpi16.trendValueList[0]) ? -1 : this.maturityValue.kpi16.trendValueList[0].maturity),
                      'maturityRange': undefinedCheck(this.maturityValue.kpi16) ? 'undefined' : this.maturityValue.kpi16.maturityRange
                    }]
                }]
            };


      }

      function undefinedCheck(attr) {
        if (attr === undefined || attr === 'undefined') {
          return true;
        }
        return false;
      }

        function getMaturityValue(mv) {
            if (mv === undefined) {
                return 0;
            } else if (mv === -1) {
                return 0;
            } else {
                return mv;
            }
        }

        function getTotalCountMaturityValue() {
            if (sprintVelocity && totalDefectCount) {
                let defectCountFinal = 0;
                if (Array.isArray(totalDefectCount)) {
                    totalDefectCount.forEach((elem) => {
                        if (elem && elem.noOfDefect) {
                            defectCountFinal += elem.noOfDefect;
                        }
                    });
                }
                totalDefectCount = defectCountFinal;
                const totalDefectCal = totalDefectCount / sprintVelocity;
                if (totalDefectCal <= 5) {
                    return 5;
                } else if (totalDefectCal > 5 && totalDefectCal <= 15) {
                    return 4;
                } else if (totalDefectCal > 15 && totalDefectCal <= 30) {
                    return 3;
                } else if (totalDefectCal > 30 && totalDefectCal < 50) {
                    return 2;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }

        }

        d3v3.select('.chart123').datum(root).call(sunburstBarChart());


        const div = d3v3.select('.chart123').append('div')
            .attr('class', 'tooltip')
            .style('opacity', 1)
            .style('display', 'inline-block');

        function sunburstBarChart() {
            const edge = 520,
                maxBarValue = 5,
                rotation = -95 * Math.PI / 180;

            const radius = edge / 2,
                effectiveEdge = edge * 1.2,
                scale = d3v3.scale.linear().domain([0, maxBarValue + 2]);

            const partition = d3v3.layout.partition()
                .sort(null)
                .size([2 * Math.PI, radius * radius])
                .value(function (d) { return 1; });

            const arc = d3v3.svg.arc()
                .startAngle(function (d) { return d.x + rotation; })
                .endAngle(function (d) { return d.x + d.dx + rotation; })
                .innerRadius(function (d) {
                    if (d.depth === 0) {
                        d.yi = Math.sqrt(d.y);
                    } else {
                        d.yi = scale.range([Math.sqrt(d.dy), edge / 2.15])(d.depth);
                    }
                    return d.yi;
                })
                .outerRadius(function (d) {
                    if (d.depth === 0) {
                        d.yo = Math.sqrt(d.y + d.dy);
                    } else if (d.depth === maxBarValue + 1) {
                        d.yo = edge / 2;
                    } else {
                        d.yo = scale.range([Math.sqrt(d.y + d.dy), edge / 2.15])(d.depth);
                    }
                    return d.yo;
                });

            const labelArc = d3v3.svg.arc()
                .startAngle(function (d) { return d.x + rotation; })
                .endAngle(function (d) { return d.x + d.dx + rotation; })
                .innerRadius(function (d, i) {
                    return d3v3.scale.linear().domain([-1, d.textLines.length]).range([d.yi, d.yo])(i);
                })
                .outerRadius(function (d, i) {
                    return d3v3.scale.linear().domain([-1, d.textLines.length]).range([d.yi, d.yo])(i);
                });

            const oArc = d3v3.svg.arc()
                .startAngle(function (d) {
                    return d.values[0].x + rotation;
                })
                .endAngle(function (d) {
                    return d.values[d.values.length - 1].x + d.values[d.values.length - 1].dx + rotation;
                })
                .innerRadius(function (d) { d.yi = edge / 2; return d.yi; })
                .outerRadius(function (d) { d.yo = effectiveEdge * 0.96 / 2; return d.yo; });

            const outerLabelArc = d3v3.svg.arc()
                .startAngle(function (d) {
                    return d.values[0].x + rotation;
                })
                .endAngle(function (d) {
                    return d.values[d.values.length - 1].x + d.values[d.values.length - 1].dx + rotation;
                })
                .innerRadius(function (d, i) {
                    return d3v3.scale.linear().domain([0, d.values[0].textLines.length - 1]).range([d.yi * 1.05, d.yo * 0.9])(i);
                })
                .outerRadius(function (d, i) {
                    // (edge / 2) + ((effectiveEdge - edge) * 0.12)
                    return d3v3.scale.linear().domain([0, d.values[0].textLines.length - 1]).range([d.yi * 1.05, d.yo * 0.9])(i);
                });

            const chart = function (selection) {
                selection.each(function (data) {
                    const root1 = getRoot(data);

                    const svg = d3v3.select(this).append('svg')
                        .attr('width', effectiveEdge)
                        .attr('height', effectiveEdge)
                        .append('g')
                        .attr('transform', 'translate(' + (effectiveEdge / 2) + ',' + (effectiveEdge / 2) + ')')
                        .attr('id', 'KPI-Maturity-Chart');

                    let rotate = 5;
                    function rotateChart() {
                        svg.transition().attr('transform', 'translate(' + effectiveEdge / 2 + ',' + effectiveEdge / 2 + ') rotate(' + rotate + ')');
                        rotate = (rotate + 5) % 360;
                    }
                    const intervalId = window.setInterval(rotateChart, 100);


                    if (!startRotation) {
                        window.clearInterval(intervalId);
                    }


                    // Inner nodes including the last node for names
                    const g = svg.datum(root1).selectAll('path')
                        .data(partition.nodes)
                        .enter()
                        .append('g');



                    g.append('path')
                        .attr('display', function (d) { return d.depth ? null : 'none'; }) // hide inner ring
                        .attr('d', arc)
                        .attr('class', function (d) {

                        })
                        .on('mouseover', function (d) {
                            d3v3.select(this).classed('highlight', true);
                            div.transition()
                                .duration(200)
                                .style('opacity', .9);
                            div.html(d.maturityLevelsToolTip);


                        })
                        .on('mouseout', function (d) {
                            d3v3.select(this).classed('highlight', false);
                            div.transition()
                                .duration(500)
                                .style('opacity', 1);
                        })
                        .attr('class', function (d) {
                            let styleClass = 'nodesBorder';
                            if (d.depth && d.depth <= maxBarValue) {
                                styleClass += ' group-' + d.group + (d.on ? '-on' : '-off');
                            } else if (d.depth === maxBarValue + 1) {
                                styleClass += ' labelTextBackground';
                            }
                            return styleClass;
                        })
                        .attr('fill-rule', 'evenodd');

                    // Add labels to the last arc
                    g.filter(function (d) { return d.depth === maxBarValue + 1; })
                        .selectAll('.labelPath')
                        .data(function (d, i) { d.i = i; return Array(d.textLines.length).fill(d); })
                        .enter()
                        .append('path')
                        .attr('fill', 'none')
                        .attr('stroke', 'none')
                        .attr('id', function (d, i) {
                            return 'arc-label' + d.i + '-' + i;
                        })
                        .attr('d', labelArc);

                    g.filter(function (d) { return d.depth === maxBarValue + 1; })
                        .selectAll('.labelText')
                        .data(function (d, i) { d.i = i; return Array(d.textLines.length).fill(d); })
                        .enter()
                        .append('text')
                        .attr('text-anchor', 'middle')
                        .append('textPath')
                        .attr('class', 'labelText')
                        .attr('startOffset', '25%')
                        .attr('xlink:href', function (d, i) {
                            return '#arc-label' + d.i + '-' + i;
                        })
                        .text(function (d, i) {
                            return d.textLines[d.textLines.length - 1 - i];
                        });

                    // Groups data for outer circle
                    const groups = d3v3.nest()
                        .key(function (d) { return d.group; })
                        .entries(root1.children);

                    const og = svg.selectAll('.outerLabels')
                        .data(groups, function (d, i) { return i; })
                        .enter()
                        .append('g');

                    // Outer circle
                    og.append('path')
                        .attr('d', oArc)
                        .attr('class', function (d, i) { return 'outerCircleBorder group-' + (i + 1) + '-on'; });

                    // Outer labels
                    og.selectAll('.outerLabelPath')
                        .data(function (d, i) { d.i = i; return Array(d.values[0].textLines.length).fill(d); })
                        .enter()
                        .append('path')
                        .attr('fill', 'none')
                        .attr('stroke', 'none')
                        .attr('id', function (d, i) {
                            return 'outer-arc-label' + d.i + '-' + i;
                        })
                        .attr('d', outerLabelArc);

                    og.selectAll('.outerLabelText')
                        .data(function (d, i) { d.i = i; return Array(d.values[0].textLines.length).fill(d); })
                        .enter()
                        .append('text')
                        .attr('text-anchor', 'middle')
                        .attr('class', 'outerLabelText')
                        .append('textPath')
                        .attr('startOffset', '25%')
                        .attr('xlink:href', function (d, i) {
                            return '#outer-arc-label' + d.i + '-' + i;
                        })
                        .text(function (d, i) {
                            return d.values[0].textLines[d.values[0].textLines.length - 1 - i];
                            // return d.key;
                        });

                    // Center labels
                    const cg = svg.append('g');
                    const yScale = d3v3.scale.linear().domain([-1, root1.textLines.length]).range([-root1.yo * 0.5, root1.yo * 0.8]);

                    cg.selectAll('.centerLabelText')
                        .data(root1.textLines)
                        .enter()
                        .append('text')
                        .attr('x', 0)
                        .attr('y', function (d, i) { return yScale(i); })
                        .attr('text-anchor', 'middle')
                        .attr('class', 'centerLabelText')
                        .text(function (d) { return d; });
                });
            };





            function appendChild(parent, g, b, i, j) {
                const child = <any>{};
                child.group = (i + 1);
                child.textLines = (g.textLines);
                // for display description and M1 -M5  data on mouse hover
                child.maturityLevelsToolTip = <any>maturityLevelTooltip(b);

                if (j < b.value) {
                    child.on = true;
                }
                parent.children = parent.children || [];
                parent.children.push(child);

                if (j < maxBarValue) {
                    appendChild(parent.children[parent.children.length - 1], g, b, i, j + 1);
                } else {
                    child.textLines = b.textLines;
                }
            }

            function getRoot(data) {
                const root2 = <any>{};

                root2.textLines = data.textLines;
                root2.children = [];

                data.groups.forEach(function (g, i) {
                    g.bars.forEach(function (b) {
                        appendChild(root2, g, b, i, 0);
                    });
                });

                return root2;
            }

            return chart;
        }

        function maturityLevelTooltip(maturityLevelData) {
            if (maturityLevelData.maturityRange === undefined) {
                maturityLevelData.maturityRange = ['NA', 'NA', 'NA', 'NA', 'NA'];
            }
            // currently we are using static descriptio  for display tooltip, when descrioption is available in JSON then remove below function 'getMaturityLevelDescriptio
            const textLine = maturityLevelData.textLines && maturityLevelData.textLines.toString().replace(/,/g, ' ');
            let renderDescription =
                '<div class="table-wrap">' +
                '<p> <strong>Leading KPI : </strong> ' + textLine + ' </p>';

            renderDescription += '<div class="p-grid justify-content-start maturity-level-header" ><span class="p-col"><strong>Maturity Level :</strong></span>';

            if (maturityLevelData.maturityRange[0].charAt(0) === '-' && (textLine !== 'Defect Removal Efficiency' && textLine !== 'Sprint Predictability' && textLine !== 'Sprint Velocity' && textLine !== 'Regression Automation Coverage' && textLine !== 'In-sprint Automation Coverage')) {
                maturityLevelData.maturityRange[0] = '>=' + maturityLevelData.maturityRange[0].substring(1);
            }
            if (maturityLevelData.maturityRange[0].charAt(0) === '-' && (textLine === 'Defect Removal Efficiency' || textLine === 'Sprint Predictability' || textLine === 'Sprint Velocity' || textLine === 'Regression Automation Coverage' || textLine === 'In-sprint Automation Coverage')) {
                maturityLevelData.maturityRange[0] = '0 - ' + maturityLevelData.maturityRange[0].substring(1);
            }

            renderDescription += '<span class="p-col"><strong>M1</strong></br><sub>' + maturityLevelData.maturityRange[0] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M2</strong></br><sub>' + maturityLevelData.maturityRange[1] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M3</strong></br><sub>' + maturityLevelData.maturityRange[2] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M4</strong></br><sub>' + maturityLevelData.maturityRange[3] + '</sub></span>';


            if (maturityLevelData.maturityRange[4].slice(-1) === '-' && (textLine !== 'Defect Removal Efficiency' && textLine !== 'Sprint Predictability' && textLine !== 'Sprint Velocity' && textLine !== 'Regression Automation Coverage' && textLine !== 'In-sprint Automation Coverage')) {
                maturityLevelData.maturityRange[4] = maturityLevelData.maturityRange[4] + '0';
            }
            if (maturityLevelData.maturityRange[4].slice(-1) === '-' && (textLine === 'Defect Removal Efficiency' || textLine === 'Sprint Predictability' || textLine === 'Sprint Velocity' || textLine === 'Regression Automation Coverage' || textLine === 'In-sprint Automation Coverage')) {
                maturityLevelData.maturityRange[4] = maturityLevelData.maturityRange[4].slice(0, -1) + '>=';
            }

            renderDescription += '<span class="p-col"><strong>M5</strong></br><sub>' + maturityLevelData.maturityRange[4] + '</sub></span>';
            renderDescription += '';
            renderDescription += '</div></div>';
            return renderDescription;
        }



    }


}
