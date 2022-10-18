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

import { Component, OnInit } from '@angular/core';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { Router } from '@angular/router';

declare var $: any;

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    isSuperAdmin = false;
    isProjectAdmin = false;
    changePswdDisabled = false;
    adLogin = false;
    constructor(private getAuthorizationService: GetAuthorizationService, public router: Router) {
    }
    ngOnInit() {
        if (this.getAuthorizationService.checkIfSuperUser()) {
            // logged in as SuperAdmin
            this.isSuperAdmin = true;
        }

        if(this.getAuthorizationService.checkIfProjectAdmin()) {
            this.isProjectAdmin = true;
        }

        if (!localStorage.getItem('user_email')) {
            this.changePswdDisabled = true;
        }

        this.adLogin = localStorage.loginType === 'AD';
    }

}
