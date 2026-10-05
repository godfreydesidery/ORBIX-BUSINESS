import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IProduct } from 'src/app/domain/product';
import { ISupplier } from 'src/app/domain/supplier';
import { ISupplierProduct } from 'src/app/domain/supplier-product';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-supplier-price-list',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './supplier-price-list.component.html',
  styleUrl: './supplier-price-list.component.scss'
})
export class SupplierPriceListComponent {
  trackById = trackById
/**Data */
  id : any = null
  code : string = ''
  name : string = ''
  contactName : string = ''
  address : string = ''
  phoneNo : string = ''

  active : string = 'Inactive'

  supplier : ISupplier

  /**Collections */

  suppliers : ISupplier[] = []
  totalSuppliers : number = 0
  suppliersRequest : number = 0 // number of the latest list request; answers to older ones are ignored

  /**Identifiers */
  supplierId : string = ''


  page: number = 1; // Initialize the current page to 1
  pageSizeSuppliers : number = 15
  listSearchTimerSuppliers : any = null
  filterRecords : string = ''
  productPage: number = 1 // the product list has its own page and search
  filterProductRecords : string = ''
  selectedOption: string = '';


  productId : any
  productName : string = ''
  productDescription : string = ''
  productCode : string = ''

  selectedSupplier : ISupplier

  supplierName : string = ''

  maxSupplyQty : number = 0

