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

import { ReceiptItem } from 'src/app/domain/receipt-item';
import { ICustomer } from 'src/app/domain/customer';
import { IRestaurantDineable } from 'src/app/domain/restaurant-dineable';
import { IDineable } from 'src/app/domain/dineable';
import { IRestaurantSalesOrder, IRestaurantSalesOrderDetail } from 'src/app/domain/restaurant-sales-order';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-restaurant-sales-order',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-sales-order.component.html',
  styleUrl: './restaurant-sales-order.component.scss'
})
export class RestaurantSalesOrderComponent {
restaurantId: number;

  restaurantDineables : IRestaurantDineable[] = []

  searchKey : string = ''

  dineableId : any
  dineableName : string = ''
  dineableDescription : string = ''
  dineableCode : string = ''
  discount : number = 0

  selectedRestaurant : IRestaurant

  restaurantName : string = ''

  searchedDineables : IDineable[] = []

  isUserTyping: boolean = true; // Flag to detect user typing

  showImportList : boolean = false

  importDineables : IDineable[] = []

  restaurantSalesOrders : IRestaurantSalesOrder[] = []

  restaurantSalesOrderId : any = null

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
    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    await this.loadSelectedRestaurant()
    //await this.getAllPendingOrders()
  }





  searchTerm: string = '';
  filteredDineables: any[] = [];
  selectedDineable: any | null = null;
  isDropdownOpen: boolean = false;
  searchDineables(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    
    }
    this.filteredDineables = [];
    if (this.searchTerm.trim().length >= 2) {
      this.http
      //await this.http.get<IDineable[]>(API_URL+'/restaurant_dineables/get_dineables_by_restaurant_containing?dineable_name_like=' + searchKey + '&restaurant_id=' + this.restaurantId , options)
        .get<IDineable[]>(API_URL+'/restaurant_dineables/get_dineables_by_restaurant_containing?dineable_name_like=' + this.searchTerm + '&restaurant_id=' + this.restaurantId , options)
        .subscribe(
          (data) => (this.filteredDineables = data),
          (error) => console.error('Error fetching dineables:', error)
        );
    } else {
      this.filteredDineables = [];
    }
  }

  selectDineable(dineable: any): void {
    this.selectedDineable = dineable;
    this.searchTerm = dineable.name;
    this.isDropdownOpen = false;
    
    this.searchDineableInRestaurant(dineable.id)
    this.filteredDineables = [];
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



    async getAllPendingOrders(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.restaurantSalesOrders = []
    
      await this.http.get<IRestaurantSalesOrder[]>(API_URL+'/restaurant_sales_orders/get_all_pending_by_restaurant?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.restaurantSalesOrders.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }

    restaurantSalesOrder! : IRestaurantSalesOrder

    totalAmount : number = 0

    async get(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IRestaurantSalesOrder>(API_URL+'/restaurant_sales_orders/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)
          console.log(data)

          this.restaurantSalesOrder = data!

          this.restaurantSalesOrderId = this.restaurantSalesOrder.id

          this.customerName = data!.customerName

          this.totalAmount = 0

          var sn = 1
          this.restaurantSalesOrder.restaurantSalesOrderDetails.forEach(element => {
            element.sn = sn
            this.totalAmount = this.totalAmount + ((+element.sellingPriceVatIncl) * element.qty - (+element.discount))
            sn = sn + 1
            
          })

          this.receiptData = this.restaurantSalesOrder.restaurantSalesOrderDetails

        }
      )
    }


    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
    
      var restaurantSalesOrder = {
        id : null,
        no : null,
        summary : null,
        restaurantId : this.restaurantId,
        customerName : this.customerName
      }
    
      if(restaurantSalesOrder.id === null){
        /**Create new uom */
        await this.http.post<IRestaurantSalesOrder>(API_URL+'/restaurant_sales_orders/create', restaurantSalesOrder, options)
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
        await this.http.post<IRestaurantSalesOrder>(API_URL+'/restaurant_sales_orders/update', restaurantSalesOrder, options)
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
      this.restaurantSalesOrder!
      this.restaurantSalesOrderId = null
      this.customerName = ''
    }

    // Triggered on every keystroke
  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getRestaurantDineableLike(searchText);
    }
  }

  onDineableSelected(): void {
    const selectedDineable = this.searchedDineables.find(
      (dineable) => dineable.name === this.searchKey
    );

    if (selectedDineable) {
      this.searchDineableInRestaurant(selectedDineable.id)
      this.searchedDineables = []; // Clear suggestions after selection
    } else {
      console.warn('Selected dineable is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);
    
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
            this.dineableDescription =data!.description

           
            
          }
        )
        .catch(error => {
          console.log(error)
          
        }       
      ) 
      
    }

    salesOrderDetailId : any = null
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
    baseUom : string = ''

    qty : number = 0

    clearRestaurantDineable(){
      this.dineableId = null

      this.searchTerm = ''
    
      this.salesOrderDetailId = null
      this.searchKey = ''
      this.dineableName = ''
      this.dineableCode = ''
      this.dineableDescription = ''
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


  searchDineableInRestaurant = async (dineable_id : any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.clearRestaurantDineable()

    await this.http.get<IRestaurantDineable>(API_URL+'/restaurant_dineables/get_dineable_in_restaurant?dineable_id=' + dineable_id + '&restaurant_id=' + this.restaurantId , options)
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

  getRestaurantDineableLike = async (searchKey : string) => {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      await this.http.get<IDineable[]>(API_URL+'/restaurant_dineables/get_dineables_by_restaurant_containing?dineable_name_like=' + searchKey + '&restaurant_id=' + this.restaurantId , options)
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

    saveDetail(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var detail = {
        id : this.salesOrderDetailId,
        restaurantSalesOrderId : this.restaurantSalesOrderId,
        dineableId : this.dineableId,
        discount : this.discount,
        qty : this.qty
      }

      if(detail.id === null){
        /**Create new detail */
        this.http.post<IRestaurantSalesOrderDetail>(API_URL+'/restaurant_sales_orders/create_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.restaurantSalesOrderId)
            }
          )
          .catch(error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }       
        ) 
      }else if(detail.id !== null){
        /**Update detail */
        this.http.post<IRestaurantSalesOrderDetail>(API_URL+'/restaurant_sales_orders/update_detail', detail, options)
          .toPromise()
          .then(
            data => {
              console.log(data)
              this.get(this.restaurantSalesOrderId)
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
      this.http.get<IRestaurantSalesOrderDetail>(API_URL+'/restaurant_sales_orders/remove_detail?restaurant_sales_order_detail_id=' + id + '&restaurant_sales_order_id=' + this.restaurantSalesOrderId, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.restaurantSalesOrderId)
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
      await this.http.post<IRestaurantSalesOrder>(API_URL+'/restaurant_sales_orders/confirm?restaurant_sales_order_id=' + this.restaurantSalesOrderId + '&pay_code=' + this.payCode + '&pay_ref_no=' + this.payRefNo, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.restaurantSalesOrderId)
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
      this.http.post<IRestaurantSalesOrder>(API_URL+'/restaurant_sales_orders/cancel?restaurant_sales_order_id=' + this.restaurantSalesOrderId, null, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.restaurantSalesOrderId)
            this.msg.showSuccessMessage('Order canceled successifully')
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }       
      )
    }



    receiptData : IRestaurantSalesOrderDetail [] = []

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
          item.name = element.dineableName
          item.amount = element.sellingPriceVatIncl * (+element.qty) - (+element.discount)
          item.qty = element.qty
          items.push(item)
        })

        var customer: ICustomer = {
              name: '',
              address: '',
              phone: ''
            }
    
        this.printer.print(items, 'NA', 0, customer)
        //this.toPrintReceipt = false
      }

    
    
}
