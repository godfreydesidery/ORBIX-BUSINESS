import { Routes } from '@angular/router';
import { PagesComponent } from './pages.component';
import { UserComponent } from './identity-and-access/user/user.component';
import { RoleComponent } from './identity-and-access/role/role.component';
import { CompanyComponent } from './administration-units/company/company.component';
import { BranchComponent } from './administration-units/branch/branch.component';
import { DepartmentComponent } from './administration-units/department/department.component';
import { WarehouseComponent } from './administration-units/warehouse/warehouse.component';
import { ShopComponent } from './administration-units/shop/shop.component';
import { ShopTillComponent } from './administration-units/shop-till/shop-till.component';
import { SystemProfileComponent } from './system/system-profile/system-profile.component';
import { RoleAccessComponent } from './identity-and-access/role-access/role-access.component';
import { AuthGuard } from '../auth-guard';
import { ProductComponent } from './inventory/product/product.component';

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
        data: { breadcrumb: '' }
        //canActivate : [AuthGuard]
      },
      //Identity and Access
      {
        path : 'identity-and-access/user',
        loadComponent : () => import('./identity-and-access/identity-and-access.routes').then(c => UserComponent),
        data : { breadcrumb : 'Identity and Acces > User'},
        canActivate : [AuthGuard]
      },
      {
        path : 'identity-and-access/role',
        loadComponent : () => import('./identity-and-access/identity-and-access.routes').then(c => RoleComponent),
        data : { breadcrumb : 'Identity and Acces/Role'},
        canActivate : [AuthGuard]
      },
      {
        path : 'identity-and-access/role-access',
        loadComponent : () => import('./identity-and-access/identity-and-access.routes').then(c => RoleAccessComponent),
        data : { breadcrumb : 'Identity and Acces/Role Access'},
        canActivate : [AuthGuard]
      },


