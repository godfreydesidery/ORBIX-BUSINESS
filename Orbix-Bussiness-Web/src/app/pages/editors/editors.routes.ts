import { Routes } from "@angular/router";
import { CkeditorComponent } from "./ckeditor/ckeditor.component";

export const routes: Routes = [
    { path: '', redirectTo: 'ckeditor', pathMatch: 'full' },
    { path: 'ckeditor', component: CkeditorComponent, data: { breadcrumb: 'Ckeditor' } }
];