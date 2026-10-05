import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { IMaintenance } from 'src/app/domain/maintenance';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { version } from 'moment';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-maintenance-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './maintenance-billing.component.html',
  styleUrl: './maintenance-billing.component.scss'
})
export class MaintenanceBillingComponent {
  trackById = trackById
page: number = 1; // Initialize the current page to 1
  pageSize : number = 10
  listSearchTimer : any = null

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
  totalMaintenances : number = 0
  maintenancesRequest : number = 0 // number of the latest list request; answers to older ones are ignored
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
    // One page at a time, searched on the server against the shown columns
    var page = this.page
    var request = ++this.maintenancesRequest

    await this.http.get<IPage<IMaintenance>>(API_URL+'/maintenances/get_all_checked_in_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.maintenancesRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSize))
        if(page > lastPage){
          this.page = lastPage
          this.getAllCheckedInMaintenances()
          return
        }
        var sn = (page - 1) * this.pageSize + 1
        data!.content.forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        this.maintenances = data!.content
        this.totalMaintenances = data!.totalElements
      }
    )
  }

  pageChanged(page : number){
    this.page = page
    this.getAllCheckedInMaintenances()
  }

  searchList(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimer)
    this.listSearchTimer = setTimeout(() => {
      this.page = 1
      this.getAllCheckedInMaintenances()
    }, 300)
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