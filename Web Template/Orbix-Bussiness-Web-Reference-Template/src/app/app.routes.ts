import { Routes } from '@angular/router';

export const routes: Routes = [
    { 
        path: '', 
        redirectTo: 'pages', 
        pathMatch: 'full' 
    },
    {
        path: 'pages', 
        loadChildren: () => import('./pages/pages.routes').then(p => p.routes)
    }, 
    { 
        path: 'login', 
        loadComponent: () => import('./pages/login/login.component').then(c => c.LoginComponent),
    },
    { 
        path: 'register', 
        loadComponent: () => import('./pages/register/register.component').then(c => c.RegisterComponent),
    }, 
    { 
        path: '**', 
        loadComponent: () => import('./pages/error/error.component').then(c => c.ErrorComponent)  
    }
];