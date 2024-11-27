import { Routes } from "@angular/router";
import { CompanyComponent } from "./company/company.component";
import { BranchComponent } from "./branch/branch.component";
import { DepartmentComponent } from "./department/department.component";
import { WarehouseComponent } from "./warehouse/warehouse.component";
import { ShopComponent } from "./shop/shop.component";
import { ShopTillComponent } from "./shop-till/shop-till.component";

export const routes : Routes = [
    {path : '', redirectTo : '', pathMatch : 'full'},
    //{path : 'admin-units/company', component : CompanyComponent, data : {breadcrumb : 'Admin Unit/Company'}},
    //{path : 'administration-unit/branch', component : BranchComponent, data : {breadcrumb : 'Admin Unit/Branch'}},
    //{path : 'administration-unit/department', component : DepartmentComponent, data : {breadcrumb : 'Admin Unit/Department'}},
    //{path : 'administration-unit/warehouse', component : WarehouseComponent, data : {breadcrumb : 'Admin Unit/Warehouse'}},
    //{path : 'administration-unit/shop', component : ShopComponent, data : {breadcrumb : 'Admin Unit/Shop'}},
    //{path : 'administration-unit/shop-till', component : ShopTillComponent, data : {breadcrumb : 'Admin Unit/Shop Till'}},
]