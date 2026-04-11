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
import { RestaurantComponent } from './administration-units/restaurant/restaurant.component';
import { WorkshopComponent } from './administration-units/workshop/workshop.component';
import { version } from 'moment';
import { CurrencyConversionComponent } from './administration-units/currency-conversion/currency-conversion.component';

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
        path : 'admin-unit/currency-conversion',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => CurrencyConversionComponent),
        data : { breadcrumb : 'Admin Unit/Currency Conversion'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/workshop',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => WorkshopComponent),
        data : { breadcrumb : 'Admin Unit/Workshop'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/shop-till',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => ShopTillComponent),
        data : { breadcrumb : 'Admin Unit/Shop Till'},
        canActivate : [AuthGuard]
      },
      {
        path : 'admin-unit/restaurant',
        loadComponent : () => import('./administration-units/administration-units.routes').then(c => RestaurantComponent),
        data : { breadcrumb : 'Admin Unit/Restaurant'},
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
        path : 'inventory/service',
        loadComponent: () => import('./inventory/service/service.component').then(c => c.ServiceComponent),
        data : { breadcrumb : 'Service/Service'},
        canActivate : [AuthGuard]
      },
      {
        path : 'inventory/dineable',
        loadComponent: () => import('./inventory/dineable/dineable.component').then(c => c.DineableComponent),
        data : { breadcrumb : 'Dineable/Dineable'},
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
        path : 'mechandizing/select-restaurant',
        loadComponent: () => import('./mechandizing/select-restaurant/select-restaurant.component').then(c => c.SelectRestaurantComponent),
        data : { breadcrumb : 'Mechandizing/Select Restaurant'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-dineable-stock-status',
        loadComponent: () => import('./mechandizing/restaurant-dineable-stock-status/restaurant-dineable-stock-status.component').then(c => c.RestaurantDineableStockStatusComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Dineable Stock Status'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-dineable-product',
        loadComponent: () => import('./mechandizing/restaurant-dineable-product/restaurant-dineable-product.component').then(c => c.RestaurantDineableProductComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Dineable Product'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/shop-product-stock-status',
        loadComponent: () => import('./mechandizing/shop-product-stock-status/shop-product-stock-status.component').then(c => c.ShopProductStockStatusComponent),
        data : { breadcrumb : 'Mechandizing/Shop Stock Status'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-product-stock-status',
        loadComponent: () => import('./mechandizing/restaurant-product-stock-status/restaurant-product-stock-status.component').then(c => c.RestaurantProductStockStatusComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Stock Status'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/shop-product-stock-status-out',
        loadComponent: () => import('./mechandizing/shop-product-stock-status-out/shop-product-stock-status-out.component').then(c => c.ShopProductStockStatusOutComponent),
        data : { breadcrumb : 'Mechandizing/Shop Stock Status(Out of stock)'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/shop-product-stock-status-under',
        loadComponent: () => import('./mechandizing/shop-product-stock-status-under/shop-product-stock-status-under.component').then(c => c.ShopProductStockStatusUnderComponent),
        data : { breadcrumb : 'Mechandizing/Shop Stock Status(Under stock)'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/shop-product-stock-log',
        loadComponent: () => import('./mechandizing/shop-product-stock-log/shop-product-stock-log.component').then(c => c.ShopProductStockLogComponent),
        data : { breadcrumb : 'Mechandizing/Shop Stock Card'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/restaurant-product-stock-log',
        loadComponent: () => import('./mechandizing/restaurant-product-stock-log/restaurant-product-stock-log.component').then(c => c.RestaurantProductStockLogComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Stock Card'},
        canActivate : [AuthGuard]
      },



      {
        path : 'mechandizing/import-product',
        loadComponent: () => import('./mechandizing/import-product/import-product.component').then(c => c.ImportProductComponent),
        data : { breadcrumb : 'Mechandizing/Import Product'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/import-restaurant-product',
        loadComponent: () => import('./mechandizing/import-restaurant-product/import-restaurant-product.component').then(c => c.ImportRestaurantProductComponent),
        data : { breadcrumb : 'Mechandizing/Import Restaurant Product'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/import-dineable',
        loadComponent: () => import('./mechandizing/import-dineable/import-dineable.component').then(c => c.ImportDineableComponent),
        data : { breadcrumb : 'Mechandizing/Import Dineable'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-badge',
        loadComponent: () => import('./mechandizing/restaurant-badge/restaurant-badge.component').then(c => c.RestaurantBadgeComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Badge'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-agent',
        loadComponent: () => import('./mechandizing/restaurant-agent/restaurant-agent.component').then(c => c.RestaurantAgentComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Agent'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/shop-sales-order',
        loadComponent: () => import('./mechandizing/shop-sales-order/shop-sales-order.component').then(c => c.ShopSalesOrderComponent),
        data : { breadcrumb : 'Mechandizing/Sales Order'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/restaurant-sales-order',
        loadComponent: () => import('./mechandizing/restaurant-sales-order/restaurant-sales-order.component').then(c => c.RestaurantSalesOrderComponent),
        data : { breadcrumb : 'Mechandizing/Restaurant Sales Order'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/shop-lpo',
        loadComponent: () => import('./mechandizing/shop-lpo/shop-lpo.component').then(c => c.ShopLpoComponent),
        data : { breadcrumb : 'Mechandizing/Shop LPO'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/shop-grn',
        loadComponent: () => import('./mechandizing/shop-grn/shop-grn.component').then(c => c.ShopGrnComponent),
        data : { breadcrumb : 'Mechandizing/Shop GRN'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/sales-listing-report',
        loadComponent: () => import('./mechandizing/reports/sales-listing-report/sales-listing-report.component').then(c => c.SalesListingReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Sales Listing Report'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/restaurant-sales-listing-report',
        loadComponent: () => import('./mechandizing/reports/restaurant-sales-listing-report/restaurant-sales-listing-report.component').then(c => c.RestaurantSalesListingReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Sales Listing Report'},
        canActivate : [AuthGuard]
      },
      {
        path : 'mechandizing/fast-moving-products-report',
        loadComponent: () => import('./mechandizing/reports/fast-moving-products-report/fast-moving-products-report.component').then(c => c.FastMovingProductsReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Fast Moving Products Report'},
        canActivate : [AuthGuard]
      },

      {
        path : 'mechandizing/fast-moving-dineables-report',
        loadComponent: () => import('./mechandizing/reports/fast-moving-dineables-report/fast-moving-dineables-report.component').then(c => c.FastMovingDineablesReportComponent),
        data : { breadcrumb : 'Mechandizing/Reports/Fast Moving Meals Report'},
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
      {
        path : 'parking-management/reports/vehicle-removed-report',
        loadComponent: () => import('./parking-management/reports/vehicle-equipment-removed-report/vehicle-equipment-removed-report.component').then(c => c.VehicleEquipmentRemovedReportComponent),
        data : { breadcrumb : 'Parking Management/Reports/Vehicle Archived Report'},
        canActivate : [AuthGuard]
      },

      /**End of Parking Management */

      //Maintenance Management
      /**Start of Maintenance management */
      {
        path : 'maintenance-management/maintenance',
        loadComponent: () => import('./maintenance-management/maintenance/maintenance.component').then(c => c.MaintenanceComponent),
        data : { breadcrumb : 'Maintenance Management/Maintenance'},
        canActivate : [AuthGuard]
      },
      {
        path : 'maintenance-management/maintenance-verify',
        loadComponent: () => import('./maintenance-management/maintenance-verify/maintenance-verify.component').then(c => c.MaintenanceVerifyComponent),
        data : { breadcrumb : 'Maintenance Management/Maintenance Verify'},
        canActivate : [AuthGuard]
      },
      {
        path : 'maintenance-management/maintenance-issue-type',
        loadComponent: () => import('./maintenance-management/maintenance-issue-type/maintenance-issue-type.component').then(c => c.MaintenanceIssueTypeComponent),
        data : { breadcrumb : 'Maintenance Management/Maintenance Issue Type'},
        canActivate : [AuthGuard]
      },
      {
        path : 'maintenance-management/service-specialist',
        loadComponent: () => import('./maintenance-management/service-specialist/service-specialist.component').then(c => c.ServiceSpecialistComponent),
        data : { breadcrumb : 'Maintenance Management/Service Specialist'},
        canActivate : [AuthGuard]
      },
      /**End of Maintenance Management */

      /**Workshop */


      /**End of Workshop */
      {
        path : 'workshop/my-jobs',
        loadComponent: () => import('./workshop/my-jobs/my-jobs.component').then(c => c.MyJobsComponent),
        data : { breadcrumb : 'Workshop/My Jobs'},
        canActivate : [AuthGuard]
      },
      {
        path : 'workshop/my-closed-jobs',
        loadComponent: () => import('./workshop/my-closed-jobs/my-closed-jobs.component').then(c => c.MyClosedJobsComponent),
        data : { breadcrumb : 'Workshop/Verified Jobs'},
        canActivate : [AuthGuard]
      },

      /**Storage Management */
      {
        path : 'storage-management/warehouse',
        loadComponent: () => import('./storage-management/warehouse/warehouse.component').then(c => c.WarehouseComponent),
        data : { breadcrumb : 'Storage Management/Warehouse'},
        canActivate : [AuthGuard]
      },
      {
        path : 'storage-management/good-type',
        loadComponent: () => import('./storage-management/good-type/good-type.component').then(c => c.GoodTypeComponent),
        data : { breadcrumb : 'Storage Management/Good Types'},
        canActivate : [AuthGuard]
      },

      {
        path : 'storage-management/select-warehouse',
        loadComponent: () => import('./storage-management/select-warehouse/select-warehouse.component').then(c => c.SelectWarehouseComponent),
        data : { breadcrumb : 'Warehouse/Select Warehouse'},
        canActivate : [AuthGuard]
      },
      {
        path : 'storage-management/cash-collections',
        loadComponent: () => import('./storage-management/storage-cash-collection/storage-cash-collection.component').then(c => c.StorageCashCollectionComponent),
        data : { breadcrumb : 'Warehouse/Cash Collections'},
        canActivate : [AuthGuard]
      },
      {
        path : 'storage-management/storage-report',
        loadComponent: () => import('./storage-management/reports/storage-report/storage-report.component').then(c => c.StorageReportComponent),
        data : { breadcrumb : 'Warehouse/Reports/Storage Report'},
        canActivate : [AuthGuard]
      },
      {
        path : 'storage-management/goods-removed-report',
        loadComponent: () => import('./storage-management/reports/goods-removed-report/goods-removed-report.component').then(c => c.GoodsRemovedReportComponent),
        data : { breadcrumb : 'Warehouse/Reports/Goods Removed Report'},
        canActivate : [AuthGuard]
      },

      /**Bond Management */
      {
        path : 'bond-management/bond-zone',
        loadComponent: () => import('./bond-management/bond-zone/bond-zone.component').then(c => c.BondZoneComponent),
        data : { breadcrumb : 'Bond Management/Bond Zone'},
        canActivate : [AuthGuard]
      },
      {
        path : 'bond-management/bond-item-type',
        loadComponent: () => import('./bond-management/bond-item-type/bond-item-type.component').then(c => c.BondItemTypeComponent),
        data : { breadcrumb : 'Bond Management/Vehicle Types'},
        canActivate : [AuthGuard]
      },

      {
        path : 'bond-management/select-bond-zone',
        loadComponent: () => import('./bond-management/select-bond-zone/select-bond-zone.component').then(c => c.SelectBondZoneComponent),
        data : { breadcrumb : 'Bond/Select Bond Zone'},
        canActivate : [AuthGuard]
      },
      {
        path : 'bond-management/cash-collections',
        loadComponent: () => import('./bond-management/bond-cash-collection/bond-cash-collection.component').then(c => c.BondCashCollectionComponent),
        data : { breadcrumb : 'Bond/Cash Collections'},
        canActivate : [AuthGuard]
      },
      // {
      //   path : 'storage-management/cash-collections',
      //   loadComponent: () => import('./storage-management/storage-cash-collection/storage-cash-collection.component').then(c => c.StorageCashCollectionComponent),
      //   data : { breadcrumb : 'Warehouse/Cash Collections'},
      //   canActivate : [AuthGuard]
      // },
      // {
      //   path : 'storage-management/storage-report',
      //   loadComponent: () => import('./storage-management/reports/storage-report/storage-report.component').then(c => c.StorageReportComponent),
      //   data : { breadcrumb : 'Warehouse/Reports/Storage Report'},
      //   canActivate : [AuthGuard]
      // },
      // {
      //   path : 'storage-management/goods-removed-report',
      //   loadComponent: () => import('./storage-management/reports/goods-removed-report/goods-removed-report.component').then(c => c.GoodsRemovedReportComponent),
      //   data : { breadcrumb : 'Warehouse/Reports/Goods Removed Report'},
      //   canActivate : [AuthGuard]
      // },



      /**End Storage Management */

      /**Service Bay */

      {
        path : 'service-bay/select-workshop',
        loadComponent: () => import('./service-bay/select-workshop/select-workshop.component').then(c => c.SelectWorkshopComponent),
        data : { breadcrumb : 'Workshop/Select Workshop'},
        canActivate : [AuthGuard]
      },

      {
        path : 'workshop-management/cash-collections',
        loadComponent: () => import('./service-bay/service-cash-collection/service-cash-collection.component').then(c => c.ServiceCashCollectionComponent),
        data : { breadcrumb : 'Workshop Cash Collections'},
        canActivate : [AuthGuard]
      },

      /** End of Service Bay */


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
        path : 'accounts-and-finance/parking-discounts',
        loadComponent: () => import('./accounts-and-finance/parking-discounts/parking-discounts.component').then(c => c.ParkingDiscountsComponent),
        data : { breadcrumb : 'Accounts & Finance | Parking Discounts'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/discounts',
        loadComponent: () => import('./accounts-and-finance/discounts/discounts.component').then(c => c.DiscountsComponent),
        data : { breadcrumb : 'Accounts & Finance | Discounts'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/storage-billing',
        loadComponent: () => import('./accounts-and-finance/storage-billing/storage-billing.component').then(c => c.StorageBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Storage Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/storage-discounts',
        loadComponent: () => import('./accounts-and-finance/storage-discounts/storage-discounts.component').then(c => c.StorageDiscountsComponent),
        data : { breadcrumb : 'Accounts & Finance | Storage Discounts'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/vehicle-equipment-billing',
        loadComponent: () => import('./accounts-and-finance/vehicle-equipment-billing/vehicle-equipment-billing.component').then(c => c.VehicleEquipmentBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Vehicle & Equipment Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/good-billing',
        loadComponent: () => import('./accounts-and-finance/good-billing/good-billing.component').then(c => c.GoodBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Good Storage Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/bond-billing',
        loadComponent: () => import('./accounts-and-finance/bond-billing/bond-billing.component').then(c => c.BondBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Bond Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/bond-item-billing',
        loadComponent: () => import('./accounts-and-finance/bond-item-billing/bond-item-billing.component').then(c => c.BondItemBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Bond Item Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/release-vehicle-equipment',
        loadComponent: () => import('./accounts-and-finance/release-vehicle-equipment/release-vehicle-equipment.component').then(c => c.ReleaseVehicleEquipmentComponent),
        data : { breadcrumb : 'Accounts & Finance | Release Vehicle & Equipment'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/maintenance-billing',
        loadComponent: () => import('./accounts-and-finance/maintenance-billing/maintenance-billing.component').then(c => c.MaintenanceBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Maintenance Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/maintenance-vehicle-equipment-billing',
        loadComponent: () => import('./accounts-and-finance/maintenance-vehicle-equipment-billing/maintenance-vehicle-equipment-billing.component').then(c => c.MaintenanceVehicleEquipmentBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Maintenance Veh/Eq Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/weigh-billing',
        loadComponent: () => import('./accounts-and-finance/weigh-billing/weigh-billing.component').then(c => c.WeighBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Weigh Billing'},
        canActivate : [AuthGuard]
      },
      {
        path : 'accounts-and-finance/weight-billing',
        loadComponent: () => import('./accounts-and-finance/weight-billing/weight-billing.component').then(c => c.WeightBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Weight Billing'},
        canActivate : [AuthGuard]
      },

      {
        path : 'accounts-and-finance/service-bay-billing',
        loadComponent: () => import('./accounts-and-finance/service-bay-billing/service-bay-billing.component').then(c => c.ServiceBayBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Service Bay Billing'},
        canActivate : [AuthGuard]
      },

      {
        path : 'accounts-and-finance/machine-service-billing',
        loadComponent: () => import('./accounts-and-finance/machine-service-billing/machine-service-billing.component').then(c => c.MachineServiceBillingComponent),
        data : { breadcrumb : 'Accounts & Finance | Machine Service Billing'},
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

      {
        path : 'procurement/supplier-price-list',
        loadComponent: () => import('./procurement/supplier-price-list/supplier-price-list.component').then(c => c.SupplierPriceListComponent),
        data : { breadcrumb : 'Procurement/Supplier Price List'}, 
        canActivate : [AuthGuard]
      },

      {
        path : 'procurement/view-supplier-products',
        loadComponent: () => import('./procurement/view-supplier-products/view-supplier-products.component').then(c => c.ViewSupplierProductsComponent),
        data : { breadcrumb : 'Procurement/View Supplier Products'}, 
        canActivate : [AuthGuard]
      },

      {
        path : 'procurement/lpo',
        loadComponent: () => import('./procurement/lpo/lpo.component').then(c => c.LpoComponent),
        data : { breadcrumb : 'Procurement/LPO'}, 
        canActivate : [AuthGuard]
      },
      {
        path : 'procurement/grn',
        loadComponent: () => import('./procurement/grn/grn.component').then(c => c.GrnComponent),
        data : { breadcrumb : 'Procurement/GRN'}, 
        canActivate : [AuthGuard]
      },

      {
        path : 'procurement/reports/lpo-report',
        loadComponent: () => import('./procurement/reports/lpo-report/lpo-report.component').then(c => c.LpoReportComponent),
        data : { breadcrumb : 'Procurement/LPO Report'}, 
        canActivate : [AuthGuard]
      },
      {
        path : 'procurement/reports/grn-report',
        loadComponent: () => import('./procurement/reports/grn-report/grn-report.component').then(c => c.GrnReportComponent),
        data : { breadcrumb : 'Procurement/Grn Report'}, 
        canActivate : [AuthGuard]
      },
      

      // End of Procurement

      // Fleet operations
      {
        path : 'fleet-operations/weighbridge',
        loadComponent: () => import('./fleet-operations/weighbridge/weighbridge.component').then(c => c.WeighbridgeComponent),
        data : { breadcrumb : 'Fleet Operations/Weigh Bridge'}, 
        canActivate : [AuthGuard]
      },
      {
        path : 'fleet-management/cash-collections',
        loadComponent: () => import('./fleet-operations/weigh-cash-collection/weigh-cash-collection.component').then(c => c.WeighCashCollectionComponent),
        data : { breadcrumb : 'Weight/Cash Collections'},
        canActivate : [AuthGuard]
      },
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