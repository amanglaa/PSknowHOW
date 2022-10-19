import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { faShareSquare } from '@fortawesome/free-solid-svg-icons';
import { SharedService } from 'src/app/services/shared.service';
@Component({
  selector: 'app-kpi-card',
  templateUrl: './kpi-card.component.html',
  styleUrls: ['./kpi-card.component.css']
})
export class KpiCardComponent implements OnInit, OnDestroy {
  @Input() kpiData: any;
  @Input() trendData: any;
  @Output() downloadExcel = new EventEmitter<boolean>();
  @Input() dropdownArr: any;
  @Output() optionSelected = new EventEmitter<any>();
  faShareSquare = faShareSquare;
  isTooltip: boolean = false;
  filterTooltip: boolean = false;
  @Input() trendBoxColorObj: any;
  subscriptions: any[] = [];
  filterOption: string = 'Overall';
  filterOptions: object = {};
  radioOption: string;
  filterMultiSelectOptionsData: object = {};
  kpiSelectedFilterObj: any = {};
  lhs: any = '';
  rhs: any = '';
  @Input() isShow?: any;
  @Input() showExport: boolean;
  @Input() showTrendIndicator: boolean =true;
  @Input() showChartView: boolean = true;

  constructor(private service: SharedService) {
  }
  ngOnInit(): void {
    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
      if (Object.keys(x)?.length > 0) {
        this.kpiSelectedFilterObj = JSON.parse(JSON.stringify(x));
        for (let key in x[this.kpiData?.kpiId]) {
          if (x[this.kpiData?.kpiId][key]?.includes('Overall')) {
            this.filterOptions = {};
            this.filterOption = 'Overall';
          } else {
            this.filterOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton') {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0]
          }
        }
      }
    }));
    /** assign 1st value to radio button by default */
    if(this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length > 0){
      this.radioOption = this.dropdownArr[0]?.options[0];
    }
    this.lhs = this.kpiData?.kpiDetail?.trendCalculation?.length > 0 ? this.kpiData?.kpiDetail?.trendCalculation[0].lhs : '';
    this.rhs = this.kpiData?.kpiDetail?.trendCalculation?.length > 0 ? this.kpiData?.kpiDetail?.trendCalculation[0].rhs : '';
  }

  exportToExcel() {
    this.downloadExcel.emit(true)
  }

  checkMaturity(item) {
    let maturity = item.maturity;
    if (maturity == undefined) {
      return undefined;
    }
    if (item.value.length >= 5) {
      let last5ArrItems = item.value.slice(item.value.length - 5, item.value.length);
      let tempArr = last5ArrItems.filter(x => x.data != 0);
      if (tempArr.length == 0) {
        maturity = 0;
      }
    } else {
      maturity = 0;
    }
    return maturity;
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  handleChange(type, value) {
    if (value && type?.toLowerCase() == 'radio') {
      this.optionSelected.emit(value);
    } else if (type?.toLowerCase() == 'single') {
      let obj = {
        'filter1': []
      }
      obj['filter1'].push(this.filterOption);
      this.optionSelected.emit(obj);
    } else {
      if (this.filterOptions && Object.keys(this.filterOptions)?.length == 0) {
        this.optionSelected.emit(['Overall']);
      } else {
        this.optionSelected.emit(this.filterOptions);
      }
      // this.showFilterTooltip(true);
    }
  }
  getColor(nodeName) {
    let color = '';
    for (let key in this.trendBoxColorObj) {
      if (this.trendBoxColorObj[key]?.nodeName == nodeName) {
        color = this.trendBoxColorObj[key]?.color;
      }
    }
    return color;
  }
  handleClearAll(event) {
    for (let key in this.filterOptions) {
      if (key?.toLowerCase() == event?.toLowerCase()) {
        delete this.filterOptions[key];
      }
    }
  }

  showFilterTooltip(showHide, filterNo?) {
    if (showHide) {
      this.filterMultiSelectOptionsData['details'] = {};
      this.filterMultiSelectOptionsData['details'][filterNo] = [];
      for (let i = 0; i < this.filterOptions[filterNo]?.length; i++) {

        this.filterMultiSelectOptionsData['details'][filterNo]?.push(
          {
            type: "paragraph",
            value: this.filterOptions[filterNo][i]
          }
        )
      }

    } else {
      this.filterMultiSelectOptionsData = {};
    }
  }

  ngOnDestroy() {
    this.kpiData = {};
    this.trendData = {};
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
