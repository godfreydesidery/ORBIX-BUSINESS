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
import { IShopSalesOrder, IShopSalesOrderDetail } from 'src/app/domain/shop-sales-order';

import { ReceiptItem } from 'src/app/domain/receipt-item';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-shop-sales-order',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './shop-sales-order.component.html',
  styleUrl: './shop-sales-order.component.scss'
})
export class ShopSalesOrderComponent {
  shopId: number;

  shopProducts : IShopProduct[] = []

  searchKey : string = ''

  productId : any
  productName : string = ''
  productDescription : string = ''
  productCode : string = ''
  discount : number = 0

  selectedShop : IShop

  shopName : string = ''

  searchedProducts : IProduct[] = []

  isUserTyping: boolean = true; // Flag to detect user typing

  showImportList : boolean = false

  importProducts : IProduct[] = []

  shopSalesOrders : IShopSalesOrder[] = []

  shopSalesOrderId : any = null

  customerName : string = ''

  page: number = 1; // Initialize the current page to 1
  filterRecords : string = ''
  selectedOption: string = '';

  payCode : string = 'CASH'
  payRefNo : string = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    private route: ActivatedRoute
  ) {}

  async ngOnInit(): Promise<void> {
    // Retrieve the shop_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.shopId = params['shop_id'];
      console.log('Shop ID:', this.shopId);
    });
    await this.loadSelectedShop()
    //await this.getAllPendingOrders()
  }





  searchTerm: string = '';
  filteredProducts: any[] = [];
  selectedProduct: any | null = null;
  isDropdownOpen: boolean = false;
  searchProducts(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    
    }
    this.filteredProducts = [];
    if (this.searchTerm.trim().length >= 2) {
      this.http
      //await this.http.get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId , options)
        .get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + this.searchTerm + '&shop_id=' + this.shopId , options)
        .subscribe(
          (data) => (this.filteredProducts = data),
          (error) => console.error('Error fetching products:', error)
        );
    } else {
      this.filteredProducts = [];
    }
  }

  selectProduct(product: any): void {
    this.selectedProduct = product;
    this.searchTerm = product.name;
    this.isDropdownOpen = false;
    
    this.searchProductInShop(product.id)
    this.filteredProducts = [];
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



    async getAllPendingOrders(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.shopSalesOrders = []
    
      await this.http.get<IShopSalesOrder[]>(API_URL+'/shop_sales_orders/get_all_pending_by_shop?shop_id=' + this.shopId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.shopSalesOrders.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }

    shopSalesOrder! : IShopSalesOrder

    totalAmount : number = 0

    async get(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IShopSalesOrder>(API_URL+'/shop_sales_orders/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)
          console.log(data)

          this.shopSalesOrder = data!

          this.shopSalesOrderId = this.shopSalesOrder.id

          this.customerName = data!.customerName

          this.totalAmount = 0

          var sn = 1
          this.shopSalesOrder.shopSalesOrderDetails.forEach(element => {
            element.sn = sn
            this.totalAmount = this.totalAmount + ((+element.sellingPriceVatIncl) * element.qty - (+element.discount))
            sn = sn + 1
            
          })

          this.receiptData = this.shopSalesOrder.shopSalesOrderDetails

        }
      )
    }


    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
    
      var shopSalesOrder = {
        id : null,
        no : null,
        summary : null,
        shopId : this.shopId,
        customerName : this.customerName
      }
    
      if(shopSalesOrder.id === null){
        /**Create new uom */
        await this.http.post<IShopSalesOrder>(API_URL+'/shop_sales_orders/create', shopSalesOrder, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
    
            console.log(data)

            this.get(data!.id)
 
            this.msg.showSuccessMessage('Order created successifully')
    
          }
    
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
      }else{
        /**Update an existing uom */
        await this.http.post<IShopSalesOrder>(API_URL+'/shop_sales_orders/update', shopSalesOrder, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
    
            console.log(data)

            this.get(data!.id)
    
    
            this.msg.showSuccessMessage('UOM updated successifully')
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

    clearOrder(){
      this.shopSalesOrder!
      this.shopSalesOrderId = null
      this.customerName = ''
    }

    // Triggered on every keystroke
  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getShopProductLike(searchText);
    }
  }

  onProductSelected(): void {
    const selectedProduct = this.searchedProducts.find(
      (product) => product.name === this.searchKey
    );

    if (selectedProduct) {
      this.searchProductInShop(selectedProduct.id)
      this.searchedProducts = []; // Clear suggestions after selection
    } else {
      console.warn('Selected product is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
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
            this.productDescription =data!.description

           
            
          }
        )
        .catch(error => {
          console.log(error)
          
        }       
      ) 
      
    }

    salesOrderDetailId : any = null
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
    baseUom : string = ''

    qty : number = 0

    clearShopProduct(){
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
      this.qty = 0
      this.discount = 0
      }


  searchProductInShop = async (product_id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearShopProduct()

    await this.http.get<IShopProduct>(API_URL+'/shop_products/get_product_in_shop?product_id=' + product_id + '&shop_id=' + this.shopId , options)
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
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
    
  }

  getShopProductLike = async (searchKey : string) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      await this.http.get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId , options)
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

    saveDetail(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var detail = {
        id : this.salesOrderDetailId,
        shopSalesOrderId : this.shopSalesOrderId,
        productId : this.productId,
        discount : this.discount,
        qty : this.qty
      }

      if(detail.id === null){
        /**Create new detail */
        this.http.post<IShopSalesOrderDetail>(API_URL+'/shop_sales_orders/create_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.shopSalesOrderId)
            }
          )
          .catch(error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }       
        ) 
      }else if(detail.id !== null){
        /**Update detail */
        this.http.post<IShopSalesOrderDetail>(API_URL+'/shop_sales_orders/update_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.shopSalesOrderId)
            }
          )
          .catch(error => {
            console.log(error)
          }       
        ) 
      }


    }

    removeDetail(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.http.get<IShopSalesOrderDetail>(API_URL+'/shop_sales_orders/remove_detail?shop_sales_order_detail_id=' + id + '&shop_sales_order_id=' + this.shopSalesOrderId, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.shopSalesOrderId)
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }

    async confirmOrder(){
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to confirm this order?', 'question', 'Yes', 'No') == false){
        return
      }
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.post<IShopSalesOrder>(API_URL+'/shop_sales_orders/confirm?shop_sales_order_id=' + this.shopSalesOrderId + '&pay_code=' + this.payCode + '&pay_ref_no=' + this.payRefNo, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.shopSalesOrderId)
            this.msg.showSuccessMessage('Order confirmed successifully')
            this.printReceipt()
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
      this.payCode = ''
      this.payRefNo = ''
    }

    async cancelOrder(){
      if(await this.msg.showConfirmMessageDialog('Cancel', 'Are you sure you want to cancel this order?', 'question', 'Yes', 'No') == false){
        return
      }
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.http.post<IShopSalesOrder>(API_URL+'/shop_sales_orders/cancel?shop_sales_order_id=' + this.shopSalesOrderId, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.shopSalesOrderId)
            this.msg.showSuccessMessage('Order canceled successifully')
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }



    receiptData : IShopSalesOrderDetail [] = []

    printReceipt(){
    
        // if(this.toPrintReceipt == false){
        //   return
        // }
    
        if(this.receiptData.length == 0){
          this.msg.showErrorMessage3('No data to print')
          return
        }
    
        var items : ReceiptItem[] = []
        var item : ReceiptItem
    
        this.receiptData.forEach(element => {
          item = new ReceiptItem()
          item.code = element.id
          item.name = element.productName
          item.amount = element.sellingPriceVatIncl * (+element.qty) - (+element.discount)
          item.qty = element.qty
          items.push(item)
        })
    
        this.printer.print(items, 'NA', 0)
        //this.toPrintReceipt = false
      }

    
    
}
