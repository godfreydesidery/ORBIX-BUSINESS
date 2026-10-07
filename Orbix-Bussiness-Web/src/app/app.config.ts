import 'pace';
import { ApplicationConfig, importProvidersFrom, NgModule, provideZoneChangeDetection } from '@angular/core';
import { PreloadAllModules, provideRouter, RouterModule, withPreloading } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { CommonModule, DatePipe } from '@angular/common';
import { loaderInterceptor } from '@services/loader.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    DatePipe,
    NgModule,
    CommonModule,
    provideHttpClient(withInterceptors([loaderInterceptor])),
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(
      routes,
      withPreloading(PreloadAllModules),  // comment this line for enable lazy-loading
    ),
    importProvidersFrom([
      BrowserAnimationsModule,
      ToastrModule.forRoot(), 
      RouterModule.forRoot(routes, {useHash : true})
    ])
  ],
};