  supplierProductId : any = null
  vat : number = 0
  costPriceVatIncl : number = 0

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllSuppliers()
  }

  async getAllSuppliers(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, searched on the server against the shown columns
    var page = this.page
    var request = ++this.suppliersRequest

    await this.http.get<IPage<ISupplier>>(API_URL+'/suppliers/get_page?' + pageParams(page, this.pageSizeSuppliers, this.filterRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.suppliersRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSizeSuppliers))
        if(page > lastPage){
          this.page = lastPage
          this.getAllSuppliers()
          return
        }
        var sn = (page - 1) * this.pageSizeSuppliers + 1
        data!.content.forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        this.suppliers = data!.content
        this.totalSuppliers = data!.totalElements
      }
    )
  }

  pageChangedSuppliers(page : number){
    this.page = page
    this.getAllSuppliers()
  }

  searchListSuppliers(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimerSuppliers)
    this.listSearchTimerSuppliers = setTimeout(() => {
      this.page = 1
      this.getAllSuppliers()
    }, 300)
  }



  async get(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<ISupplier>(API_URL+'/suppliers/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showSupplierData(data!)
        console.log(data)
      }
    )
  }


  add = async () => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var supplierProduct = {
        productId : this.productId,
        supplierId : this.selectedSupplierId,
        vatRate : this.vat/100,
        costPriceVatIncl : this.costPriceVatIncl,
        maxSupplyQty : this.maxSupplyQty
      }

      
  
      console.log(supplierProduct)
  
      await this.http.post<ISupplierProduct>(API_URL+'/supplier_products/create', supplierProduct, options)
      .toPromise()
      .then(
        data => {
          
          console.log(data)
          this.msg.showSuccessMessage('Product imported successfully')
          // this.loadSupplierProductsByBranch()
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  
  
    }

  

  async activate(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var company = {
      id : id
    }

    await this.http.post<String>(API_URL+'/suppliers/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllSuppliers()

          this.msg.showSuccessMessage('Supplier activated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async deactivate(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var company = {
      id : id
    }

    await this.http.post<String>(API_URL+'/suppliers/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllSuppliers()
          this.msg.showSuccessMessage('Supplier deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showSupplierData(data : ISupplier){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.contactName = data!.contactName
    this.address = data!.address
    this.phoneNo = data!.phoneNo
  }

  clearSupplierData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.contactName = ''
    this.address = ''
    this.phoneNo = ''
  }


  supplierProducts : ISupplierProduct[] = []
  totalSupplierProducts : number = 0
  supplierProductsRequest : number = 0 // number of the latest list request; answers to older ones are ignored
  pageSizeSupplierProducts : number = 15
  listSearchTimerSupplierProducts : any = null

  viewedSupplierId : any = null // supplier whose products the modal shows

  loadSupplierProductsByBranch = async (id : any) => {
    this.viewedSupplierId = id
    this.productPage = 1
    // do not show the previous supplier's products while the new supplier's products load
    this.supplierProducts = []
    this.totalSupplierProducts = 0
    await this.loadViewedSupplierProducts()
  }

  loadViewedSupplierProducts = async () => {
    await this.getViewedSupplierProductPage()
  }

  async getViewedSupplierProductPage(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, searched on the server against the shown columns
    var page = this.productPage
    var request = ++this.supplierProductsRequest

    await this.http.get<IPage<ISupplierProduct>>(API_URL+'/supplier_products/get_all_by_supplier_and_branch_page?supplier_id=' + this.viewedSupplierId + '&' + pageParams(page, this.pageSizeSupplierProducts, this.filterProductRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.supplierProductsRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSizeSupplierProducts))
        if(page > lastPage){
          this.productPage = lastPage
          this.getViewedSupplierProductPage()
          return
        }
        this.supplierProducts = data!.content
        this.totalSupplierProducts = data!.totalElements
      }
    )
    .catch(
      error => {
        console.log(error)
      }
    )
  }

  pageChangedSupplierProducts(page : number){
    this.productPage = page
    this.getViewedSupplierProductPage()
  }

  searchListSupplierProducts(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimerSupplierProducts)
    this.listSearchTimerSupplierProducts = setTimeout(() => {
      this.productPage = 1
      this.getViewedSupplierProductPage()
    }, 300)
  }


    searchTerm: string = '';
  filteredProducts: any[] = [];
  selectedProduct: any | null = null;
  isDropdownOpen: boolean = false;

  selectProduct(product: any): void {
    clearTimeout(this.searchTimer) // a search still waiting would refill the list after the selection
    this.selectedProduct = product;
    this.searchTerm = product.name;
    this.isDropdownOpen = false;
    
    this.searchSupplierProduct(product.id)
    this.filteredProducts = [];
  }


  searchSupplierProduct = async (id : any) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.clearSupplierProduct()

  
      await this.http.get<ISupplierProduct>(API_URL+'/supplier_products/get?id=' + id + '&supplier_id=' + this.selectedSupplierId , options)
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
            this.maxSupplyQty = data!.maxSupplyQty         
          }
        )
        .catch(error => {
          console.log(error)
          
        }       
      ) 

      this.filteredProducts = [];
      
    }

    clearSupplierProduct(){
      this.productId = null
      this.productName = ''
      this.productCode = ''
      this.productDescription = ''
      this.vat = 0
      this.costPriceVatIncl = 0
      this.maxSupplyQty = 0
    
      }

      selectedSupplierId : number | null = null
      supplierLoaded : boolean = false
      loadSelectedSupplier = async () => {
              let options = {
                headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
              }
        
              await this.http.get<ISupplier>(API_URL+'/suppliers/get?id=' + this.selectedSupplierId , options)
                .toPromise()
                .then(
                  data => {
                    console.log(data)
                    this.selectedSupplier = data!
                     //alert('Selected Supplier ID: ' + this.selectedSupplierId);
                    this.supplierLoaded = true
                  }
                )
                .catch(error => {
                  console.log(error)
                  this.selectedSupplierId = null
                 //alert('An error occured')
                  this.supplierLoaded = false
                }       
              ) 
            }

  searchTimer: any = null

  searchProducts(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    
    }
    this.filteredProducts = [];
    clearTimeout(this.searchTimer)
    if (this.searchTerm.trim().length >= 2) {
      // Wait for a short pause in typing before asking the server (each keystroke restarts the wait)
      this.searchTimer = setTimeout(() => {
        this.http
        //await this.http.get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId , options)
          .get<IProduct[]>(API_URL+'/products/get_products_by_company_containing?product_name_like=' + this.searchTerm, options)
          .subscribe(
            (data) => (this.filteredProducts = data),
            (error) => console.error('Error fetching products:', error)
          );
      }, 300)
    } else {
      this.filteredProducts = [];
    }
  }

  searchProduct = async (id : any) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.clearSupplierProduct()
  
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
      this.filteredProducts = [];
      
    }

    selectSupplier(id : any){
      this.clearSupplierProduct()
      this.selectedSupplierId = id
    }


    updateSupplierProduct = async () => {
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
    
        var supplierProduct = {
          productId : this.productId,
          supplierId : this.selectedSupplierId,
          vatRate : this.vat/100,
          costPriceVatIncl : this.costPriceVatIncl,
          maxSupplyQty : this.maxSupplyQty
        }
    
        await this.http.post<ISupplierProduct>(API_URL+'/supplier_products/update', supplierProduct, options)
        .toPromise()
        .then(
          data => {
            
            console.log(data)
            this.msg.showSuccessMessage('Product updated successfully')
            // this.loadSupplierProductsByBranch()
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