import { Routes } from "@angular/router";
import { InputsComponent } from "./inputs/inputs.component";
import { LayoutsComponent } from "./layouts/layouts.component";
import { ValidationsComponent } from "./validations/validations.component";
import { WizardComponent } from "./wizard/wizard.component";

export const routes: Routes = [
    { path: '', redirectTo: 'inputs', pathMatch: 'full' },
    { path: 'inputs', component: InputsComponent, data: { breadcrumb: 'Inputs' } },
    { path: 'layouts', component: LayoutsComponent, data: { breadcrumb: 'Layouts' } },
    { path: 'validations', component: ValidationsComponent, data: { breadcrumb: 'Validations' } },
    { path: 'wizard', component: WizardComponent, data: { breadcrumb: 'Wizard' } }
];