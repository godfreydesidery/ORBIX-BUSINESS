import { Routes } from "@angular/router"; 
import { BasicTablesComponent } from "./basic-tables/basic-tables.component";
import { DynamicTablesComponent } from "./dynamic-tables/dynamic-tables.component";

export const routes: Routes = [
    { path: '', redirectTo: 'basic-tables', pathMatch: 'full' },
    { path: 'basic-tables', component: BasicTablesComponent, data: { breadcrumb: 'Basic' } },
    { path: 'dynamic-tables', component: DynamicTablesComponent, data: { breadcrumb: 'Dynamic' } }
];