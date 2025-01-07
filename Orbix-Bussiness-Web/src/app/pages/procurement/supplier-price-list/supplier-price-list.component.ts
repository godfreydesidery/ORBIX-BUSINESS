import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { ICompany } from 'src/app/domain/company';
import { IProduct } from 'src/app/domain/product';
import { ISupplier } from 'src/app/domain/supplier';
import { ISupplierProduct } from 'src/app/domain/supplier-product';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-supplier-price-list',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './supplier-price-list.component.html',
  styleUrl: './supplier-price-list.component.scss'
})
export class SupplierPriceListComponent {
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

  /**Identifiers */
  supplierId : string = ''


  page: number = 1; // Initialize the current page to 1
  filterRecords : string = ''
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
    this.suppliers = []

    await this.http.get<ISupplier[]>(API_URL+'/suppliers', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.suppliers.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
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

  loadSupplierProductsByBranch = async (id : any) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      this.supplierProducts = []
  
      await this.http.get<ISupplierProduct[]>(API_URL+'/supplier_products/get_all_by_supplier_and_branch?supplier_id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.supplierProducts = data!
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        }
      )
    }


    searchTerm: string = '';
  filteredProducts: any[] = [];
  selectedProduct: any | null = null;
  isDropdownOpen: boolean = false;

  selectProduct(product: any): void {
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