// Refactor later
      {
        path : 'management/cashier-collections',
        loadComponent: () => import('./management/cashier-collection/cashier-collection.component').then(c => c.CashierCollectionComponent),
        data : { breadcrumb : 'Management/Cash Collections'},
        canActivate : [AuthGuard]
      },
      
      //Administration Units
      {
        path : 'admin-unit/company',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => CompanyComponent),
        data : { breadcrumb : 'Admin Unit/Company'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/branch',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => BranchComponent),
        data : { breadcrumb : 'Admin Unit/Branch'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/department',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => DepartmentComponent),
        data : { breadcrumb : 'Admin Unit/Department'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/warehouse',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => WarehouseComponent),
        data : { breadcrumb : 'Admin Unit/Warehouse'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/shop',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => ShopComponent),
        data : { breadcrumb : 'Admin Unit/Shop'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/shop-till',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => ShopTillComponent),
        data : { breadcrumb : 'Admin Unit/Shop Till'},
        canActivate : [AuthGuard]
      },

      // Inventory

      {
        path : 'inventory/product',
        loadComponent: () => import('./inventory/product/product.component').then(c => c.ProductComponent),
        data : { breadcrumb : 'Product/Product'},
        canActivate : [AuthGuard]
      },
      {
        path : 'inventory/uom',
        loadComponent: () => import('./inventory/uom/uom.component').then(c => c.UomComponent),
        data : { breadcrumb : 'Product/UOM'},
        canActivate : [AuthGuard]
      },

      // Mechandizing

      {
        path : 'mechandizing/select-shop',
        loadComponent: () => import('./mechandizing/select-shop/select-shop.component').then(c => c.SelectShopComponent),
        data : { breadcrumb : 'Mechandizing/Select Shop'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/shop-product-stock-status',
        loadComponent: () => import('./mechandizing/shop-product-stock-status/shop-product-stock-status.component').then(c => c.ShopProductStockStatusComponent),
        data : { breadcrumb : 'Mechandizing/Shop Stock Status'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/import-product',
        loadComponent: () => import('./mechandizing/import-product/import-product.component').then(c => c.ImportProductComponent),
        data : { breadcrumb : 'Mechandizing/Import Product'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/shop-sales-order',
        loadComponent: () => import('./mechandizing/shop-sales-order/shop-sales-order.component').then(c => c.ShopSalesOrderComponent),
        data : { breadcrumb : 'Mechandizing/Sales Order'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/sales-listing-report',
        loadComponent: () => import('./mechandizing/reports/sales-listing-report/sales-listing-report.component').then(c => c.SalesListingReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Sales Listing Report'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/fast-moving-products-report',
        loadComponent: () => import('./mechandizing/reports/fast-moving-products-report/fast-moving-products-report.component').then(c => c.FastMovingProductsReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Fast Moving Products Report'},
        canActivate : [AuthGuard]
      },


      //System Profile
      /**Start of System Profile */
      {
        path : 'system/system-profile',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => SystemProfileComponent),
        data : { breadcrumb : 'System/System Profile'},
        canActivate : [AuthGuard]
      },
      /**End of System Profile */

      {
        path : 'blank',
        loadComponent: () => import('./blank/blank.component').then(c => c.BlankComponent),
        data : { breadcrumb : 'Blank: Please ignore'},
        canActivate : [AuthGuard]
      },

      //Parking Management
      /**Start of parking management */
      {
        path : 'parking-management/vehicle-register',
        loadComponent: () => import('./parking-management/vehicle-and-equipment-register/vehicle-and-equipment-register.component').then(c => c.VehicleEquipmentRegisterComponent),
        data : { breadcrumb : 'Parking Management/Vehicle Register'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/parking',
        loadComponent: () => import('./parking-management/vehicle-register/vehicle-register.component').then(c => c.VehicleRegisterComponent),
        data : { breadcrumb : 'Parking Management/Vehicle Register'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/parking-zone',
        loadComponent: () => import('./parking-management/parking-zone/parking-zone.component').then(c => c.ParkingZoneComponent),
        data : { breadcrumb : 'Parking Management/Parking Zone'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/vehicle-type',
        loadComponent: () => import('./parking-management/vehicle-type/vehicle-type.component').then(c => c.VehicleTypeComponent),
        data : { breadcrumb : 'Parking Management/Vehicle Type'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/vehicle-and-equipment-type',
        loadComponent: () => import('./parking-management/vehicle-and-equipment-type/vehicle-and-equipment-type.component').then(c => c.VehicleEquipmentTypeComponent),
        data : { breadcrumb : 'Parking Management/Vehicle Type'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/parking-price-plan',
        loadComponent: () => import('./parking-management/parking-price-plan/parking-price-plan.component').then(c => c.ParkingPricePlanComponent),
        data : { breadcrumb : 'Parking Management/Parking Price Plan'},
        canActivate : [AuthGuard]
      },
      {
        path : 'parking-management/reports/parking-report',
        loadComponent: () => import('./parking-management/reports/parking-report/parking-report.component').then(c => c.ParkingReportComponent),
        data : { breadcrumb : 'Parking Management/Reports/Parking Report'},
        canActivate : [AuthGuard]
      },

      /**End of Parking Management */


      {
        path : 'accounts-and-finance/invoices/receivable-invoice-list',
        loadComponent: () => import('./accounts-and-finance/invoices/receivable-invoice-list/receivable-invoice-list.component').then(c => c.ReceivableInvoiceListComponent),
        data : { breadcrumb : 'Accounts & Finance/Invoices/Receivable Invoices'},
        canActivate : [AuthGuard]
      },

      {
        path : 'accounts-and-finance/invoices/parking-receivable-invoice-list',
        loadComponent: () => import('./accounts-and-finance/invoices/parking-receivable-invoice-list/parking-receivable-invoice-list.component').then(c => c.ParkingReceivableInvoiceListComponent),
        data : { breadcrumb : 'Accounts & Finance/Invoices/Parking Receivable Invoices'},
        canActivate : [AuthGuard]
      },

      {
        path : 'accounts-and-finance/invoices/receivable-invoice',
        loadComponent: () => import('./accounts-and-finance/invoices/receivable-invoice/receivable-invoice.component').then(c => c.ReceivableInvoiceComponent),
        data : { breadcrumb : 'Accounts & Finance/Invoices/Receivable Invoice'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/parking-billing',
        loadComponent: () => import('./accounts-and-finance/parking-billing/parking-billing.component').then(c => c.ParkingBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Parking Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/vehicle-equipment-billing',
        loadComponent: () => import('./accounts-and-finance/vehicle-equipment-billing/vehicle-equipment-billing.component').then(c => c.VehicleEquipmentBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Vehicle & Equipment Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/release-vehicle-equipment',
        loadComponent: () => import('./accounts-and-finance/release-vehicle-equipment/release-vehicle-equipment.component').then(c => c.ReleaseVehicleEquipmentComponent),
        data : { breadcrumb : 'Accounts & Finance | Release Vehicle & Equipment'},
        canActivate : [AuthGuard]
      },

      // Management

      {
        path : 'management/management-board',
        loadComponent: () => import('./management/management-board/management-board.component').then(c => c.ManagementBoardComponent),
        data : { breadcrumb : 'Management/Management Board'},
        canActivate : [AuthGuard]
      },

      {
        path : 'management/cash-collections',
        loadComponent: () => import('./management/cash-collection/cash-collection.component').then(c => c.CashCollectionComponent),
        data : { breadcrumb : 'Management/Cash Collections'},
        canActivate : [AuthGuard]
      },
      {
        path : 'management/vehicle-registration-report',
        loadComponent: () => import('./management/vehicle-registration-report/vehicle-registration-report.component').then(c => c.VehicleRegistrationReportComponent),
        data : { breadcrumb : 'Management/Vehicle Registration Report'}, 
        canActivate : [AuthGuard]
      },

      


      // End of Management


      // Procurement
      {
        path : 'procurement/suppliers',
        loadComponent: () => import('./procurement/supplier/supplier.component').then(c => c.SupplierComponent),
        data : { breadcrumb : 'Procurement/Supplier'}, 
        canActivate : [AuthGuard]
      },
      {
        path : 'procurement/supplier-product-list',
        loadComponent: () => import('./procurement/supplier-product-list/supplier-product-list.component').then(c => c.SupplierProductListComponent),
        data : { breadcrumb : 'Procurement/Supplier Product List'}, 
        canActivate : [AuthGuard]
      },

      // End of Procurement



      { 
        path: 'blank', 
        loadComponent: () => import('./blank/blank.component').then(c => c.BlankComponent),
        data: { breadcrumb: 'Blank page'}
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