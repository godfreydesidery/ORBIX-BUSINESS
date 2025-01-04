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

import { ISupplier } from 'src/app/domain/supplier';
import { ISupplierProduct } from 'src/app/domain/supplier-product';
import { IGrn, IGrnDetail } from 'src/app/domain/grn';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-grn',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './grn.component.html',
  styleUrl: './grn.component.scss'
})
export class GrnComponent {
shopId: any = null

  products : IProduct[] = []

  status : string = ''

  searchKey : string = '' 

  productId : any
  productName : string = ''
  productDescription : string = ''
  productCode : string = ''

  supplierId : any = null
  supplierName : string = ''
  supplierCode : string = ''

  shopName : string = ''

  searchedProducts : IProduct[] = []

  isUserTyping: boolean = true; // Flag to detect user typing


  grns : IGrn[] = []

  grnId : any = null

  lpoNo : string = ''


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
    this.getCompanySuppliers()
    this.getBranchShops()
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
        .get<IProduct[]>(API_URL+'/products/get_products_by_company_containing?product_name_like=' + this.searchTerm, options)
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
    
    this.searchCompanyProduct(product.id)
    this.filteredProducts = [];
  }














  // loadSelectedShop = async () => {
  //     let options = {
  //       headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //     }
  
  //     await this.http.get<IShop>(API_URL+'/shops/get_selected_shop?shop_id=' + this.shopId , options)
  //       .toPromise()
  //       .then(
  //         data => {
  //           console.log(data)
  //           this.shopName = data!.name
  //           this.selectedShop = data!
            
  //         }
  //       )
  //       .catch(error => {
  //         console.log(error)
          
  //       }       
  //     ) 
  //   }



    async getAllPendingOrders(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.grns = []
    
      await this.http.get<IGrn[]>(API_URL+'/grns/get_all_visible_by_branch', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.grns.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }

    grn! : IGrn

    totalAmount : number = 0

    async get(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IGrn>(API_URL+'/grns/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)
          console.log(data)

          this.grn = data!

          this.status = this.grn.status

          this.grnId = this.grn.id

          this.totalAmount = 0

          var sn = 1
          this.grn.grnDetails.forEach(element => {
            element.sn = sn
            this.totalAmount = this.totalAmount + ((+element.costPriceVatIncl) * element.qty)
            sn = sn + 1
            
          })
        }
      )
    }


    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
    
      var grn = {
        id : null,
        no : null,
        summary : null,
        shopId : this.shopId,
        supplierId : this.supplierId
      }

      if(grn.id === null){
        /**Create new grn */
        await this.http.post<IGrn>(API_URL+'/grns/create', grn, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
    
            console.log(data)

            this.get(data!.id)
 
            this.msg.showSuccessMessage('GRN created successifully')
    
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
        await this.http.post<IGrn>(API_URL+'/grns/update', grn, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
    
            console.log(data)

            this.get(data!.id)
    
    
            this.msg.showSuccessMessage('GRN updated successifully')
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

    public async createByLpoNo(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.post<IGrn>(API_URL+'/grns/create_by_lpo_no?lpo_no=' + this.lpoNo, null, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
    
            console.log(data)

            this.get(data!.id)
 
            this.msg.showSuccessMessage('GRN created successifully')
    
          }
    
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }

    clearOrder(){
      this.grn!
      this.grnId = null
      this.supplierId = null
      this.shopId = null
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
      this.searchCompanyProduct(selectedProduct.id)
      this.searchedProducts = []; // Clear suggestions after selection
    } else {
      console.warn('Selected product is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
  }

  searchCompanyProduct = async (productId : any) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.clearGrnDetail()
  
      await this.http.get<IProduct>(API_URL+'/products/get_company_product?product_id=' + productId, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.productId = data!.id
            this.productCode = data!.code
            this.productName = data!.name
            this.productDescription = data!.description
            //this.costPriceVatIncl = data!.costPriceVatIncl

          }
        )
        .catch(error => {
          console.log(error)          
        }       
      ) 
      
    }

    grnDetailId : any = null
    vat : number = 0
    costPriceVatIncl : number = 0

    baseUom : string = ''

    qty : number = 0

    clearGrnDetail(){
      this.productId = null

      this.searchTerm = ''
    
      this.grnDetailId = null
      this.searchKey = ''
      this.productName = ''
      this.productCode = ''
      this.productDescription = ''
      this.vat = 0
      this.costPriceVatIncl = 0

      this.baseUom = ''
      this.qty = 0
    }


  // searchProductInShop = async (product_id : any) => {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }
  //   this.clearShopProduct()

  //   await this.http.get<IShopProduct>(API_URL+'/shop_products/get_product_in_shop?product_id=' + product_id + '&shop_id=' + this.shopId , options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         console.log(data)
  //         this.productId = data!.productId
  //         this.productCode = data!.productCode
  //         this.productName = data!.productName
  //         this.productDescription = data!.productDescription
  //         this.vat = data!.vatRate * 100
  //         this.costPriceVatIncl = data!.costPriceVatIncl
  //         this.baseUom = data!.baseUom
          
  //       }
  //     )
  //     .catch(error => {
  //       console.log(error)
        
  //     }       
  //   ) 
    
  // }

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
        id : this.grnDetailId,
        grnId : this.grnId,
        productId : this.productId,
        qty : this.qty,
        costPriceVatIncl : this.costPriceVatIncl,
        vatRate : this.vat / 100
      }

      if(detail.id === null){
        /**Create new detail */
        this.http.post<IGrnDetail>(API_URL+'/grns/create_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.grnId)
            }
          )
          .catch(error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }       
        ) 
      }else if(detail.id !== null){
        /**Update detail */
        this.http.post<IGrnDetail>(API_URL+'/grns/update_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.grnId)
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
      this.http.get<IGrnDetail>(API_URL+'/grns/remove_detail?grn_detail_id=' + id + '&grn_id=' + this.grnId, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.grnId)
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }


    async approveOrder(){
      if(await this.msg.showConfirmMessageDialog('Approve', 'Are you sure you want to approve this order?', 'question', 'Yes', 'No') == false){
        return
      }
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.http.post<IGrn>(API_URL+'/grns/approve?grn_id=' + this.grnId, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.grnId)
            this.msg.showSuccessMessage('GRN approved successifully')
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }
    

    async cancelOrder(){
      if(await this.msg.showConfirmMessageDialog('Cancel', 'Are you sure you want to cancel this order?', 'question', 'Yes', 'No') == false){
        return
      }
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.http.post<IGrn>(API_URL+'/grns/cancel?grn_id=' + this.grnId, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.grnId)
            this.msg.showSuccessMessage('GRN canceled successifully')
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }

    async archiveOrder(){
      if(await this.msg.showConfirmMessageDialog('Cancel', 'Are you sure you want to archive this order?', 'question', 'Yes', 'No') == false){
        return
      }
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.http.post<IGrn>(API_URL+'/grns/archive?grn_id=' + this.grnId, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.grnId)
            this.msg.showSuccessMessage('GRN archived successifully')
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }

    suppliers : ISupplier[] = []
    getCompanySuppliers(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.suppliers = []
      this.http.get<ISupplier[]>(API_URL+'/suppliers/get_all_by_company', options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.suppliers = data!
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }

    async onSupplierChange(event: any): Promise<void> {
      this.supplierId = await event.target.value;
      await this.loadSelectedSupplier()
    }

    supplier! : ISupplier
    loadSelectedSupplier = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.get<ISupplier>(API_URL+'/suppliers/get?id=' + this.supplierId , options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.supplier = data!
            
          }
        )
        .catch(error => {
          console.log(error)
          this.supplierId = null
        }       
      ) 
    }

    shops : IShop[] = []
    getBranchShops(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.shops = []
      this.http.get<IShop[]>(API_URL+'/shops/get_branch_shops', options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.shops = data!
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }

    async onShopChange(event: any): Promise<void> {
      this.shopId = await event.target.value;
      await this.loadSelectedSupplier()
    }

    shop! : IShop
    loadSelectedShop = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.get<IShop>(API_URL+'/shops/get?id=' + this.shopId , options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.shop = data!
            
          }
        )
        .catch(error => {
          console.log(error)
          this.shopId = null
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
