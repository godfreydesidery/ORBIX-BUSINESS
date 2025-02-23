import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IMaintenance } from 'src/app/domain/maintenance';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { version } from 'moment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-maintenance-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './maintenance-billing.component.html',
  styleUrl: './maintenance-billing.component.scss'
})
export class MaintenanceBillingComponent {
page: number = 1; // Initialize the current page to 1

  filterRecords : string = ''

  startedAt : Date | null
  endedAt : Date | null
  billingType : string
  qty : number
  price : number
  discount : number

  maintenanceId : any

  autoBilling : any = 1

  maintenanceAmount : number

  billingStartAt : string = ''
  ////////////////////////////////////////////


  // Maintenance attributes
  // maintenanceId : any = null
  maintenanceNo : string = ''
  documentHeader: any;
  invoice: any;

  cash : number = 0

  mpesa : number = 0
  mpesaRefNo : string = ''


  /////////////////////////////////////////////


  // Owner information
  ownerFirstName: string = ''
  ownerMiddleName: string = ''
  ownerLastName: string = ''
  ownerCompanyName: string = ''
  ownerIdNo: string = ''
  ownerIdType: string = ''
  ownerPhoneNo: string = ''
  ownerEmail: string = ''
  ownerAddress: string = ''

  color : string = ''

  validUntilDate : Date | null = new Date()

  comments : string = ''

  cardNo : string = ''

  goodCategory : string = ''

  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  

  startBillingAt : Date | null


  vehicleEquipmentName : string = ''
  goodTypeId: any = ''
  vehicleEquipmentTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  warehouseName : string = ''





  ////////////////////////////////////////




  /**Collections */
  maintenances : IMaintenance[] = []
  // maintenanceBillReceivables : IMaintenanceBillReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private msg : MsgBoxService
  ) {}


  ngOnInit(){
    this.getAllCheckedInMaintenances()   
  }

  async getAllCheckedInMaintenances(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.maintenances = []

    await this.http.get<IMaintenance[]>(API_URL+'/maintenances/get_all_checked_in', options)
    .toPromise()
    .then(
      data => {
        data?.reverse()
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.maintenances.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getMaintenance(maintenanceId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IMaintenance>(API_URL+'/maintenances/get?id=' + maintenanceId, options)
    .toPromise()
    .then(
      data => {
        this.maintenanceId = data!.id
      }
    )
  }

  refreshMaintenanceAmounts(){
    this.maintenanceAmount = (this.price * this.qty) - this.discount
  }


  async maintenanceBilling(maintenanceId : any){

    localStorage.setItem('maintenance-id', '');
    localStorage.setItem('maintenance-id', maintenanceId);

    await this.router.navigate(['app/accounts-and-finance/maintenance-vehicle-equipment-billing'], { 
      queryParams: { maintenance_id: maintenanceId}
    });

  }

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IMaintenance>(API_URL+'/maintenances/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.startBillingAt = null
        this.showMaintenanceData(data!)
        console.log(data)
      }
    )
  }

  showMaintenanceData(data : IMaintenance){

    this.maintenanceId = data?.id
    this.maintenanceNo = data?.no

    this.ownerFirstName = data?.ownerFirstName
    this.ownerMiddleName = data?.ownerMiddleName
    this.ownerLastName = data?.ownerLastName
    this.ownerCompanyName = data?.ownerCompanyName
    this.ownerIdNo = data?.ownerIdNo
    this.ownerIdType = data?.ownerIdType
    this.ownerPhoneNo = data?.ownerPhoneNo
    this.ownerEmail = data?.ownerEmail
    this.ownerAddress = data?.ownerAddress


    this.vehicleEquipmentName = data!.vehicleEquipmentName
    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName


     this.status = data!.status
     this.validUntilDate = null
     this.comments = data!.comments// check this

  }

  clearMaintenanceData(){
    this.maintenanceId = null
    this.maintenanceNo = ''
    this.ownerFirstName = ''
    this.ownerMiddleName = ''
    this.ownerLastName = ''
    this.ownerCompanyName = ''
    this.ownerIdNo = ''
    this.ownerIdType = ''
    this.ownerPhoneNo = ''
    this.ownerEmail = ''
    this.ownerAddress = ''

    this.billingType = ''
    this.billingAmount = 0
    this.billingStartAt = ''
    this.vehicleEquipmentName = ''

    this.vehicleEquipmentTypeName = ''

    this.goodCategory = ''

    this.warehouseName = ''
    this.billingType = ''

    this.color = ''
    this.comments = ''
  }

}