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
import { IRestaurantDineable } from 'src/app/domain/restaurant-dineable';
import { IDineable } from 'src/app/domain/dineable';
import { JwtHelperService } from '@auth0/angular-jwt';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-restaurant-dineable-stock-status',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-dineable-stock-status.component.html',
  styleUrl: './restaurant-dineable-stock-status.component.scss'
})
export class RestaurantDineableStockStatusComponent {
restaurantId: number;

  restaurantDineables : IRestaurantDineable[] = []

  searchKey : string = ''

  dineableId : any
  dineableName : string = ''
  dineableDescription : string = ''
  dineableCode : string = ''

  selectedRestaurant : IRestaurant

  restaurantName : string = ''

  currentStock : number = 0

  searchedDineables : IDineable[] = []

  isUserTyping: boolean = true; // Flag to detect user typing

  showImportList : boolean = false

  importDineables : IDineable[] = []

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
    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    this.loadRestaurantDineableStockStatus()
    this.loadSelectedRestaurant()
  }

  showRestaurantDineables: IRestaurantDineable[] = [];

  loadRestaurantDineableStockStatus = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.restaurantDineables = []

    await this.http.get<IRestaurantDineable[]>(API_URL+'/restaurant_dineables/get_stock_by_restaurant?restaurant_id=' + this.restaurantId, options)
    .toPromise()
    .then(
      data => {
        this.restaurantDineables = data!

        this.showRestaurantDineables = this.restaurantDineables

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
    this.showRestaurantDineables = []
    this.restaurantDineables.forEach(element => {
      if((+element.currentStock) < element.minStock){
        this.showRestaurantDineables.push(element)
      }
    })
  }

  filterOutofStock(){
    this.showRestaurantDineables = []
    this.restaurantDineables.forEach(element => {
      if((+element.currentStock) <= 0){
        this.showRestaurantDineables.push(element)
      }
    })
  }

  restaurantDineableId : any = null
  vat : number = 0
  costPriceVatIncl : number = 0
  sellingPriceVatIncl : number = 0
  costPriceVatExcl : number = 0
  sellingPriceVatExcl : number = 0
  minStock : number = 0
  maxStock : number = 0
  defaultReorderQty : number = 0
  defaultReorderLevel : number = 0
  importRestaurantDineable = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var restaurantDineable = {
      dineableId : this.dineableId,
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

    await this.http.post<IRestaurantDineable>(API_URL+'/restaurant_dineables/create', restaurantDineable, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Dineable imported successfully')
        this.loadRestaurantDineableStockStatus()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  updateRestaurantDineable = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var restaurantDineable = {
      dineableId : this.dineableId,
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

    await this.http.post<IRestaurantDineable>(API_URL+'/restaurant_dineables/update', restaurantDineable, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Dineable updated successfully')
        this.loadRestaurantDineableStockStatus()
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

    var restaurantDineable = {
      dineableId : this.dineableId,
      restaurantId : this.restaurantId,
      currentStock : this.currentStock
    }

    await this.http.post<IRestaurantDineable>(API_URL+'/restaurant_dineables/adjust_stock', restaurantDineable, options)
    .toPromise()
    .then(
      data => {
        
        console.log(data)
        this.msg.showSuccessMessage('Dineable stock updated successfully')
        this.loadRestaurantDineableStockStatus()
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )


  }

  clearRestaurantDineable(){
  this.dineableId = null

  this.dineableName = ''
  this.dineableCode = ''
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

  getCompanyDineableLike = async (searchKey : string) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IDineable[]>(API_URL+'/dineables/get_dineables_by_company?dineable_name_like=' + searchKey , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.searchedDineables = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

  searchDineable = async (id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearRestaurantDineable()

    await this.http.get<IDineable>(API_URL+'/dineables/get?id=' + id , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.dineableId = data!.id
          this.dineableCode = data!.code
          this.dineableName = data!.name
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
    
  }

  searchRestaurantDineable = async (id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearRestaurantDineable()

    await this.http.get<IRestaurantDineable>(API_URL+'/restaurant_dineables/get?id=' + id + '&restaurant_id=' + this.restaurantId , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.dineableId = data!.dineableId
          this.dineableCode = data!.dineableCode
          this.dineableName = data!.dineableName
          this.dineableDescription = data!.dineableDescription
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
  onDineableSelected(): void {
    const selectedDineable = this.searchedDineables.find(
      (dineable) => dineable.name === this.searchKey
    );

    if (selectedDineable) {
      this.searchDineable(selectedDineable.id);
      this.searchedDineables = []; // Clear suggestions after selection
    } else {
      console.warn('Selected dineable is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
  }

  // Triggered on every keystroke
  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getCompanyDineableLike(searchText);
    }
  }


  toggleShowImportList = async () => {

    if(this.showImportList){
      this.importDineables = []
      this.showImportList = false
      return
    }
    await this.getDineablesToImport()
    if(this.importDineables.length == 0){
      this.msg.showErrorMessage3('No dineables to import')
      return
    }
    this.showImportList = true
  }
  

  getDineablesToImport = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IDineable[]>(API_URL+'/dineables/get_company_sellable_dineables', options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.importDineables = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

  clearImportList = () => {
    this.importDineables = []
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
