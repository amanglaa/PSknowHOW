import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { KpiCardComponent } from './kpi-card.component';
import { SharedService } from 'src/app/services/shared.service';

describe('KpiCardComponent', () => {
  let component: KpiCardComponent;
  let fixture: ComponentFixture<KpiCardComponent>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiCardComponent ],
      providers: [SharedService]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should showFilterTooltip',()=>{
    component.filterOptions ={
      1:['Overall','P1']
    };
    component.showFilterTooltip(true,1);
    expect(component.filterMultiSelectOptionsData['details'][1].length).toEqual( component.filterOptions[1].length);
    component.showFilterTooltip(false);
    expect(Object.keys(component.filterMultiSelectOptionsData).length).toEqual(0);
  });

  it('should handle clear all filter',()=>{
    component.filterOptions={
      filter1:[],
      filter2:[]
    }
    component.handleClearAll('filter1');
    expect(component.filterOptions.hasOwnProperty('filter1')).toBeFalsy();
  });

  it('should return color based on nodename',()=>{
    component.trendBoxColorObj ={
      "C1_corporate": {
          "nodeName": "C1",
          "color": "#079FFF"
      },
      "C1": {
          "nodeName": "C1",
          "color": "#079FFF"
      }
  };
  expect(component.getColor('C1')).toBe('#079FFF');
  });

  it('sholud handle filter change for radio',()=>{
    let spy = spyOn(component.optionSelected,'emit');
    component.handleChange('radio','Story Points');
    expect(spy).toHaveBeenCalledWith('Story Points');
  });

  it('sholud handle filter change for single select',()=>{
    let filterOptionsingle = {
      'filter1': [{}]
    };
    component.filterOptions ={};
    let spy = spyOn(component.optionSelected,'emit');
    component.handleChange('single',undefined);
    expect(spy).toHaveBeenCalled();
  });

  it('sholud handle filter change for multi select',()=>{
    let filterOptionMulti ={filter1 : ['P1','P2']};
    component.filterOptions = filterOptionMulti;
    let spy = spyOn(component.optionSelected,'emit');
    component.handleChange('multi',undefined);
    expect(spy).toHaveBeenCalledWith(filterOptionMulti);
  });

  it('should show tooltip',()=>{
    component.showTooltip(true);
    expect(component.isTooltip).toBeTrue();
  });

  it('should return valid maturity value', () => {
    let item = {
      "data": "EU",
      "value": [
        {
          "data": "2",
          "value": 2,
          "hoverValue": {},
          "date": "2022-08-29 to 2022-09-04",
          "sprojectName": "Aarti Cons",
          "xName": 1
        },
        {
          "data": "5",
          "value": 5,
          "hoverValue": {},
          "date": "2022-09-05 to 2022-09-11",
          "sprojectName": "Aarti Cons",
          "xName": 2
        },
        {
          "data": "11",
          "value": 11,
          "hoverValue": {},
          "date": "2022-09-12 to 2022-09-18",
          "sprojectName": "Aarti Cons",
          "xName": 3
        },
        {
          "data": "2",
          "value": 2,
          "hoverValue": {},
          "date": "2022-09-19 to 2022-09-25",
          "sprojectName": "Aarti Cons",
          "xName": 4
        },
        {
          "data": "4",
          "value": 4,
          "hoverValue": {},
          "date": "2022-09-26 to 2022-10-02",
          "sprojectName": "Aarti Cons",
          "xName": 5
        }
      ],
      "maturity": "3"
    };
    expect(component.checkMaturity(item)).toBe('3');

    let item1 = {
      "data": "EU",
      "value": [],
      "maturity": "5"
    }
    expect(component.checkMaturity(item1)).toBe(0);
  });


  it('should set filter default option', fakeAsync(() => {
    let response = {
      "kpi113": [
        "Overall"
      ]
    };

    component.kpiData = {
      "kpiId": "kpi113",
      "kpiName": "Value delivered (Cost of Delay)",
      "isEnabled": true,
      "order": 28,
      "kpiDetail": {
        "id": "633ed17f2c2d5abef2451ff3",
        "kpiId": "kpi113",
      },
      "shown": true
    };
    sharedService.setKpiSubFilterObj(response);
    component.ngOnInit();
    tick();
    expect(component.filterOption).toBe('Overall');
  }));

  it('should set default filter value for kpi having radiobutton filter', fakeAsync(() => {
    component.kpiData = {
      "kpiId": "kpi3",
      "kpiName": "Lead Time",
      "isEnabled": true,
      "order": 25,
      "kpiDetail": {
        "id": "633ed17f2c2d5abef2451ff0",
        "kpiId": "kpi3",
        "kpiName": "Lead Time",
        "kpiSource": "Jira",
        "kanban": false,
        "kpiFilter": "radioButton",
      },
      "shown": true
    }

    let response = { "kpi3": ['default'] };
    sharedService.setKpiSubFilterObj(response);
    component.ngOnInit();
    tick();
    expect(component.radioOption).toBe('default');
  }));
});
