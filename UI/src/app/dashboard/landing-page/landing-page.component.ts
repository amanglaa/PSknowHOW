import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { HttpService } from 'src/app/services/http.service';

declare var require: any

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})

export class LandingPageComponent implements OnInit {
  selectedValue: string = '';
  overallSummary: any;
  area;
  landingInfo;
  voiceForm = new FormGroup({
    feedbackType: new FormControl('', Validators.required),
    category: new FormControl('', Validators.required),
    feedback: new FormControl('', {validators:[Validators.required, Validators.maxLength(600)]})
  });
  isFeedbackSubmitted: boolean = false;
  formMessage: string = '';
  verticalArray: Array<string> = [];
  summaryItems: any = [];
  tableHeadingArr: any = [];
  totalsArray: any = [];
  totalProjects: any = 0;
  totalProjects30Days: any = 0;
  totalUsers:any = 0;
  newUsers:any = 0;
  isProducer: Boolean = false;

  constructor(private httpService: HttpService) { }

  ngOnInit(): void {
    
    // this.getRoles();
    this.getImpInfo();
    this.getCategory();
    // this.getAccountSummary();
    // this.getTotalUsersCount();
  }

  // handleChange(e) {
  //   if (e.checked) {
  //     this.getAccountSummary('29');
  //   } else {
  //     this.getAccountSummary();
  //   }
  // }

  getImpInfo(){
    this.httpService.getLandingInfo().subscribe((response) =>  {
      if(response.data){
        this.landingInfo = response.data;
      }
    }, error => {
      this.landingInfo = require('../../../test/resource/fakeLandingInfo.json').data;
    })
  }

  save() {
    let postObj = this.voiceForm.value;
    postObj['username'] = localStorage.getItem('user_name');
    this.httpService.submitFeedbackData(postObj).subscribe((response) => {
      if(response.message){
        this.isFeedbackSubmitted = true;
        this.formMessage = response.message;
        setTimeout(() => {
          this.formMessage = '';
        }, 3000);
        this.voiceForm.reset();
      }
    }, error => {
      console.log(error);
      this.isFeedbackSubmitted = false;
      setTimeout(() => {
        this.formMessage = '';
      }, 3000);
    })
  }

  getCategory() {
    this.httpService.getFeedbackCategory().subscribe((response) => {
      if(response.data){
        this.area = response.data;
      }
    }, error => {
      console.log(error);
    })
  }

  getTotalUsersCount(){
    this.httpService.getUsersCount().subscribe((response)=>{
      if(response.data){
        this.totalUsers = response.data['Total Users'];
        this.newUsers = response.data['New Users Added in last 30 days'];
      }
    }, error=>{
      console.log(error);
    })
  }

}
