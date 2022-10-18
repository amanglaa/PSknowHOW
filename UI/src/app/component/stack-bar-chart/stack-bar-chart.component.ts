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

 import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges } from '@angular/core';
 declare var $: any;
 import * as d3v3 from 'd3-3';
 
 
 @Component({
  selector: 'app-stack-bar-chart',
  templateUrl: './stack-bar-chart.component.html',
  styleUrls: ['./stack-bar-chart.component.css']
 })
 
 
 
 export class StackBarChartComponent implements OnChanges {
 
   elem;
   @Input() data: any;
   constructor(private viewContainerRef: ViewContainerRef) { }
 
   ngOnChanges(changes: SimpleChanges) {
     // only run when property "data" changed
     if (changes['data']) {
       this.elem = this.viewContainerRef.element.nativeElement;
       if (!changes['data'].firstChange) {
         this.draw('update');
       } else {
         this.draw('new');
       }
     }
   }
 
 
   draw(status) {
     const elem = this.elem;
     if (status !== 'new') {
       d3v3.select(elem).select('svg').remove();
       d3v3.select(elem).select('.tooltip').remove();
       d3v3.select(elem).select('.legend').remove();
 
     }
 
     const data1 = this.data;
 
     const agingData = [];
     for (const i in data1) {
       const tempObj = {};
       tempObj['Original Pass %'] = parseInt(data1[i].passed, 10);
       tempObj['Original Execution %'] = parseInt(data1[i].executed, 10);
 
       tempObj['Pass %'] = (tempObj['Original Pass %'] * tempObj['Original Execution %']) / 100;
       tempObj['Execution %'] = tempObj['Original Execution %'] - tempObj['Pass %'];
 
       // take date in case of Kanban, otherwise index for the x-axis
       if (data1[i].kanbanDate) {
         tempObj['data'] = data1[i].kanbanDate;
       } else {
         tempObj['data'] = parseInt(i, 10) + 1;
       }
 
       tempObj['projectName'] = data1[i].sProjectName;
       tempObj['date'] = data1[i].date;
       if (data1[i] && data1[i].howerValue && Object.keys(data1[i].howerValue).length !== 0) {
         tempObj['hoverValue'] = data1[i].howerValue;
       }
       agingData.push(tempObj);
     }
 
     const data = agingData;
 
     const margin = {
       top: 20,
       right: 10,
       bottom: 20,
       left: 40
     };
 
     // fix for DTI-15996: Intermittent display issue in Test Execution Percentage chart
     let staticWidth = 883;
     staticWidth = staticWidth > d3v3.select(elem).select('.stackChart').node().getBoundingClientRect().width ? staticWidth : d3v3.select(elem).select('.stackChart').node().getBoundingClientRect().width;
       const width = staticWidth - 100 - margin.left - margin.right,
       height = 220 - margin.top - margin.bottom;
 
     const svg = d3v3.select(this.elem).select('.stackChart')
       .append('svg')
       .attr('width', width + margin.left + margin.right)
       .attr('height', height + margin.top + margin.bottom)
       .append('g')
       .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
 
 
     // removing data from list
     const removeData = ['projectName', 'data', 'Original Pass %', 'Original Execution %', 'hoverValue', 'date'];
     const legendData = Object.keys(data[0]);
     for (let index = 0; index < removeData.length; index++) {
       const pos = legendData.indexOf(removeData[index]);
       if (pos > -1) {
         legendData.splice(pos, 1);
       }
     }
 
 
 
     // Transpose the data into layers
     const dataset = d3v3.layout.stack()(legendData.map(function (project) {
       return data.map(function (d) {
         return {
           x: d.data,
           y: +d[project],
           originalExecution: d['Original Execution %'],
           originalPass: d['Original Pass %'],
           hoverValue: d['hoverValue'],
           name: project,
           date: d['date']
         };
       });
     }));
 
 
     // Set x, y and colors
     const x = d3v3.scale.ordinal()
       .domain(dataset[0].map(function (d) {
         return d.x;
       }))
       .rangeRoundBands([10, width - 10], 0.02);
 
     const y = d3v3.scale.linear()
       .domain([0, 100])
       .range([height, 0]);
 
     const colors = ['#FFC35C', '#FFF1C4', '#C6DBEF', '#74C476', '#BAE4B3', '#9E9AC8', '#FC9272', '#FCBBA1', '#FEE0D2', '#fda899', '#eba6ff', '#bcca83', '#c6c0cb', '#f9feae', '#a6fe8e', '#bfcd03', '#ebd3b6', '#fec0df', '#c2eefc', '#cac2fc', '#bcfcd6', '#f4e5ff', '#f9dc56', '#80da93', '#ebb678', '#b1fb27', '#8dd0df', '#1ff479', '#cedbd8', '#d2e6b8', '#85feea', '#97db5c', '#deb7b7', '#cad5ef', '#d0eb6d', '#fe9fe1', '#bdc7ad', '#fcdadf', '#d9bae2', '#dac05a', '#ecda91', '#fef5d0', '#ffa4bc', '#d2fcf5', '#b1c7d0', '#e6f0fb', '#83e5bd', '#b2e598', '#fdfb73', '#a6cfa2', '#a1e3e1', '#e4e21c', '#8ef8b0', '#fdc7ff', '#cefeb9', '#d4c491', '#fdc5b5', '#e4fdde', '#d5ccc7', '#acdaf8', '#e4c9da', '#fdc664', '#badfc5', '#bbd15f', '#fdf1f7', '#b4c3ed', '#93e8fd', '#ddd9e3', '#e5e5d7', '#98e116', '#e0d0fb', '#fccd95', '#aafdfe', '#deba9d', '#dce994', '#d6b2fb', '#c5bedd', '#bec4c3', '#feab79', '#82ea7f', '#fed7f3', '#8affd2', '#d6fc50', '#e8aed9', '#f7b7c2', '#2bf4e5', '#ffad53', '#d3fe94', '#c2e530', '#aef161', '#fee5d4', '#a3d378', '#dad351', '#fec328', '#a4e3b3', '#a9efd9', '#feeb8a', '#85d4cd', '#e0b4c9', '#55e358', '#bddbe1', '#48ffa0', '#d6eee1', '#fee3b0', '#ff99fd', '#3fe0b0', '#84d7fe', '#7be9d3', '#1bf3cd', '#d4d4af', '#d6d278', '#70fffe', '#aac8e1', '#ffb0fe', '#b5ccbf', '#c6ccd5', '#ccd9c5', '#feee3e', '#cdc0ab', '#58e182', '#e9b8f8', '#f7bc97', '#b4e275', '#bee8e1', '#e1e3ff', '#ebebb2', '#8fd4af', '#c0d3a2', '#e7c67d', '#6ceba9', '#d8cde4', '#eacdc6', '#d4eef1', '#e7e8e9', '#7deaec', '#88f31d', '#c0efbe', '#75d8be', '#a0d1d3', '#fdacd4', '#09f64f', '#c4dc8c', '#d0d1fc', '#ecccf1', '#f7d517', '#d0dde8', '#e7dadc', '#78fe8b', '#e6e373', '#cde5fd', '#b1fead', '#e7fec5', '#e9fdfd', '#b9c3d9', '#cebec1', '#eab3a4', '#edb84a', '#8aea52', '#bad5fd', '#a7ddea', '#f9c9d5', '#f2deec', '#fbf4e7', '#b3d8cf', '#feb9ed', '#96e89d', '#d5d99c', '#efd16f', '#ecfb92', '#d0bbd2', '#89d97b', '#cdc47c', '#acd345', '#21e0f4', '#9ed693', '#9adfcd', '#ccdc4c', '#e3e3c2', '#d8f003', '#e7f158', '#b8fff2', '#cdb9e9', '#faa8ae', '#cac75c', '#d7d119', '#eccb48', '#bbd7ea', '#a0f3c1', '#cbeaa6', '#c5f4e0', '#ede9f5', '#b8bffd', '#97cced', '#70dba6', '#72dbf2', '#e6bddd', '#ddc2fe', '#b8ddb0', '#e7d29e', '#c7ec8c', '#d1edce', '#c8fb75', '#f9ffe8', '#e9abeb', '#b5d1b3', '#feb6af', '#ffba6e', '#92f772', '#e8f4cb', '#ffeda8', '#e5ff77', '#f6f8fe', '#c4c699', '#c5ca3f', '#e9bb29', '#19e2dc', '#d9bfb4', '#c6cabf', '#e6c091', '#bcd0d0', '#d5cbd3', '#aae346', '#d0d2d3', '#f1c7ab', '#72ef95', '#cdda6f', '#d9d5c0', '#a2eb81', '#72f3cc', '#91efe6', '#fed284', '#a6edf6', '#4bfee0', '#9bfb4f', '#c1f3f6', '#e0f9ab', '#d4ffd2', '#fffac1', '#a9c5fd', '#f0acc4', '#7adf34', '#a8d516', '#86dade', '#6cdfd7', '#a7d5b9', '#b4d899', '#f1c0bf', '#a9e8c7', '#e6e04c', '#fcd4bf', '#67feb6', '#ddfeee', '#f5af92', '#a7cddc', '#c7cae9', '#e1c2c9', '#06f6a4', '#64f36c', '#ffd056', '#e9d3ea', '#94f595', '#eadbcc', '#acf2b2', '#a2fde0', '#f5e5c6', '#f7eb67', '#f1e9e6', '#e2efdb', '#fbfd52', '#aacac8', '#6fdf6e', '#23e83d', '#d6c33e', '#edbb66', '#96d8f1', '#d9c9a7', '#30ef8c', '#66ed41', '#eabeed', '#c3dbce', '#d6d6e9', '#f8c6ee', '#d5d9d1', '#96f0cc', '#c7ef56', '#8ef6ff', '#aff0e8', '#b7f593', '#dee7e0', '#bdff5c', '#eff01a', '#d7f0fe', '#bdfdc3', '#f0ef9c', '#d3fe14', '#bec0ea', '#c6c2be', '#eab1b3'];
 
 
 
     // Define and draw axes
     const yAxis = d3v3.svg.axis()
       .scale(y)
       .orient('left')
       .ticks(5)
       .tickSize(-width, 0, 0)
       .tickFormat(function (d) {
         return d;
       });
 
     const xAxis = d3v3.svg.axis()
       .scale(x)
       .orient('bottom');
 
     svg.append('g')
       .attr('class', 'y axis')
       .call(yAxis);
 
     svg.append('g')
       .attr('class', 'x axis')
       .attr('transform', 'translate(0,' + height + ')')
       .call(xAxis);
 
     // Prep the tooltip bits, initial display is hidden
     const div = d3v3.select(this.elem).select('.stackChart').append('div')
       .attr('class', 'tooltip')
       .style('opacity', 0)
       .style('display', 'none');
 
 
     // Create groups for each series, rects for each segment
     const groups = svg.selectAll('g.ageing')
       .data(dataset)
       .enter().append('g')
       .attr('class', 'ageing')
       .style('fill', function (d, i) {
         return colors[i];
       });
 
     groups.selectAll('rect')
       .data(function (d) {
         return d;
       })
       .enter()
       .append('g')
       .append('rect')
       .attr('x', function (d) {
         return x(d.x);
       })
       .attr('y', function (d) {
         return y(d.y0 + d.y);
       })
       .attr('height', function (d) {
         return y(d.y0) - y(d.y0 + d.y);
       })
       .attr('width', x.rangeBand() - 10)
       .on('mouseover', function (d) {
 
         // on hover showing tooltip
         div.style('display', 'block');
         div.transition()
           .duration(200)
           .style('opacity', .9);
         const toolTipObj = {};
 
         if (d.date) {
           toolTipObj['date'] = d.date;
         }
         if (d.name === 'Execution %') {
           if (d.hoverValue) {
             toolTipObj['Executed'] = d.hoverValue['Executed'];
             toolTipObj['Total'] = d.hoverValue['Total'];
           }
           toolTipObj[d.name] = d.originalExecution;
 
 
         }
         if (d.name === 'Pass %') {
           if (d.hoverValue) {
             toolTipObj['Pass'] = d.hoverValue['Passed'];
             toolTipObj['Executed'] = d.hoverValue['Executed'];
           }
 
           toolTipObj[d.name] = d.originalPass;
 
         }
 
         div.html('');
         for (const key in toolTipObj) {
           div.append('div').html(`${key}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${toolTipObj[key]}` + ' </span>');
         }
 
 
 
       })
       .on('mouseout', function () {
         div.style('display', 'none');
         div.transition().duration(500).style('opacity', 0);
       })
       .on('mousemove', function (d) {
         const xPosition = d3v3.mouse(this)[0] - 15;
         let yPosition;
 
 
         if (d.hoverValue) {
           yPosition = d3v3.mouse(this)[1] - 85;
           d3v3.select('.tooltip').style('height', '100px');
         } else {
           yPosition = d3v3.mouse(this)[1] - 35;
           d3v3.select('.tooltip').style('height', '35px');
 
 
         }
 
         div.style('display', 'block');
         div.transition()
           .duration(0).attr('transform', 'translate(' + xPosition + ',' + yPosition + ')').style('opacity', 1)
           .style('left', xPosition + 'px')
           .style('top', yPosition + 'px');
 
 
       });
 
 
 
     // width of single Bar
     const widthOfBar = d3v3.select(this.elem).select('g.ageing:last-child g:last-child rect').node().getBoundingClientRect().width;
 
 
     // labels on the bar chart
     svg.selectAll('g.ageing:last-child').selectAll('g').append('text').attr('dy', '1.3em')
       .attr('x', function (d) {
         return x(d.x) + widthOfBar / 2;
       })
       .attr('y', function (d) {
         return y(d.y0 + d.y) - 20;
       })
       .attr('text-anchor', 'middle')
       .attr('font-family', 'sans-serif')
       .attr('font-size', '11px')
       .attr('fill', 'black')
       .text(function (d) {
         if (d.name === 'Execution %') {
           return d.originalExecution;
         }
         if (d.name === 'Pass %') {
           return d.originalPass;
         }
       });
 
     const YCaption = svg.append('g')
       .attr('class', 'y axis')
       .append('text')
       .attr('y', -25)
       .attr('x', -110)
       .attr('transform', 'rotate(-90)')
       .attr('fill', '#000');
 
     // adding yaxis caption
     YCaption.text('Execution %');
 
 
     // Draw legend
     d3v3.select(this.elem).select('.stackChart').append('div')
       .attr('class', 'legend');
     const svg2 = d3v3.select(this.elem).select('.legend')
       .append('svg')
       .attr('width', 100)
       .attr('height', 20 * (dataset.length + 1));
     const legend = svg2.append('g').attr('class', 'legend1').attr('transform', 'translate(-20,30)');
     legend.selectAll('rect')
       .data(dataset)
       .enter()
       .append('rect')
       .attr('x', 30)
       .attr('y', function (d, i) {
         return (i - 1) * 20;
       })
       .attr('width', 10)
       .attr('height', 10)
       .style('fill', function (d, i) {
         const color = colors[i];
         return color;
       });
     legend.selectAll('text')
       .data(dataset)
       .enter()
       .append('text')
       .attr('x', 40 + 5)
       .attr('width', 5)
       .attr('height', 5)
       .attr('y', function (d, i) {
         return (i - 1) * 20 + 7;
       })
       .text(function (d, i) {
         switch (i) {
           case i:
             return legendData[i];
         }
       });
 
 
 
 
   }
 
 }