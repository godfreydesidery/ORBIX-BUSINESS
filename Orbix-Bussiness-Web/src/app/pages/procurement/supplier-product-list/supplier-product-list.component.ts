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
import { ISupplier } from 'src/app/domain/supplier';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { IProduct } from 'src/app/domain/product';
import { JwtHelperService } from '@auth0/angular-jwt';
import { ISupplierProduct } from 'src/app/domain/supplier-product';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-supplier-product-list',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './supplier-product-list.component.html',
  styleUrl: './supplier-product-list.component.scss'
})
export class SupplierProductListComponent {
  supplierId: number;

  supplierProducts : ISupplierProduct[] = []

  searchKey : string = ''

  productId : any
  productName : string = ''
  productDescription : string = ''
  productCode : string = ''

  selectedSupplier : ISupplier

  supplierName : string = ''

  maxSupplyQty : number = 0

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

  async ngOnInit(): Promise<void> {

    this.getProductsToImport()
    await this.loadAvailableSuppliers()
  }

  loadSupplierProductsByBranch = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.supplierProducts = []

    await this.http.get<ISupplierProduct[]>(API_URL+'/supplier_products/get_all_by_supplier_and_branch?supplier_id=' + this.selectedSupplierId, options)
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

  supplierProductId : any = null
  vat : number = 0
  costPriceVatIncl : number = 0

  importSupplierProduct = async () => {
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
        this.loadSupplierProductsByBranch()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


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
        this.loadSupplierProductsByBranch()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  

  clearSupplierProduct(){
  this.productId = null

  this.productName = ''
  this.productCode = ''
  this.vat = 0
  this.costPriceVatIncl = 0
  this.maxSupplyQty = 0

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

    await this.http.get<IProduct[]>(API_URL+'/products/get_company_products', options)
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




  ///////////////////////////////////////

  availableSuppliers :ISupplier[] = []
  
  selectedSupplierId :any = null

  branchId :string = ''

  supplierLoaded :boolean = false

      loadAvailableSuppliers = async () => {
  
        this.availableSuppliers = []
  
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
  
        await this.http.get<ISupplier[]>(API_URL+'/suppliers' , options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              
              this.availableSuppliers = data!
              
            }
          )
      }
  
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

      filteredSuppliers : ISupplier[] = []
  
    async onSupplierChange(event: any): Promise<void> {
      this.selectedSupplierId = await event.target.value;
      await this.loadSelectedSupplier()
      await this.loadSupplierProductsByBranch()
    }
  
    selectSupplier(supplier: any){
      //localStorage.setItem('selected-supplier-id', this.selectedSupplierId!);
      if(this.selectedSupplierId! === '' || this.selectedSupplierId === null){
        this.msg.showErrorMessage3('Please select a supplier first')
        return
      }
      this.loadSelectedSupplier()
    }
  
    clearSelectedSupplier(){
      this.selectedSupplierId = null     
    }


  ////////////////////////////////////

  searchedSuppliers : ISupplier[] = []

  
  onSupplierSelected(): void {
    const selectedProduct = this.searchedSuppliers.find(
      (supplier: { name: string; }) => supplier.name === this.searchKey
    );

    if (selectedProduct) {
      this.searchProduct(selectedProduct.id);
      this.searchedProducts = []; // Clear suggestions after selection
    } else {
      console.warn('Selected supplier is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
  }

  isSupplierDropdownOpen : false
  // Triggered on every keystroke
  onSupplierInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getSupplierLike(searchText);
    }
  }

  getSupplierLike = async (searchKey : string) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<ISupplier[]>(API_URL+'/suppliers/get_supplier_by_name_containing?supplier_name_like=' + searchKey, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.searchedSuppliers = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

  searchSupplier = async (id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearSupplierProduct()

    await this.http.get<ISupplier>(API_URL+'/suppliers/get?id=' + this.supplierId , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.supplierId = data!.id
         // this.supplierCode = data!.code
          this.supplierName = data!.name
          
        }
      )
      .catch(error => {
        console.log(error)
        
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




 
  

}
