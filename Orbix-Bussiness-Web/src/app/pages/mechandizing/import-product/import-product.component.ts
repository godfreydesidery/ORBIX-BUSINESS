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
import { IShop } from 'src/app/domain/shop';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { IShopProduct } from 'src/app/domain/shop-product';
import { IProduct } from 'src/app/domain/product';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-import-product',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './import-product.component.html',
  styleUrl: './import-product.component.scss'
})
export class ImportProductComponent {
  
    shopId: number;
  
    shopProducts : IShopProduct[] = []
  
    searchKey : string = ''
  
    productId : any
    productName : string = ''
    productDescription : string = ''
    productCode : string = ''
  
    selectedShop : IShop
  
    shopName : string = ''
  
    searchedProducts : IProduct[] = []
  
    isUserTyping: boolean = true; // Flag to detect user typing
  
    showImportList : boolean = false
  
    importProducts : IProduct[] = []

    page: number = 1; // Initialize the current page to 1
    filterRecords : string = ''
    selectedOption: string = '';
    options: string[] = ['Option 1', 'Option 2', 'Option 3'];
  
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
      // Retrieve the shop_id query parameter from the URL
      this.route.queryParams.subscribe(params => {
        this.shopId = params['shop_id'];
        console.log('Shop ID:', this.shopId);
      });
      this.loadSelectedShop()
      this.getProductsToImportByShop()     
    }
  
    loadShopProductStockStatus = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      this.shopProducts = []
  
      await this.http.get<IShopProduct[]>(API_URL+'/shop_products/get_stock_by_shop?shop_id=' + this.shopId, options)
      .toPromise()
      .then(
        data => {
          this.shopProducts = data!
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        }
      )
    }
  
    shopProductId : any = null
    vat : number = 0
    costPriceVatIncl : number = 0
    sellingPriceVatIncl : number = 0
    costPriceVatExcl : number = 0
    sellingPriceVatExcl : number = 0
    minStock : number = 0
    maxStock : number = 0
    defaultReorderQty : number = 0
    defaultReorderLevel : number = 0
    importShopProduct = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var shopProduct = {
        productId : this.productId,
        shopId : this.shopId,
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
  
      await this.http.post<IShopProduct>(API_URL+'/shop_products/create', shopProduct, options)
      .toPromise()
      .then(
        data => {
          
          console.log(data)
          this.msg.showSuccessMessage('Product imported successfully')
          this.loadShopProductStockStatus()
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  
  
    }
  
    updateShopProduct = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var shopProduct = {
        productId : this.productId,
        shopId : this.shopId,
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
  
      await this.http.post<IShopProduct>(API_URL+'/shop_products/update', shopProduct, options)
      .toPromise()
      .then(
        data => {
          
          console.log(data)
          this.msg.showSuccessMessage('Product updated successfully')
          this.loadShopProductStockStatus()
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  
  
    }
  
    clearShopProduct(){
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
    }
  
    loadSelectedShop = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      await this.http.get<IShop>(API_URL+'/shops/get_selected_shop?shop_id=' + this.shopId , options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.shopName = data!.name
            this.selectedShop = data!
            
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
      this.clearShopProduct()
  
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
  
    searchShopProduct = async (id : any) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.clearShopProduct()
  
      await this.http.get<IShopProduct>(API_URL+'/shop_products/get?id=' + id + '&shop_id=' + this.shopId , options)
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
      await this.getProductsToImportByShop()
      if(this.importProducts.length == 0){
        this.msg.showErrorMessage3('No products to import')
        return
      }
      this.showImportList = true
    }
    
  
    getProductsToImportByShop = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IProduct[]>(API_URL+'/products/get_company_sellable_products_by_shop?shop_id=' + this.shopId, options)
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
  
    
  
  
  
}
