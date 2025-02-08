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
import { IWarehouse } from 'src/app/domain/warehouse';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-select-warehouse',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-warehouse.component.html',
  styleUrl: './select-warehouse.component.scss'
})
export class SelectWarehouseComponent {
underStock : number = 0
  outofStock : number = 0


  availableWarehouses :IWarehouse[] = []

  selectedWarehouse :IWarehouse | null = null
  selectedWarehouseId :string | null = null

  branchId :string = ''

  warehouseLoaded :boolean = false 

  lpoPage: number = 1; // Initialize the current page to 1
  grnPage: number = 1; // Initialize the current page to 1
  filterLpoRecords : string = ''
  filterGrnRecords : string = ''
  selectedOption: string = '';
    
  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

    ngOnInit() {
      this.selectedWarehouseId = localStorage.getItem('selected-warehouse-id')
      if(this.selectedWarehouseId == '' || this.selectedWarehouseId == null){
        this.warehouseLoaded = false
        this.loadAvailableWarehouses()
      }else{      
        this.selectedWarehouseId = localStorage.getItem('selected-warehouse-id')
        this.loadSelectedWarehouse()
        
      }     
        
    }

    loadAvailableWarehouses = async () => {

      this.availableWarehouses = []

      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.get<IWarehouse[]>(API_URL+'/warehouses/get_branch_available_warehouses_by_user' , options)
        .toPromise()
        .then(
          data => {
            this.availableWarehouses = data!
            console.log(data)
          }
        )
    }

  loadSelectedWarehouse = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IWarehouse>(API_URL + '/warehouses/get_selected_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedWarehouse = data!
          localStorage.setItem('selected-warehouse-id', this.selectedWarehouse.id.toString());
          this.warehouseLoaded = true
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-warehouse-id', '');
        this.warehouseLoaded = false
      }
      )
  }

    onWarehouseChange(event: any): void {
      this.selectedWarehouseId = event.target.value;
      console.log('Selected Warehouse ID:', this.selectedWarehouseId);
      //alert('Selected Warehouse ID: ' + this.selectedWarehouseId);
  }

  selectWarehouse(){
    //localStorage.setItem('selected-warehouse-id', this.selectedWarehouseId!);
    if(this.selectedWarehouseId! === '' || this.selectedWarehouseId === null){
      this.msg.showErrorMessage3('Please select a warehouse first')
      return
    }
    this.loadSelectedWarehouse()
  }

  clearSelectedWarehouse(){
    localStorage.setItem('selected-warehouse-id', '');
    this.selectedWarehouseId = ''
    
  }

  lpos : ILpo[] = []
  async getAllPendingOrders(){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        this.lpos = []
      
        await this.http.get<ILpo[]>(API_URL+'/lpos/get_all_visible_by_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
        .toPromise()
        .then(
          data => {
            this.lpos = [...data!]
            
            console.log(data)
          }
        )
      }

      grns : IGrn[] = []
      async getAllPendingGrns(){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        this.grns = []
      
        await this.http.get<IGrn[]>(API_URL+'/grns/get_all_visible_by_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
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
        .catch(error => {
          console.log(error)
        })
      }


      lpoId : any = null

      async get(id : any){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        await this.http.get<ILpo>(API_URL+'/lpos/get?id=' + id, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
            console.log(data)
            this.lpoId = data!.id

            this.router.navigate(['/app/mechandizing/warehouse-lpo'], {
              queryParams: {
                warehouse_id: this.selectedWarehouseId,
                lpo_id: id
              }
            });
          }
        )
      }

      async getGrn(id : any){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        await this.http.get<ILpo>(API_URL+'/grns/get?id=' + id, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
            console.log(data)
            this.lpoId = data!.id

            this.router.navigate(['/app/mechandizing/warehouse-grn'], {
              queryParams: {
                warehouse_id: this.selectedWarehouseId,
                grn_id: id
              }
            });
          }
        )
      }

      getUnderstock = async () => {

        this.underStock = 0
  
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
  
        await this.http.get<number>(API_URL+'/warehouse_products/get_check_under_stock_by_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
          .toPromise()
          .then(
            data => {
              this.underStock = data!
              console.log(data)
            }
          )
      }

      getOutofstock = async () => {

        this.outofStock = 0
  
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
  
        await this.http.get<number>(API_URL+'/warehouse_products/get_check_out_of_stock_by_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
          .toPromise()
          .then(
            data => {
              this.outofStock = data!
              console.log(data)
            }
          )
      }






      // Alerts array to manage the notifications
  alerts: { 
    type: string; 
    message: string;
    link?: string
   }[] = [
    { type: 'success', message: 'Warehouse selected successfully!', link: 'warehouse-sales-order' },
    
  ];

  // Close alert method
  closeAlert(index: number) {
    this.alerts.splice(index, 1); // Remove the alert at the given index
  }

  // Add an alert for testing purposes
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }

}