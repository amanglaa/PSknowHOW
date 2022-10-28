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

import {
  Component,
  Input,
  ViewContainerRef,
  OnChanges,
  SimpleChanges,
  OnInit,
} from '@angular/core';
import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-grouped-column-plus-line-chart',
  templateUrl: './grouped-column-plus-line-chart.component.html',
  styleUrls: ['./grouped-column-plus-line-chart.component.css'],
})
export class GroupedColumnPlusLineChartComponent implements OnInit, OnChanges {
  @Input() data: any;
  @Input() lineChart: boolean;
  @Input() thresholdValue: number;
  @Input() color: any;
  @Input() yCaption: string;
  @Input() xCaption: string;
  @Input() unit: string;
  @Input() barLegend: string;
  @Input() lineLegend: string;
  @Input() selectedtype: string;
  elem: any;
  drillDownLevel: number;
  lastLevel: any;
  dataPoints: any;
  barChart = true;
  maxValue = 1000;
  unmodifiedData: any = [];

  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban') {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    // only run when property "data" changed
    if (changes['data']) {
      this.elem = this.viewContainerRef.element.nativeElement;
      this.unmodifiedData = JSON.parse(JSON.stringify(this.data));
      this.dataPoints = this.unmodifiedData.length;
      this.data = this.transform2(this.data);
      this.draw2(this.data);
    }
  }

  transform2(data) {
    const result = [];
    const newObj = {};
    newObj['values'] = [];

    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < data[0].value.length; i++) {
      if (data[0].value[i].hoverValue) {
        newObj['values'].push({
          value: data[0].value[i].value,
          lineValue: data[0].value[i].lineValue,
          hoverValue: data[0].value[i].hoverValue,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
        });
      } else {
        newObj['values'].push({
          value: data[0].value[i].value,
          lineValue: data[0].value[i].lineValue,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
        });
      }
    }

    newObj['values'].forEach((element, index) => {
      const newNewObj = {};
      newNewObj['categorie'] = index + 1;
      newNewObj['values'] = [element];
      result.push(newNewObj);
    });

    for (let i = 1; i < data.length; i++) {
      for (let j = 0; j < data[i].value.length; j++) {
        // eslint-disable-next-line @typescript-eslint/no-shadow
        const newObj = {};
        newObj['values'] = [];
        if (result[j] && result[j]['categorie'] && j + 1 === result[j]['categorie']) {
          if (data[i].value[j].hoverValue) {
            result[j].values.push({
              value: data[i].value[j].value,
              lineValue: data[i].value[j].lineValue,
              hoverValue: data[i].value[j].hoverValue,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          } else {
            result[j].values.push({
              value: data[i].value[j].value,
              lineValue: data[i].value[j].lineValue,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          }
        }
      }
    }

    return result;
  }

  draw2(data) {
    const showUnit = this.unit?.toLowerCase() !== 'number' ? this.unit : '';
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#svgLegend').select('svg').remove();
    d3.select(this.elem).select('#legendIndicator').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();

    const self = this;

    const categoriesNames = data.map((d) => d.categorie);
    const rateNames = data[0].values.map((d) => d.rate);

    const margin = { top: 35, right: 50, bottom: 50, left: 50 };
    const barWidth = 20;
    const width = data.length <= 5 ? document.getElementById('chart').offsetWidth - 70 : data.length * barWidth * 8;
    const height = 225;
    const paddingFactor = width < 600 ? 0.30 : 0.55;

    const x0 = d3.scaleBand().range([0, width - margin.left]).padding([((6 + this.dataPoints) / (3 * this.dataPoints)) * paddingFactor]);

    const x1 = d3.scaleBand();

    const y = d3.scaleLinear().range([height - margin.top, 0]);

    x0.domain(categoriesNames);
    x1.domain(rateNames)
      .range([0, x0.bandwidth()]); //.padding(0.0);

    const maxBarValue = d3.max(data, (categorie) => d3.max(categorie.values, (d) => d.value));

    const maxLineValue = d3.max(data, (categorie) => d3.max(categorie.values, (d) => d.lineValue));

    let divisor = 10;
    let power = 1;
    let maxVal = maxBarValue >= maxLineValue ? maxBarValue : maxLineValue;
    if (self.thresholdValue) {
      maxVal = maxVal >= self.thresholdValue ? maxVal : self.thresholdValue;
    }
    let quotient = maxVal;
    while (quotient >= 1) {
      quotient = quotient / Math.pow(divisor, power);
      ++power;
    }
    divisor = Math.pow(10, power > 1 ? power - 1 : 1);


    let maxYValue = maxVal;


    if (maxYValue > 0 && maxYValue <= 50) {
      maxYValue = 50;
    } else if (maxYValue > 50 && maxYValue <= 100) {
      maxYValue = 100;
    } else if (maxYValue > 100 && maxYValue <= 200) {
      maxYValue = 200;
    } else if (maxYValue > 200 && maxYValue <= 500) {
      maxYValue = 500;
    } else if (maxYValue > 500) {
      maxYValue += divisor;
    }

    if (!maxYValue) {
      maxYValue = 50;
    }

    y.domain([0, maxYValue]);

    let tickPadding = 10;
    if (width < 600) {
      tickPadding = 6;
    }

    const xAxis = d3
      .axisBottom(x0)
      .tickSize(0)
      .tickPadding(tickPadding);
    // .orient('bottom');

    const yAxis = d3
      .axisLeft(y)
      // .orient('left')
      .ticks(5)
      .tickSize(-height, 0, 0);
    // .tickPadding(15);

    const color = d3.scaleOrdinal().range(this.color);

    const svgX = d3
      .select(this.elem)
      .select('#horizontalSVG')
      .append('svg')
      .attr('width', width)
      .attr('height', height);
    // .append('g');


    const svgY = d3
      .select(this.elem)
      .select('#verticalSVG')
      .append('svg')
      .attr('height', height)
      .attr('width', 50);

    const svgLegend = d3
      .select(this.elem)
      .select('#svgLegend')
      .append('svg')
      .attr('width', width + margin.right - 50)
      .attr('height', 50)
      .append('g');


    svgX
      .append('g')
      .attr('class', 'xAxis')
      .attr('transform', 'translate(0,' + (height - margin.top) + ')')
      .attr('stroke-width', '1')
      .attr('opacity', '1')
      .call(xAxis);

    // eslint-disable-next-line @typescript-eslint/naming-convention
    const XCaption = d3
      .select(this.elem).select('#xCaptionContainer').append('text');

    if (this.xCaption) {
      XCaption.text(this.xCaption);
    } else {
      XCaption.text('Sprints');
    }

    svgY
      .append('g')
      .attr('class', 'yAxis')
      .call(yAxis.tickSize(0))
      .style('opacity', '0')
      .attr('transform', 'translate(' + margin.left + ',' + 5 + ')')
      .append('text')
      .attr('x', -60)
      .attr('y', -40)
      .attr('transform', 'rotate(-90)')
      .attr('fill', '#437495')
      .attr('font-size', '12px')
      .text(self.yCaption);

    // gridlines
    svgX.selectAll('line.gridline').data(y.ticks(4)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', width)
      .attr('y1', (d) => y(d))
      .attr('y2', (d) => y(d))
      .style('stroke', '#dedede')
      .style('fill', 'none')
      .attr('class', 'gridline');

    svgY
      .select('.yAxis')
      .transition()
      .duration(500)
      .delay(1300)
      .style('opacity', '1')
      .style('font-size', '10px');

    const xTick = self.dataPoints === 1 ? 12 : 0;
    svgX
      .select('.xAxis')
      .selectAll('.tick text')
      .attr('y', 10)
      .attr('x', xTick)
      .style('font-size', '10px')
      .style('fill', 'black');

    svgX
      .select('.xAxis')
      .selectAll('line')
      .attr('x1', xTick)
      .attr('x2', xTick)
      .attr('y1', 0)
      .attr('y2', 5)
      .style('stroke', '#333333');

    const slice = svgX
      .selectAll('.slice')
      .data(data)
      .enter()
      .append('g')
      .attr('class', 'g')
      .attr('transform', (d) => 'translate(' + x0(d.categorie) + ',0)');

    slice
      .selectAll('rect')
      .data((d) => d.values)
      .enter()
      .append('rect')
      .attr('width', barWidth)
      .attr('x', (d, i) =>paddingFactor < 0.55 && data.length <= 5 && self.dataPoints === 1 ? x1(d.rate) + barWidth / 1.5 : x1(d.rate))
      .style('fill', (d) => color(d.rate))
      .attr('y', (d) => y(0))
      .attr('height', (d) => height - margin.top - y(0))
      .attr('class', 'bar')
      .on('mouseover', (d, i) => { })
      .on('mouseout', (d) => {
        // d3.select(this).style('fill', color(d.rate));
      });

    slice
      .selectAll('rect')
      .transition()
      .delay((d) => 200)
      .duration(1000)
      .attr('y', (d) => y(d.value))
      .attr('height', (d) => height - margin.top - y(d.value));

    // threshold line
    if (self.thresholdValue) {
      if (self.thresholdValue > maxYValue) {
        self.thresholdValue = maxYValue;
        self.thresholdValue++;
      }
      svgX
        .append('svg:line')
        .attr('x1', 0)
        .attr('x2', width)
        .attr('y1', y(self.thresholdValue))
        .attr('y2', y(self.thresholdValue))
        .style('stroke', '#333333')
        .style('stroke-dasharray', '4,4')
        .attr('class', 'thresholdline');
      svgX
        .append('text')
        .attr('x', width - 40)
        .attr('y', y(self.thresholdValue))
        .attr('dy', '.5em')
        .attr('text-anchor', 'end')
        .text(self.thresholdValue)
        .attr('class', 'thresholdlinetext');
    }

    // Define the div for the tooltip
    const div = d3.select(this.elem).select('#chart').append('div')
      .attr('class', 'tooltip')
      .style('display', 'none')
      .style('opacity', 0);

    // bar legend
    const prevLength = -40;
    let legend = svgLegend.selectAll('.d3-legend')
      .data(data[0].values)
      .enter()
      .append('g')
      .attr('class', 'd3-legend')
      .attr('transform', (d, i) => {
        const len = ((i + 1) * 160) + prevLength;
        return 'translate(' + (len) + ', 0)';
      });

    // Legend indicator  .attr("x", width/2)
    legend.append('rect')
      .attr('width', 12)
      .attr('height', 12)
      .style('fill', (d, i) => color(i));

    //Legend text /.attr("x", width/2 + 20)
    legend.append('text')
      .attr('x', 24)
      .attr('y', 2)
      .attr('dy', '.85em')
      .style('text-anchor', 'start')
      .style('font-size', 10)
      .text((d) => d.rate.length > 15 ? d.rate.substring(0, 12) + '...' : d.rate);

    // bar legend text
    svgLegend.append('text')
      .attr('x', 0)
      .attr('y', 0)
      .attr('dy', '.85em')
      .style('text-anchor', 'start')
      .style('font-size', 10)
      .text(self.barLegend);
    // self.lineChart = false;
    if (self.lineChart) {
      const lineOpacity = '1';
      const lineOpacityHover = '0.85';
      const otherLinesOpacityHover = '0.1';
      const lineStroke = '2px';
      const lineStrokeHover = '4px';
      const circleOpacity = '1';
      const circleOpacityOnLineHover = '0.25';
      const circleRadius = 3;
      const circleRadiusHover = 4;
      const duration = 250;

      try {
        const newRawData = JSON.parse(JSON.stringify(self.unmodifiedData));
        const colorArr = this.color;

        /* Add line into SVG acoording to data */
        let maxObjectNo = 0;
        let maxXValueCount = 0;
        // used to find object whose value is max on x axis
        for (const maxCount in newRawData) {
          if (maxXValueCount < newRawData[maxCount].value.length) {
            maxXValueCount = newRawData[maxCount].value.length;
            maxObjectNo = parseInt(maxCount, 10);
          }
        }
        const yScale = d3.scaleLinear()
          .domain([0, maxYValue])
          .range([height - margin.top, 0]);


        const xScale = d3.scaleBand()
          .rangeRound([0, width - margin.left])
          .domain(newRawData[maxObjectNo].value.map((d, i) => i + 1))
          .padding([((6 + self.dataPoints) / (3 * self.dataPoints)) * paddingFactor]);


        const elem = this.elem;

        const lines = svgX.append('g')
          .attr('class', 'lines');

        const line = d3.line()
          .x((d, i) => paddingFactor < 0.55 && data.length <= 5 && self.dataPoints === 1 ? xScale(i + 1) + barWidth / 1.5 : xScale(i + 1))
          .y(d => yScale(d.lineValue));

        lines.selectAll('.line-group')
          .data(newRawData)
          .enter()
          .append('g')
          .attr('class', (d, i) =>'line-group' + i)
          .on('mouseover', (d, i) => {
            svgX.append('text')
              .attr('class', 'title-text')
              .style('fill', colorArr[i])
              .text(d.data)
              .attr('text-anchor', 'middle')
              .attr('x', (width - margin.left) / 2)
              .attr('y', 15);
          })
          .on('mouseout', (d) => {
            svgX.select('.title-text').remove();
          })
          .append('path')
          .attr('class', (d, i) => {
            const className = 'line' + i;
            return className;
          })
          .attr('d', (d, i) => line(d.value))
          .style('stroke', (d, i) => colorArr[i])
          .style('opacity', lineOpacity)
          .style('fill', 'none')
          .style('stroke-width', '2')
          .on('mouseover', function(d) {
            d3.select(elem).selectAll('.line')
              .style('opacity', otherLinesOpacityHover);
            d3.select(elem).selectAll('.circle')
              .style('opacity', circleOpacityOnLineHover);
            d3.select(this)
              .style('opacity', lineOpacityHover)
              .style('stroke-width', lineStrokeHover)
              .style('cursor', 'pointer');
          })
          .on('mouseout', function(d) {
            d3.selectAll('.line')
              .style('opacity', lineOpacity);
            d3.selectAll('.circle')
              .style('opacity', circleOpacity);
            d3.select(this)
              .style('stroke-width', lineStroke)
              .style('cursor', 'none');
          });

        /* Add circles (data) on the line */
        lines.selectAll('circle-group')
          .data(newRawData).enter()
          .append('g')
          .attr('class', (d, i) => 'circlegroup' + i)
          .style('fill', (d, i) => d3.hsl([colorArr[i]]))
          .style('stroke', (d, i) => d3.hsl([colorArr[i]]).brighter())
          .selectAll('circle')
          .data((d, index) => d.value)
          .enter()
          .append('g')
          .attr('class', 'circle')
          .on('mouseover', (event, d) => {
            const topValue = 80;
            if (d.hoverValue) {
              div.transition()
                .duration(200)
                .style('display', 'block')
                .style('position', 'fixed')
                .style('opacity', .9);

              const circle = event.target;
              const {
                top: yPosition,
                left: xPosition
              } = circle.getBoundingClientRect();

              div.html(`${d.date || d.sSprintName}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${d.lineValue + ' ' + showUnit}` + '</span>')
                .style('left', xPosition + 20 + 'px')
                .style('top', yPosition + 20 + 'px');
              for (const hoverData in d.hoverValue) {
                div.append('p').html(`${hoverData}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${d.hoverValue[hoverData]}` + ' </span>');
              }
            }
          })
          .on('mouseout', (d) => {
            div.transition()
              .duration(500)
              .style('display', 'none')
              .style('opacity', 0);

          })
          .append('circle')
          .attr('cx', (d, i) => paddingFactor < 0.55 && data.length <= 5 && self.dataPoints === 1 ? xScale(i + 1) + barWidth / 1.5 : xScale(i + 1))
          .attr('cy', d => yScale(d.lineValue))
          .attr('r', circleRadius)
          .style('stroke-width', 1)
          .style('opacity', circleOpacity)
          .on('mouseover', function(d) {
            d3.select(this)
              .transition()
              .duration(duration)
              .attr('r', circleRadiusHover);
          })
          .on('mouseout', function(d) {
            d3.select(this)
              .transition()
              .duration(duration)
              .attr('r', circleRadius);
          });

        newRawData.forEach((element, index) => {
          d3.select(this.elem).selectAll('.circlegroup' + index).selectAll('circle').each(function(dataObj, idx) {
            const tick = d3.select(this);
            tick.attr('transform', () => 'translate(' + (x1(element.data) + barWidth / 2) + ',0)');
          });

          d3.select(this.elem).selectAll('.line-group' + index).each(function(dataObj, idx) {
            const tick = d3.select(this);
            tick.attr('transform', () => 'translate(' + (x1(element.data) + barWidth / 2) + ',0)');
          });
        });


        // line legend
        legend = svgLegend.selectAll('.d3-lineLegend')
          .data(data[0].values)
          .enter()
          .append('g')
          .attr('class', 'd3-lineLegend')
          .attr('transform', (d, i) => {
            const len = ((i + 1) * 160) + prevLength;
            return 'translate(' + (len) + ', 12)';
          });

        const legendLine = legend
          .append('svg:line')
          .attr('x1', 0)
          .attr('x2', 15)
          .attr('y1', 8)
          .attr('y2', 8)
          .style('stroke', (d, i) => color(i))
          .attr('stroke-width', '2')
          .attr('class', 'legendLine');


        legend.append('circle')
          .style('stroke', 'gray')
          .style('fill', (d, i) => color(i))
          .attr('r', 4)
          .attr('cx', 7)
          .attr('cy', 8);

        //Legend text /.attr("x", width/2 + 20)
        legend.append('text')
          .attr('x', 24)
          .attr('y', 2)
          .attr('dy', '.85em')
          .style('text-anchor', 'start')
          .style('font-size', 10)
          .text((d) => d.rate.length > 15 ? d.rate.substring(0, 12) + '...' : d.rate);

        svgLegend.append('text')
          .attr('x', 0)
          .attr('y', 15)
          .attr('dy', '.85em')
          .style('text-anchor', 'start')
          .style('font-size', 10)
          .text(self.lineLegend);
      } catch (ex) {
        console.log(ex);
      }

      const content = this.elem.querySelector('#horizontalSVG');
      content.scrollLeft += width;

      const legendIndicator = d3
        .select(this.elem)
        .select('#legendIndicator')
        .append('svg')
        .attr('height', 30)
        .attr('width', 100)
        .attr('cursor', 'pointer')
        .append('g')
        .attr('class', 'd3-legend');

      legendIndicator.append('rect')
        .attr('width', 12)
        .attr('height', 12)
        .attr('x', 32)
        .attr('y', 7)
        .style('fill', (d, i) => '#DF9292');

      legendIndicator.append('text')
        .attr('x', 52)
        .attr('y', 8)
        .attr('dy', '.85em')
        .style('text-anchor', 'start')
        .style('font-size', 10)
        .text((d) => 'Legend');


      legendIndicator
        .on('mouseover', () => {
          const topValue = 30;

          div.transition()
            .duration(200)
            .style('display', 'block')
            .style('opacity', 1)
            .style('padding', '20px 10px')
            .style('max-width', 'unset')
            .style('width', '620px');

          const htmlString = self.elem.querySelector('#svgLegend').innerHTML;

          div.html(htmlString)
            .style('left', 70 + 'px')
            .style('top', y[0] - topValue + 'px');
        })
        .on('mouseout', () => {
          div.transition()
            .duration(500)
            .style('display', 'none')
            .style('opacity', 0)
            .style('padding', '5px')
            .style('max-width', '220px')
            .style('width', 'auto');

        });
    }
  }
}
