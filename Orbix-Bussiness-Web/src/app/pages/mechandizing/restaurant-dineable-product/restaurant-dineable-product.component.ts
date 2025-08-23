import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { IRestaurant } from 'src/app/domain/restaurant';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';

import { HttpClient } from '@angular/common/http';

import { ActivatedRoute, Router } from '@angular/router';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { AuthService } from 'src/app/auth.service';

import { ReceiptItem } from 'src/app/domain/receipt-item';
import { ICustomer } from 'src/app/domain/customer';
import { IRestaurantDineable } from 'src/app/domain/restaurant-dineable';
import { IDineable } from 'src/app/domain/dineable';
import { IRestaurantSalesOrder, IRestaurantSalesOrderDetail } from 'src/app/domain/restaurant-sales-order';
import { IRestaurantAgent } from 'src/app/domain/restaurant-agent';
import { error } from 'src/custom-packages/util';
import { IProduct } from 'src/app/domain/product';
import { IRestaurantProduct } from 'src/app/domain/restaurant-product';
import { IRestaurantDinableProduct } from 'src/app/domain/restaurant-dineable-product';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-restaurant-dineable-product',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-dineable-product.component.html',
  styleUrl: './restaurant-dineable-product.component.scss'
})
export class RestaurantDineableProductComponent {

  // ============================
  // Restaurant & Product Context
  // ============================
  restaurantId: number;
  selectedRestaurant: IRestaurant;
  restaurantName: string = '';
  restaurantProducts: IRestaurantProduct[] = [];
  searchedProducts: IProduct[] = [];
  filteredProducts: any[] = [];
  selectedProduct: any | null = null;
  isDropdownOpen: boolean = false;

  // ============================
  // Product Details
  // ============================
  productId: any;
  productName: string = '';
  productDescription: string = '';
  productCode: string = '';
  discount: number = 0;
  vat: number = 0;
  costPriceVatIncl: number = 0;
  sellingPriceVatIncl: number = 0;
  costPriceVatExcl: number = 0;
  sellingPriceVatExcl: number = 0;
  minStock: number = 0;
  maxStock: number = 0;
  defaultReorderQty: number = 0;
  defaultReorderLevel: number = 0;
  baseUom: string = '';
  qty: number | string = 1;

  // ============================
  // Sales Orders
  // ============================
  restaurantSalesOrders: IRestaurantSalesOrder[] = [];
  restaurantSalesOrderId: any = null;
  salesOrderDetailId: any = null;
  restaurantDineableId: any = null;
  customerName: string = '';

  // ============================
  // Search & Filters
  // ============================
  searchTerm: string = '';
  searchKey: string = '';
  isUserTyping: boolean = true; // Flag to detect user typing
  page: number = 1; // Pagination
  filterRecords: string = '';
  selectedOption: string = '';

  // ============================
  // Importing / Dineables
  // ============================
  showImportList: boolean = false;
  importDineables: IDineable[] = [];

  // ============================
  // Payment
  // ============================
  payCode: string = 'CASH';
  payRefNo: string = '';
  selectedAgentId: any = null;



  restaurantDineables: IRestaurantDineable[] = []

  restaurantDinableProducts: IRestaurantDinableProduct[] = []

  selectedDineableId: any = null
  selectedDineableCode: string = ''
  selectedDineableName: string = ''

