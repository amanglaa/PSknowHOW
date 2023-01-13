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
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewContainerRef,
} from '@angular/core';

import * as d3 from 'd3';

@Component({
  selector: 'app-piechart',
  templateUrl: './piechart.component.html',
  styleUrls: ['./piechart.component.css'],
})
export class PiechartComponent implements OnChanges, OnInit {
  @Input() data: any; // json data

  
  private svg: any;
  private margin = 20;
  private width = 400;
  private height = 400;
  // The radius of the pie chart is half the smallest side
  private radius = Math.min(this.width, this.height) / 2 - this.margin;
  private colors;

  elem;

  constructor(private viewContainerRef: ViewContainerRef) {}

  private createSvg(): void {
    this.svg = d3
      .select('figure#pie')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr(
        'transform',
        'translate(' + this.width / 2 + ',' + this.height / 2 + ')',
      );
  }
  //d.Stars.toString()
  private createColors(): void {
    this.colors = d3
      .scaleOrdinal()
      .domain(this.data[0].data.map((d) => d.value.toString()))
      .range(['#c7d3ec', '#a5b8db', '#879cc4', '#677795', '#5a6782']);
  }
  private drawChart(): void {
    // Compute the position of each group on the pie:
    const pie = d3.pie<any>().value((d: any) => Number(d.value));

    // Build the pie chart
    this.svg
      .selectAll('pieces')
      .data(pie(this.data[0].data))
      .enter()
      .append('path')
      .attr('d', d3.arc().innerRadius(0).outerRadius(this.radius))
      .attr('fill', (d: any, i: any) => this.colors(i))
      .attr('stroke', '#121926')
      .style('stroke-width', '1px');

    // Add labels
    const labelLocation = d3.arc().innerRadius(100).outerRadius(this.radius);

    this.svg
      .selectAll('pieces')
      .data(pie(this.data[0].data))
      .enter()
      .append('text')
      .text((d: any) => `${d.data.label} (${d.data.value})`)
      .attr(
        'transform',
        (d: any) => 'translate(' + labelLocation.centroid(d) + ')',
      )
      .style('text-anchor', 'middle')
      .style('font-size', 14);
  }
  ngOnInit(): void {
    this.createSvg();
    this.createColors();
    this.drawChart();
  }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
  }
}
