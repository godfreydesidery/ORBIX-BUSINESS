import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IStorage } from 'src/app/domain/storage';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { IStorageBillReceivable } from 'src/app/domain/bill-receivable';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-storage-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './storage-billing.component.html',
  styleUrl: './storage-billing.component.scss'
})
export class StorageBillingComponent {
page: number = 1; // Initialize the current page to 1

  filterRecords : string = ''

  startedAt : Date | null
  endedAt : Date | null
  billingType : string
  qty : number
  price : number
  discount : number

  storageId : any

  autoBilling : any = 1

  storageAmount : number

  billingStartAt : string = ''
  ////////////////////////////////////////////


  // Storage attributes
  // storageId : any = null
  storageNo : string = ''
  storageBillReceivableId : any = null
  storageBillReceivableDescription : string = ''
  storageBillReceivableStartingDate : Date | null
  storageBillReceivableEndingDate : Date | null
  storageBillReceivablePrice : number = 0
  storageBillReceivableQty : number = 0
  storageBillReceivableDiscount : number = 0
  storageBillReceivableAmount : number = 0
  storageBillReceivableStatus : string = ''

  // Service attributes
  serviceBillReceivableId : any = null
  serviceBillReceivableDate : Date | null
  serviceBillReceivableDescription : string = ''
  serviceBillReceivablePrice : number = 0
  serviceBillReceivableQty : number = 0
  serviceBillReceivableDiscount : number = 0
  serviceBillReceivableStatus : string = ''
  serviceBillReceivableAmount : number = 0

  //Storage and bills Collections attributes
  storageBillReceivables : IStorageBillReceivable[] = []
  // serviceBillReceivables : IServiceBillReceivable[] = []
  // billReceivables : IBillReceivable[] = []
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


  goodName : string = ''
  goodTypeId: any = ''
  goodTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  warehouseName : string = ''

  initialQty : number = 0





  ////////////////////////////////////////




  /**Collections */
  storages : IStorage[] = []
  // storageBillReceivables : IStorageBillReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private msg : MsgBoxService
  ) {}


  ngOnInit(){
    this.getAllCheckedInStorages()   
  }

  async getAllCheckedInStorages(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.storages = []

    await this.http.get<IStorage[]>(API_URL+'/storages/get_all_checked_in', options)
    .toPromise()
    .then(
      data => {
        data?.reverse()
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.storages.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getStorageBillReceivables(storageId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.storageBillReceivables = []

    await this.http.get<IStorageBillReceivable[]>(API_URL+'/storages/get_storage_bill_receivables?storage_id=' + storageId, options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.storageBillReceivables.push(element)
          sn = sn + 1
        })
        this.qty = 0
        this.price = 0
        this.discount = 0
        this.storageAmount = 0
        this.getStorage(storageId)
        console.log(data)
      }
    )
  }

  async getStorage(storageId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IStorage>(API_URL+'/storages/get?id=' + storageId, options)
    .toPromise()
    .then(
      data => {
        this.storageId = data!.id
        this.billingType = data!.billingType
        this.price = data!.billingAmount
      }
    )
  }

  async createStorageBillReceivable(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var storageBillReceivable = {
      startedAt : this.startedAt,
      endedAt : this.endedAt,
      billingType : this.billingType,
      qty : this.qty,
      price : this.price,
      discount : this.discount,
      autoBilling : this.autoBilling,
      storageId : this.storageId
    }

    await this.http.post<IStorageBillReceivable>(API_URL+'/storages/create_storage_bill_receivable', storageBillReceivable, options)
    .toPromise()
    .then(
      data => {
        this.getStorageBillReceivables(this.storageId)
        console.log(data)
      }
    )
    .catch(
      error => {
        this.msg.showErrorMessage(error, 'Error')
        console.log(error)
      }
    )


  }

  clearStorageBill(){
    this.startedAt = null
    this.endedAt = null
    this.qty = 0
    this.price = 0
    this.discount = 0
    this.storageId = null
    this.autoBilling = 1
    this.storageAmount = 0
  }

  doAutoBilling(){
    this.clearStorageBill()
  }

  doCustomBilling(){
    this.autoBilling = 0
  }

  refreshStorageAmounts(){
    this.storageAmount = (this.price * this.qty) - this.discount
  }


  async storageBilling(storageId : any){

    localStorage.setItem('storage-id', '');
    localStorage.setItem('storage-id', storageId);

    await this.router.navigate(['app/accounts-and-finance/good-billing'], { 
      queryParams: { storage_id: storageId}
    });

  }

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IStorage>(API_URL+'/storages/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.startBillingAt = null
        this.showStorageData(data!)
        console.log(data)
      }
    )
  }

  showStorageData(data : IStorage){

    this.storageId = data?.id
    this.storageNo = data?.no

    this.ownerFirstName = data?.ownerFirstName
    this.ownerMiddleName = data?.ownerMiddleName
    this.ownerLastName = data?.ownerLastName
    this.ownerCompanyName = data?.ownerCompanyName
    this.ownerIdNo = data?.ownerIdNo
    this.ownerIdType = data?.ownerIdType
    this.ownerPhoneNo = data?.ownerPhoneNo
    this.ownerEmail = data?.ownerEmail
    this.ownerAddress = data?.ownerAddress

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount
    this.billingStartAt = data?.billingStartAt

    this.initialQty = data?.initialQty

    this.goodName = data!.goodName

    this.goodTypeName = data!.goodTypeName
    this.warehouseName = data!.warehouseName
     this.billingType = data!.billingType
     this.status = data!.status
     this.validUntilDate = null
     this.comments = data!.comments// check this

  }

  clearStorageData(){
    this.storageId = null
    this.storageNo = ''
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
    this.goodTypeName = ''

    this.initialQty = 0

    this.goodName = ''

    this.goodCategory = ''

    this.warehouseName = ''
    this.billingType = ''

    this.color = ''
    this.comments = ''
  }

}