  id: any = null
  dinableName: string = ''


  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
    private route: ActivatedRoute
  ) { }

  async ngOnInit(): Promise<void> {
    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    await this.loadSelectedRestaurant()
    await this.loadRestaurantDineableStockStatus()
  }

  searchProducts(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)

    }
    this.filteredProducts = [];
    if (this.searchTerm.trim().length >= 2) {
      this.http
        .get<IProduct[]>(API_URL + '/restaurant_products/get_products_by_restaurant_containing?product_name_like=' + this.searchTerm + '&restaurant_id=' + this.restaurantId, options)
        .subscribe(
          (data) => (this.filteredProducts = data),
          (error) => console.error('Error fetching dineables:', error)
        );
    } else {
      this.filteredProducts = [];
    }
  }

  selectProduct(product: any): void {
    this.selectedProduct = product
    this.searchTerm = product.name
    this.isDropdownOpen = false

    this.searchProductInRestaurant(product.id)
    this.filteredProducts = []
  }

  searchProductInRestaurant = async (product_id: any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.clearRestaurantProduct()

    await this.http.get<IRestaurantProduct>(API_URL + '/restaurant_products/get_product_in_restaurant?product_id=' + product_id + '&restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.productId = data!.productId
          this.productCode = data!.productCode
          this.productName = data!.productName
          this.productDescription = data!.productDescription
          this.vat = data!.vatRate * 100
          this.costPriceVatIncl = data!.costPriceVatIncl
          this.sellingPriceVatIncl = data!.sellingPriceVatIncl
          this.costPriceVatExcl = data!.costPriceVatExcl
          this.sellingPriceVatExcl = data!.sellingPriceVatExcl
          this.minStock = data!.minStock
          this.maxStock = data!.maxStock
          this.defaultReorderQty = data!.defaultReorderQty
          this.defaultReorderLevel = data!.defaultReorderLevel
          this.baseUom = data!.baseUom

          this.searchTerm = data!.productName

        }
      )
      .catch(error => {
        console.log(error)

      }
      )

  }

  clearRestaurantProduct() {
    this.productId = null

    this.searchTerm = ''

    this.salesOrderDetailId = null
    this.searchKey = ''
    this.productName = ''
    this.productCode = ''
    this.productDescription = ''
    this.vat = 0
    this.costPriceVatIncl = 0
    this.sellingPriceVatIncl = 0
    this.costPriceVatExcl = 0
    this.sellingPriceVatExcl = 0
    this.minStock = 0
    this.maxStock = 0
    this.defaultReorderQty = 0
    this.defaultReorderLevel = 0
    this.baseUom = ''
    this.qty = 1
    this.discount = 0
  }

  loadSelectedRestaurant = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurant>(API_URL + '/restaurants/get_selected_restaurant?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.restaurantName = data!.name
          this.selectedRestaurant = data!

        }
      )
      .catch(error => {
        console.log(error)

      }
      )
  }

  loadRestaurantDineableStockStatus = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.restaurantDineables = []
    await this.http.get<IRestaurantDineable[]>(API_URL + '/restaurant_dineables/get_stock_by_restaurant?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          this.restaurantDineables = data!
          var sn = 1
          this.restaurantDineables.forEach(element => {
            element.sn = sn
            sn++
          })
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        }
      )
  }

  async loadRestaurantDinableProducts(dineableId: any, dineableCode: string, dineableName: string) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.selectedDineableId = dineableId
    this.selectedDineableCode = dineableCode
    this.selectedDineableName = dineableName

    this.restaurantDinableProducts = []

    await this.http.get<IRestaurantDinableProduct[]>(API_URL + '/restaurant_dineable_products?restaurant_id=' + this.restaurantId + '&dineable_id=' + dineableId, options)
      .toPromise()
      .then(
        data => {
          this.restaurantDinableProducts = data!
          var sn = 1
          this.restaurantDinableProducts.forEach(element => {
            element.sn = sn
            sn++
          })
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        }
      )

  }

  createRestaurantDineableProduct = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var restaurantDineableProduct = {

      restaurantId: this.restaurantId,
      dineableId: this.selectedDineableId,
      productId: this.productId,
      productQty: this.qty

    }

    await this.http.post<IRestaurantDineable>(API_URL + '/restaurant_dineable_products/create', restaurantDineableProduct, options)
      .toPromise()
      .then(
        data => {

          console.log(data)
          this.msg.showSuccessMessage('Created successfully')
          this.loadRestaurantDinableProducts(this.selectedDineableId, this.selectedDineableCode, this.selectedDineableName)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  updateRestaurantDineableProduct = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var restaurantDineableProduct = {

      id: this.id,
      restaurantId: this.restaurantId,
      dineableId: this.selectedDineableId,
      productId: this.productId,
      productQty: this.qty

    }

    await this.http.post<IRestaurantDineable>(API_URL + '/restaurant_dineable_products/update', restaurantDineableProduct, options)
      .toPromise()
      .then(
        data => {

          console.log(data)
          this.msg.showSuccessMessage('Updated successfully')
          this.loadRestaurantDinableProducts(this.selectedDineableId, this.selectedDineableCode, this.selectedDineableName)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async remove(id: any) {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to remove?', 'question', 'Yes', 'No') == false) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<boolean>(API_URL + '/restaurant_dineable_products/remove?id=' + id, options)
      .toPromise()
      .then(
        () => {
          this.loadRestaurantDinableProducts(this.selectedDineableId, this.selectedDineableCode, this.selectedDineableName)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurantDinableProduct>(API_URL + '/restaurant_dineable_products/get?id=' + id, options)
      .toPromise()
      .then(
        (data) => {
          console.log(data)
          this.id = data!.id
          this.productName = data!.productName
          this.qty = data!.productQty
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )

  }
}
