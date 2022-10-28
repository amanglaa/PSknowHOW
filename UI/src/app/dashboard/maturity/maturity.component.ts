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
// import * as d3 from 'd3-3';
import * as d3 from 'd3';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { HelperService } from '../../services/helper.service';
import { Router } from '@angular/router';
import { Children } from 'preact/compat';


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
        if (this.service.getSelectedTab() === 'Maturity') {
            this.masterData = $event.masterData;
            this.filterData = $event.filterData;
            this.filterApplyData = $event.filterApplyData;
            this.loaderMaturity = true;
            const kpiIdsForCurrentBoard = this.service.getMasterData()['kpiList'].filter(kpi => kpi.calculateMaturity).map(kpi => kpi.kpiId);
            // this.drawAreaChart(null, null);
            // this.chart(null);
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
                // this.loaderMaturity = false;
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
                    // this.loaderMaturity = false;
                    this.jenkinsKpiData = newObject;
                    // if (this.jenkinsKpiData && this.jenkinsKpiData.kpi70 && this.jenkinsKpiData.kpi42) {
                    //     this.drawAreaChart(this.jenkinsKpiData.kpi70.trendValueList, this.jenkinsKpiData.kpi42.trendValueList);
                    // } else {
                    //     // this.drawAreaChart(null, null);
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
                    if (this.jiraKpiData && this.jiraKpiData.kpi35 && this.jiraKpiData.kpi111) {
                        this.drawAreaChart(this.jiraKpiData.kpi35.trendValueList, this.jiraKpiData.kpi111.trendValueList);
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
                // this.loaderMaturity = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    this.bitBucketKpiData = getData;
                    const newObject = {};
                    for (const obj in this.bitBucketKpiData) {
                        newObject[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                        this.maturityValue[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                    }
                    this.bitBucketKpiData = newObject;

                    // if (this.bitBucketKpiData && this.bitBucketKpiData.kpi11 && this.bitBucketKpiData.kpi84) {
                    //     this.drawAreaChart(this.bitBucketKpiData.kpi11.value, this.bitBucketKpiData.kpi84.value);
                    // } else {
                    //     this.drawAreaChart(null, null);
                    // }

                }
            });
    }

    drawAreaChart(totalDefectCount, sprintVelocity) {
        d3.select('svg').remove();
        d3.select('.tooltip_').remove();
        const self = this;

        const startRotation = this.loaderMaturity;
        let root;
        if (startRotation) {

            root = {
                textLines: ['..'],
                children: [{
                    textLines: ['..'],
                    children: [{
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }]
                }, {
                    textLines: ['..'],
                    children: [{
                        textLines: ['..'],
                        maturity: 0
                    }]
                }, {
                    textLines: ['..'],
                    children: [{
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }, {
                        textLines: ['..'],
                        maturity: 0
                    }]
                }]
            };
        } else {
            // on loading show data;

            root = {
                textLines: ['KPI', 'Maturity Wheel'],
                children: [{
                    textLines: ['ABC'],
                    children: [{
                        textLines: ['Mean Time', 'To Merge'],
                        maturity: getMaturityValueForChart('kpi84'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi84) ? 'undefined' : this.maturityValue.kpi84.maturityRange
                    },
                    {
                        textLines: ['Number of', 'Check Ins  Merge', ' Requests'],
                        maturity: getMaturityValueForChart('kpi11'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi11) ? 'undefined' : this.maturityValue.kpi11.maturityRange
                    },
                    {
                        textLines: ['Code Build', 'Time'],
                        maturity: getMaturityValueForChart('kpi8'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi8) ? 'undefined' : this.maturityValue.kpi8.maturityRange
                    }, {
                        textLines: ['Sonar', 'Tech Debt'],
                        maturity: getMaturityValueForChart('kpi27'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi27) ? 'undefined' : this.maturityValue.kpi27.maturityRange
                    }, {
                        textLines: ['First Time', 'Pass Rate'],
                        maturity: getMaturityValueForChart('kpi82'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi82) ? 'undefined' : this.maturityValue.kpi82.maturityRange
                    }, {
                        textLines: ['Intake', 'to', 'DOR'],
                        maturity: getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[1]) ? -1 : this.maturityValue.kpi3.trendValueList[1].value[0].maturity),
                        maturityRange: undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(5, 11)
                    }, {
                        textLines: ['DoR', 'to', 'DoD'],
                        maturity: getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[2]) ? -1 : this.maturityValue.kpi3.trendValueList[2].value[0].maturity),
                        maturityRange: undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(10, 15)
                    }, {
                        textLines: ['DoD', 'to', 'Live'],
                        maturity: getMaturityValue(undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.trendValueList) || undefinedCheck(this.maturityValue.kpi3.trendValueList[3]) ? -1 : this.maturityValue.kpi3.trendValueList[3].value[0].maturity),
                        maturityRange: undefinedCheck(this.maturityValue.kpi3) || undefinedCheck(this.maturityValue.kpi3.maturityRange) ? 'undefined' : this.maturityValue.kpi3.maturityRange.slice(15, 20)
                    },
                    {
                        textLines: ['Average', 'Resolution', 'Time'],
                        maturity: getMaturityValueForChart('kpi83'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi83) ? 'undefined' : this.maturityValue.kpi83.maturityRange
                    }]
                }, {
                    textLines: ['DEF'],
                    children: [{
                        textLines: ['Defect', 'Injection', 'Rate'],
                        maturity: getMaturityValueForChart('kpi14'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi14) ? 'undefined' : this.maturityValue.kpi14.maturityRange
                    }, {
                        textLines: ['Defect', 'Seepage', 'Rate'],
                        maturity: getMaturityValueForChart('kpi35'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi35) ? 'undefined' : this.maturityValue.kpi35.maturityRange
                    }, {
                        textLines: ['Defect', 'Removal', 'Efficiency'],
                        maturity: getMaturityValueForChart('kpi34'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi34) ? 'undefined' : this.maturityValue.kpi34.maturityRange
                    }, {
                        textLines: ['Defect', 'Rejection', 'Rate'],
                        maturity: getMaturityValueForChart('kpi37'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi37) ? 'undefined' : this.maturityValue.kpi37.maturityRange
                    }, {
                        textLines: ['Test Execution', 'and', 'pass percentage'],
                        maturity: getMaturityValueForChart('kpi70'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi70) ? 'undefined' : this.maturityValue.kpi70.maturityRange
                    }, {
                        textLines: ['Unit', 'Test', 'Coverage'],
                        maturity: getMaturityValueForChart('kpi17'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi17) ? 'undefined' : this.maturityValue.kpi17.maturityRange
                    }, {
                        textLines: ['Regression', 'Automation', 'Coverage'],
                        maturity: getMaturityValueForChart('kpi42'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi42) ? 'undefined' : this.maturityValue.kpi42.maturityRange
                    }, {
                        textLines: ['In-sprint', 'Automation', 'Coverage'],
                        maturity: getMaturityValueForChart('kpi16'),
                        maturityRange: undefinedCheck(this.maturityValue.kpi16) ? 'undefined' : this.maturityValue.kpi16.maturityRange
                    }]
                }]
            };


        }

        function getMaturityValueForChart(kpiId) {
            let result = 0;
            if (kpiId === 'kpi11') {
                result = getMaturityValue(undefinedCheck(self.maturityValue.kpi11) || undefinedCheck(self.maturityValue.kpi11.trendValueList) || undefinedCheck(self.maturityValue.kpi11.trendValueList[0]) || undefinedCheck(self.maturityValue.kpi11.trendValueList[0].value) ? -1 : self.maturityValue.kpi11.trendValueList[0].value[0].maturity);

            } else {
                result = getMaturityValue(undefinedCheck(self.maturityValue[kpiId]) ||
                    undefinedCheck(self.maturityValue[kpiId].trendValueList) ||
                    undefinedCheck(self.maturityValue[kpiId].trendValueList[0]) ? -1 : self.maturityValue[kpiId].trendValueList[0].maturity);
            }
            return result;
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

        d3.select('.chart123').datum(root).call(sunburstBarChart());


        const div = d3.select('.chart123').append('div')
            .attr('class', 'tooltip_')
            .style('opacity', 1)
            .style('display', 'inline-block');

        function sunburstBarChart() {
            const edge = 720;
                const maxBarValue = 5;
                const rotation = -95 * Math.PI / 180;

            const radius = edge / 2;
                const effectiveEdge = edge * 0.9;
                // scale = d3.scaleLinear().domain([0, maxBarValue + 5]);

            const chart = function(selection) {
                selection.each(function(data) {

                    // Data strucure
                    const partition = d3.partition()
                        .size([2 * Math.PI, radius]);

                    // Find data root
                    const root = d3.hierarchy(getRoot2(JSON.parse(JSON.stringify(data))))
                        .sum(function(d) {
                            return d.size;
                        });


                    // Size arcs
                    partition(root);

                    const svg = d3.select(this).append('svg')
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

                    const y = d3.scaleLinear()
                        .range([-(maxBarValue + 2), maxBarValue + 2]);
                    const arc = d3.arc()
                        .startAngle(function(d) {
 return d.x0 + rotation;
})
                        .endAngle(function(d) {
 return d.x1 + rotation;
})
                        .innerRadius(function(d) {
                            if (d.depth && d.depth > 2 && d.depth < maxBarValue + 3) {
                                d.yi = Math.max(0,radius - y(Math.sqrt(d.y1)));
                            } else {
                                d.yi = radius -24 - d.y1;
                            }
                            return d.yi;
                        })
                        .outerRadius(function(d) {
                            if (d.depth && d.depth > 2 && d.depth < maxBarValue + 3) {
                                d.yo = Math.max(0,radius - y(Math.sqrt(d.y0)));
                            } else {
                                d.yo = radius -24 - d.y0;
                            }
                            return d.yo;
                        });

                    // Put it all together
                    svg.selectAll('path')
                        .data(root.descendants())
                        .enter().append('g').attr('class', 'node')
                        .append('path')
                        .attr('display', function(d) {
 return d.depth ? null : 'none';
})
                        .attr('d', arc)
                        .style('stroke', '#fff')
                        .on('mouseover', function(event,d) {
                            if(d.depth > 1) {
                            d3.select(this).classed('highlight', true);
                            div.transition()
                                .duration(200)
                                .style('opacity', .9);
                            div.html(d.data.maturityLevelsToolTip);
                            }
                        })
                        .on('mouseout', function(event,d) {
                            d3.select(this).classed('highlight', false);
                            div.transition()
                                .duration(500)
                                .style('opacity', 1);
                        })
                        .attr('class', function(d) {
                            let styleClass = 'nodesBorder';
                            if (d.depth && d.depth > 2 && d.depth < maxBarValue + 3) {
                                styleClass += ' group-' + d.data.group + (d.data.on ? '-on' : '-off');
                            }
                            if (d.depth === 2) {
                                styleClass += ' labelTextBackground';
                            }

                            if (d.depth === 1) {
                                styleClass += ' group-' + d.data.children[0].group + '-on';
                            }
                            return styleClass;
                        })
                        .attr('fill-rule', 'evenodd');

                    const labelArc = d3.arc()
                        .startAngle(function(d) {
 return d.x0 + rotation;
})
                        .endAngle(function(d) {
 return d.x1 + rotation;
})
                        .innerRadius(function(d, i) {
                            if (d.data && d.data.textLines) {
                                return d3.scaleLinear().domain([-1, d.data.textLines.length]).range([d.yi, d.yo])(i);
                            }
                            return radius + 80 - d.y1;
                        })
                        .outerRadius(function(d, i) {
                            if (d.data && d.data.textLines) {
                                return d3.scaleLinear().domain([-1, d.data.textLines.length]).range([d.yi, d.yo])(i);
                            }
                            return radius + 80 - d.y0;
                        });

                    // Add labels to the last arc
                    svg.selectAll('.node').filter(function(d) {
 return d.depth === 2;
})
                        .selectAll('.labelPath')
                        .data(function(d, i) {
 d.i = i; return Array(d.data.textLines.length).fill(d);
})
                        .enter()
                        .append('path')
                        .attr('fill', 'none')
                        .attr('stroke', 'none')
                        .attr('id', function(d, i) {
                            return 'arc-label' + d.i + '-' + i;
                        })
                        .attr('d', labelArc);

                    svg.selectAll('.node').filter(function(d) {
 return d.depth === 2;
})
                        .selectAll('.labelText')
                        .data(function(d, i) {
 d.i = i; return Array(d.data.textLines.length).fill(d);
})
                        .enter()
                        .append('text')
                        .attr('text-anchor', 'middle')
                        .append('textPath')
                        .attr('class', 'labelText')
                        .attr('startOffset', '25%')
                        .attr('xlink:href', function(d, i) {
                            return '#arc-label' + d.i + '-' + i;
                        })
                        .text(function(d, i) {
                            return d.data.textLines[d.data.textLines.length - 1 - i];
                        });

					// Center labels
                     const cg = svg.append('g');
                     const yScale = d3.scaleLinear().domain([-1, root.data.textLines.length]).range([-root.yo * 0.5, root.yo * 0.8]);

                     cg.selectAll('.centerLabelText')
                         .data(root.data.textLines)
                         .enter()
                         .append('text')
                         .attr('x', 0)
                         .attr('y', function(d, i) {
 return yScale(i) - 100*i;
})
                         .attr('text-anchor', 'middle')
                         .attr('class', 'centerLabelText')
                         .text(function(d) {
 return d;
});

                });
            };

            function getRoot2(data) {
                const root2 = <any>{};

                root2.textLines = data.textLines;
                root2.children = [];

                data.children.forEach(function(group, i) {
                    root2.children.push(group);
                });

                root2.children.forEach((group, groupId) => {
                    group.children.forEach(function(kpi) {
                        kpi.children = [];
                        kpi.group = groupId + 1;
                        kpi.children = appendChild2(kpi, groupId, 0);

                        // swap parents and children
                        const flatData = collectNodes(JSON.parse(JSON.stringify(kpi.children)));
                        kpi.children = reverseNodes(JSON.parse(JSON.stringify(flatData.reverse())));
                    });
                });

                return root2;
            }

            function reverseNodes(flatData) {
                const nodes = [];
                nodes[0] = flatData.shift();
                delete nodes[0].children;
                delete nodes[0].size;
                function visitNode(collection) {
                    if (collection) {
                        while (flatData.length > 1) {
                            collection.children = [flatData.shift()];
                            delete collection.children[0].size;
                            collection.children[0].children = [{}];
                            visitNode(collection.children[0]);
                        }

                        if (flatData.length === 1) {
                            collection.children = [flatData.shift()];
                            collection.children[0].size = 1;
                            delete collection.children[0].children;
                        }
                    }
                }
                visitNode(nodes[0]);
                return nodes;
            }

            function collectNodes(rootNode) {
                const nodes = [];
                function visitNode(node) {
                    nodes.push(node);
                    if (node.children) {
                        node.children.forEach(visitNode);
                    }
                }
                visitNode(rootNode[0]);
                return nodes;
            }

            function appendChild2(kpi, groupId, j) {

                const child = <any>{};
                child.group = (groupId + 1);
                child.maturity = kpi.maturity;
                child.maturityRange = kpi.maturityRange;
                child.maturityLevelsToolTip = <any>maturityLevelTooltip(kpi);
                child.textLines = kpi.textLines;
                if (j < kpi.maturity) {
                    child.on = true;
                }
                if (j + 1 < maxBarValue) {
                    child.children = [];
                    kpi.children.push(child);
                    appendChild2(kpi.children[kpi.children.length - 1], groupId, j + 1);
                } else {
                    child.textLines = kpi.textLines;
                    child.maturity = kpi.maturity;
                    child.group = (groupId + 1);
                    child.size = 1;
                    child.maturityLevelsToolTip = <any>maturityLevelTooltip(kpi);
                    child.maturityRange = kpi.maturityRange;
                    kpi.children.push(child);
                }

                return kpi.children;
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
