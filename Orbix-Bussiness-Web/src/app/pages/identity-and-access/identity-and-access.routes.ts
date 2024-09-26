import { Routes } from "@angular/router";
import { UserComponent } from "./user/user.component";
import { RoleComponent } from "./role/role.component";

export const routes : Routes = [
    {path : '', redirectTo : '', pathMatch : 'full'},
    {path : 'identity-and-access/user', component : UserComponent, data : {breadcrumb : 'User'}},
    {path : 'identity-and-access/role', component : RoleComponent, data : {breadcrumb : 'Role'}}
]