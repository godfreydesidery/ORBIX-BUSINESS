import { Routes } from "@angular/router";
import { UserComponent } from "./user/user.component";
import { RoleComponent } from "./role/role.component";

export const routes : Routes = [
    {path : '', redirectTo : 'user', pathMatch : 'full'},
    {path : 'user', component : UserComponent, data : {breadcrumb : 'User'}},
    {path : 'role', component : RoleComponent, data : {breadcrumb : 'Role'}}
]