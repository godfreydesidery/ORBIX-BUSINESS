import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IRestaurant } from 'src/app/domain/restaurant';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { IProduct } from 'src/app/domain/product';
import { JwtHelperService } from '@auth0/angular-jwt';
import { IRestaurantProduct } from 'src/app/domain/restaurant-product';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-restaurant-product-stock-status',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-product-stock-status.component.html',
  styleUrl: './restaurant-product-stock-status.component.scss'
})
export class RestaurantProductStockStatusComponent {
restaurantId: number;

  restaurantProducts : IRestaurantProduct[] = []

  searchKey : string = ''

  productId : any
  productName : string = ''
  productDescription : string = ''
  productCode : string = ''

  selectedRestaurant : IRestaurant

  restaurantName : string = ''

  currentStock : number = 0

  searchedProducts : IProduct[] = []

  isUserTyping: boolean = true; // Flag to detect user typing

  showImportList : boolean = false

  importProducts : IProduct[] = []

  page: number = 1; // Initialize the current page to 1
  filterRecords : string = ''
  selectedOption: string = '';
  options: string[] = ['Option 1', 'Option 2', 'Option 3'];

  action : string = ''

  reason : string = '--Select Action--'

  amountToChange : number = 0

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    this.loadRestaurantProductStockStatus()
    this.loadSelectedRestaurant()
  }

  showRestaurantProducts: IRestaurantProduct[] = [];

  loadRestaurantProductStockStatus = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.restaurantProducts = []

    await this.http.get<IRestaurantProduct[]>(API_URL+'/restaurant_products/get_stock_by_restaurant?restaurant_id=' + this.restaurantId, options)
    .toPromise()
    .then(
      data => {
        this.restaurantProducts = data!

        this.showRestaurantProducts = this.restaurantProducts

        console.log(data)
      }
    )
    .catch(
      error => {
        console.log(error)
      }
    )
  }

  filterUnderStock(){
    this.showRestaurantProducts = []
    this.restaurantProducts.forEach(element => {
      if((+element.currentStock) < element.minStock){
        this.showRestaurantProducts.push(element)
      }
    })
  }

  filterOutofStock(){
    this.showRestaurantProducts = []
    this.restaurantProducts.forEach(element => {
      if((+element.currentStock) <= 0){
        this.showRestaurantProducts.push(element)
      }
    })
  }

  restaurantProductId : any = null
  vat : number = 0
  costPriceVatIncl : number = 0
  sellingPriceVatIncl : number = 0
  costPriceVatExcl : number = 0
  sellingPriceVatExcl : number = 0
  minStock : number = 0
  maxStock : number = 0
  defaultReorderQty : number = 0
  defaultReorderLevel : number = 0
  importRestaurantProduct = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var restaurantProduct = {
      productId : this.productId,
      restaurantId : this.restaurantId,
      vatRate : this.vat/100,
      costPriceVatIncl : this.costPriceVatIncl,
      sellingPriceVatIncl : this.sellingPriceVatIncl,
      costPriceVatExcl : this.costPriceVatExcl,
      sellingPriceVatExcl : this.sellingPriceVatExcl,
      minStock : this.minStock,
      maxStock : this.maxStock,
      defaultReorderQty : this.defaultReorderQty,
      defaultReorderLevel : this.defaultReorderLevel
    }

    await this.http.post<IRestaurantProduct>(API_URL+'/restaurant_products/create', restaurantProduct, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Product imported successfully')
        this.loadRestaurantProductStockStatus()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  updateRestaurantProduct = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var restaurantProduct = {
      productId : this.productId,
      restaurantId : this.restaurantId,
      vatRate : this.vat/100,
      costPriceVatIncl : this.costPriceVatIncl,
      sellingPriceVatIncl : this.sellingPriceVatIncl,
      costPriceVatExcl : this.costPriceVatExcl,
      sellingPriceVatExcl : this.sellingPriceVatExcl,
      minStock : this.minStock,
      maxStock : this.maxStock,
      defaultReorderQty : this.defaultReorderQty,
      defaultReorderLevel : this.defaultReorderLevel
    }

    await this.http.post<IRestaurantProduct>(API_URL+'/restaurant_products/update', restaurantProduct, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Product updated successfully')
        this.loadRestaurantProductStockStatus()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  adjustRestaurantStock = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var stk = 0

    if((+this.amountToChange) <= 0){
      this.msg.showErrorMessage3('Invalid')
      return
    }

    if(this.reason === '' || this.reason === null){
      this.msg.showErrorMessage3('Please enter reason')
      return
    }
      
    var command = ''
    stk = (+this.amountToChange)

    if(this.action === 'Add'){
      command = 'add_stock'
    }else if(this.action === 'Deduct'){
      command = 'deduct_stock'
    }else{
      this.msg.showErrorMessage3('Invalid')
      return;
    }


    var restaurantProduct = {
      productId : this.productId,
      restaurantId : this.restaurantId,
      qty : stk,
      reason : this.reason
    }

    await this.http.post<IRestaurantProduct>(API_URL+'/restaurant_products/' + command, restaurantProduct, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Product stock updated successfully')
        this.loadRestaurantProductStockStatus()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  clearRestaurantProduct(){
  this.productId = null

  this.productName = ''
  this.productCode = ''
  this.vat = 0
  this.costPriceVatIncl = 0
  this.sellingPriceVatIncl = 0
  this.costPriceVatExcl = 0
  this.sellingPriceVatExcl = 0
  this.minStock = 0
  this.maxStock = 0
  this.defaultReorderQty = 0
  this.defaultReorderLevel = 0

  this.reason = ''
  this.amountToChange = 0
  this.action = '--Select Action--'
  }

  loadSelectedRestaurant = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IRestaurant>(API_URL+'/restaurants/get_selected_restaurant?restaurant_id=' + this.restaurantId , options)
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

  getCompanyProductLike = async (searchKey : string) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IProduct[]>(API_URL+'/products/get_products_by_company?product_name_like=' + searchKey , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.searchedProducts = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

  searchProduct = async (id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearRestaurantProduct()

    await this.http.get<IProduct>(API_URL+'/products/get?id=' + id , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.productId = data!.id
          this.productCode = data!.code
          this.productName = data!.name
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
    
  }

  searchRestaurantProduct = async (id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearRestaurantProduct()

    await this.http.get<IRestaurantProduct>(API_URL+'/restaurant_products/get?id=' + id + '&restaurant_id=' + this.restaurantId , options)
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
          this.currentStock = data!.currentStock
          this.defaultReorderQty = data!.defaultReorderQty
          this.defaultReorderLevel = data!.defaultReorderLevel
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
    
  }


  // Handle selection from datalist
  onProductSelected(): void {
    const selectedProduct = this.searchedProducts.find(
      (product) => product.name === this.searchKey
    );

    if (selectedProduct) {
      this.searchProduct(selectedProduct.id);
      this.searchedProducts = []; // Clear suggestions after selection
    } else {
      console.warn('Selected product is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
  }

  // Triggered on every keystroke
  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getCompanyProductLike(searchText);
    }
  }


  toggleShowImportList = async () => {

    if(this.showImportList){
      this.importProducts = []
      this.showImportList = false
      return
    }
    await this.getProductsToImport()
    if(this.importProducts.length == 0){
      this.msg.showErrorMessage3('No products to import')
      return
    }
    this.showImportList = true
  }
  

  getProductsToImport = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IProduct[]>(API_URL+'/products/get_company_sellable_products', options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.importProducts = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

  clearImportList = () => {
    this.importProducts = []
    this.showImportList = false
  }


  grant(privileges: string[]): boolean {
      /** Allow user to perform an action if the user has that privilege */
      
      const currentUser = JSON.parse(localStorage.getItem('current-user')!);
      if (!currentUser || !currentUser.access_token) {
          console.error('No valid user or access token found.');
          return false;
      }
  
      const decodedToken = new JwtHelperService().decodeToken(currentUser.access_token);
      if (!decodedToken || !decodedToken.privileges) {
          console.error('No privileges found in the token.');
          return false;
      }
      const userPrivileges = decodedToken.privileges as string[];
    
      // Check if any of the required privileges exist in the user's privileges
      return privileges.some(privilege => userPrivileges.includes(privilege));
  }

  




}
