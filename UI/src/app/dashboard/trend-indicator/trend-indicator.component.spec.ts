import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendIndicatorComponent } from './trend-indicator.component';

describe('TrendIndicatorComponent', () => {
  let component: TrendIndicatorComponent;
  let fixture: ComponentFixture<TrendIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrendIndicatorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrendIndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.isTrendObject).toBeFalse();
  });

  it('should calculate datatrend and lastValue',()=>{
    component.dataTrend =[549,1177];
    component.kpiData = {
      kpiId: 'kpi997',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 3,
      kpiDetail: {
          id: '63355d7c41a0342c3790fb9e',
          kpiId: 'kpi113',
          kpiName: 'Value delivered (Cost of Delay)',
          isDeleted: 'False',
          defaultOrder: 28,
          kpiUnit: '',
          chartType: 'line',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: false,
          kpiSource: 'Jira',
          maxValue: '300',
          kanban: false,
          groupId: 4,
          kpiInfo: {
              definition: 'Cost of delay (CoD) is a prioritization framework that helps a business quantify the economic value of completing a project sooner as opposed to later.',
              formula: [
                  {
                      lhs: 'COD for a Epic or a Feature',
                      rhs: 'User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement.'
                  }
              ],
              details: [
                  {
                      type: 'paragraph',
                      value: 'It is calculated in ‘Days’. Higher the CoD for a feature or an Epic, more valuable it is for the Business or a Project'
                  },
                  {
                      type: 'paragraph',
                      value: 'A progress indicator shows trend of CoD between last 2 months. An upward trend is considered positive'
                  }
              ]
          },
          aggregationCriteria: 'sum',
          xaxisLabel: 'Months',
          yaxisLabel: 'Count(Days)',
          trendCalculative: false,
          additionalFilterSupport: false
      },
      shown: true
  };
  component.ngOnInit();
  expect(component.isTrendObject).toBeFalse();
  expect(component.dataTrend[0]).toEqual('NA');
  expect(component.lastValue).toEqual('NA');
  });

  it('should calculate datatrend and lastValue when chart type is stackedColumn',()=>{
    component.dataTrend =[549,1177];
    component.kpiData = {
      kpiId: 'kpi114',
      kpiName: 'Value delivered (Cost of Delay)',
      isEnabled: true,
      order: 3,
      kpiDetail: {
          id: '63355d7c41a0342c3790fb9e',
          kpiId: 'kpi113',
          kpiName: 'Value delivered (Cost of Delay)',
          isDeleted: 'False',
          defaultOrder: 28,
          kpiUnit: '',
          chartType: 'stackedColumn',
          showTrend: true,
          isPositiveTrend: true,
          calculateMaturity: false,
          kpiSource: 'Jira',
          maxValue: '300',
          kanban: false,
          groupId: 4,
          kpiInfo: {
              definition: 'Cost of delay (CoD) is a prioritization framework that helps a business quantify the economic value of completing a project sooner as opposed to later.',
              formula: [
                  {
                      lhs: 'COD for a Epic or a Feature',
                      rhs: 'User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement.'
                  }
              ],
              details: [
                  {
                      type: 'paragraph',
                      value: 'It is calculated in ‘Days’. Higher the CoD for a feature or an Epic, more valuable it is for the Business or a Project'
                  },
                  {
                      type: 'paragraph',
                      value: 'A progress indicator shows trend of CoD between last 2 months. An upward trend is considered positive'
                  }
              ]
          },
          aggregationCriteria: 'sum',
          xaxisLabel: 'Months',
          yaxisLabel: 'Count(Days)',
          trendCalculative: false,
          additionalFilterSupport: false
      },
      shown: true
  };
  component.ngOnInit();
  expect(component.dataTrend[0]).toEqual(0);
  expect(component.lastValue).toEqual(0);

  });
});
