import { Routes } from '@angular/router';
import { PagesComponent } from './pages.component';

export const routes: Routes = [
  {
    path: '',
    component: PagesComponent,
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component').then(c => c.DashboardComponent),
        data: { breadcrumb: 'Dashboard' }
      },
      { 
        path: 'blank', 
        loadComponent: () => import('./blank/blank.component').then(c => c.BlankComponent),
        data: { breadcrumb: 'Blank page' } 
      },
      { 
        path: 'search', 
        loadComponent: () => import('./search/search.component').then(c => c.SearchComponent),
        data: { breadcrumb: 'Search' } 
      },
      {
        path: 'maps',
        loadChildren: () => import('./maps/maps.routes').then(p => p.routes),
        data: { breadcrumb: 'Maps' }
      },
      { 
        path: 'charts', 
        loadComponent: () => import('./charts/charts.component').then(c => c.ChartsComponent),
        data: { breadcrumb: 'Charts' } 
      },
      {
        path: 'ui',
        loadChildren: () => import('./ui/ui.routes').then(p => p.routes),
        data: { breadcrumb: 'UI' }
      },
      {
        path: 'tools',
        loadChildren: () => import('./tools/tools.routes').then(p => p.routes),
        data: { breadcrumb: 'Tools' }
      },
      {
        path: 'mail',
        loadChildren: () => import('./mail/mail.routes').then(p => p.routes),
        data: { breadcrumb: 'Mail' }
      },
      { 
        path: 'calendar', 
        loadComponent: () => import('./calendar/calendar.component').then(c => c.CalendarComponent),
        data: { breadcrumb: 'Calendar' } 
      },
      {
        path: 'form-elements',
        loadChildren: () => import('./form-elements/form-elements.routes').then(p => p.routes),
        data: { breadcrumb: 'Form Elements' }
      },
      {
        path: 'tables',
        loadChildren: () => import('./tables/tables.routes').then(p => p.routes),
        data: { breadcrumb: 'Tables' }
      },
      {
        path: 'editors',
        loadChildren: () => import('./editors/editors.routes').then(p => p.routes),
        data: { breadcrumb: 'Editors' }
      },
      {
        path: 'profile',
        loadChildren: () => import('./profile/profile.routes').then(p => p.routes),
        data: { breadcrumb: 'Profile' }
      },
    ]
  }
]  