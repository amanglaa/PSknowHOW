import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { AdSettingsComponent } from './ad-settings.component';
import { InputNumberModule } from 'primeng/inputnumber';
import { environment } from 'src/environments/environment';

describe('AdSettingsComponent', () => {
  let component: AdSettingsComponent;
  let fixture: ComponentFixture<AdSettingsComponent>;
  let httpService: HttpService;
  const baseUrl = environment.baseUrl;
  let httpMock;

  const fakeADSettings = {
    "message": "Sucessfully fetch the active directory user ",
    "success": true,
    "data": {
      "username": "test-username",
      "host": "test-host-name",
      "port": 639,
      "rootDn": "test-root",
      "domain": "testdomain.net"
    }
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdSettingsComponent],
      imports: [FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        InputNumberModule
      ],
      providers: [
        HttpService,
        MessageService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdSettingsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get AD config on load', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/activedirectory')[0].flush(fakeADSettings);
    expect(component.adSettingsForm.controls['domain'].value).toEqual(fakeADSettings['data']['domain']);
  });

  it('should submit AD config', () => {
    component.ngOnInit();
    fixture.detectChanges();
    for (const obj in fakeADSettings['data']) {
      if (obj !== 'password') {
        if (component.adSettingsForm && component.adSettingsForm.controls[obj]) {
          component.adSettingsForm.controls[obj].setValue(fakeADSettings['data'][obj]);
        }
      }
    }
    component.adSettingsForm.controls['password'].setValue('testPassword');
    expect(component.adSettingsForm.valid).toBeTrue();
    let fakeSubmitResponse = {
      "message": "created and updated active directory user",
      "success": true,
      "data": {
        "username": "test-username",
        "password": "8HvZjOM5y5T2c6ROq5CN7Z/IaAk0Q/cuubrN9sPOXmWdzEwlwNu9i48pHMPuvbAH",
        "host": "test-host-name",
        "port": 639,
        "rootDn": "test-root",
        "domain": "testdomain.net"
      }
    }
    component.submit();
    httpMock.match(baseUrl + '/api/activedirectory')[0].flush(fakeSubmitResponse);
  });
});
