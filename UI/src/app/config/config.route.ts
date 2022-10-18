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

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigComponent } from './config.component';
import { UploadComponent } from './upload/upload.component';
import { DashboardconfigComponent } from './dashboard-config/dashboard-config.component';
import { AdvancedSettingsComponent } from './advanced-settings/advanced-settings.component';
import { AccessGuard } from '../services/access.guard';
import { GuestGuard } from '../services/guest.guard';

export const ConfigRoutes: Routes = [
    {
        path: '',
        component: ConfigComponent,
        children: [
            // {
            //     path: '',
            //     redirectTo: 'ProjectConfig',
            //     pathMatch: 'full'
            // },
            {
                path: '',
                loadChildren: './project-config/project-config.module#ProjectConfigModule',
                canActivate: [GuestGuard]
                // loadChildren: () => import('./profile/profile.module#ProfileModule').then(m => m.LazyModule)
            },
            {
                path: 'Upload',
                component: UploadComponent,
                canActivate: [GuestGuard]
            },
            {
                path: 'Dashboardconfig',
                component: DashboardconfigComponent,
                canActivate: [AccessGuard && GuestGuard]
            }
            ,
            {
                path: 'Profile',
                loadChildren: './profile/profile.module#ProfileModule',
                canActivate: [GuestGuard]
                // loadChildren: () => import('./profile/profile.module#ProfileModule').then(m => m.LazyModule)
            },
            {
                path: 'AdvancedSettings',
                component: AdvancedSettingsComponent,
                canActivate: [AccessGuard  && GuestGuard]
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(ConfigRoutes)],
    exports: [RouterModule],
    providers: [
        AccessGuard
    ]
})

export class ConfigRoutingModule { }
