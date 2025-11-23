import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IParking } from 'src/app/domain/parking';
import { IParkingZone } from 'src/app/domain/parking-zone';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { IParkingBillReceivable } from 'src/app/domain/bill-receivable';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { IBillView } from 'src/app/domain/bill-view';

const API_URL = environment.apiUrl;


@Component({
  selector: 'az-parking-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './parking-billing.component.html',
  styleUrl: './parking-billing.component.scss'
})
export class ParkingBillingComponent {

  page: number = 1; // Initialize the current page to 1

  filterRecords : string = ''

  startedAt : Date | null
  endedAt : Date | null
  billingType : string
  qty : number
  price : number
  discount : number

  parkingId : any

  autoBilling : any = 1

  parkingAmount : number

  billingStartAt : string = ''
  ////////////////////////////////////////////


  // Parking attributes
  // parkingId : any = null
  parkingNo : string = ''
  parkingBillReceivableId : any = null
  parkingBillReceivableDescription : string = ''
  parkingBillReceivableStartingDate : Date | null
  parkingBillReceivableEndingDate : Date | null
  parkingBillReceivablePrice : number = 0
  parkingBillReceivableQty : number = 0
  parkingBillReceivableDiscount : number = 0
  parkingBillReceivableAmount : number = 0
  parkingBillReceivableStatus : string = ''

  // Service attributes
  serviceBillReceivableId : any = null
  serviceBillReceivableDate : Date | null
  serviceBillReceivableDescription : string = ''
  serviceBillReceivablePrice : number = 0
  serviceBillReceivableQty : number = 0
  serviceBillReceivableDiscount : number = 0
  serviceBillReceivableStatus : string = ''
  serviceBillReceivableAmount : number = 0

  //Parking and bills Collections attributes
  parkingBillReceivables : IParkingBillReceivable[] = []
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

  // Agent Information
  agentName: string = ''
  agentAddress: string = ''
  agentPhoneNo: string = ''
  agentEmail: string = ''
  tformNumber: string = ''

  // Vehicle or Equipment Information
  registrationNo: string = ''
  chasisNo: string = ''
  leftFrontLamp: string = ''
  rightFrontLamp: string = ''
  leftRearLamp: string = ''
  rightRearLamp: string = ''
  leftSideMirror: string = ''
  rightSideMirror: string = ''
  leftWiper: string = ''
  rightWiper: string = ''
  backWiper: string = ''
  fuelCap: string = ''
  spareTire: string = ''
  battery: string = ''
  starter: string = ''
  aerial: string = ''
  wheelCap: string = ''
  roundMirror: string = ''
  tireIndicator: string = ''
  hasKeys : string = ''

  color : string = ''

  validUntilDate : Date | null = new Date()

  comments : string = ''

  cardNo : string = ''

  vehicleEquipmentCategory : string = ''

  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  

  startBillingAt : Date | null


  vehicleEquipmentTypeId: any = ''
  vehicleEquipmentTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  parkingZoneName : string = ''





  ////////////////////////////////////////




  /**Collections */
  parkings : IParking[] = []
  // parkingBillReceivables : IParkingBillReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private msg : MsgBoxService
  ) {}


  ngOnInit(){
    this.getAllCheckedInParkings()   
  }

  async getAllCheckedInParkings(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkings = []

    await this.http.get<IParking[]>(API_URL+'/parkings/get_all_checked_in', options)
    .toPromise()
    .then(
      data => {
        data?.reverse()
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.parkings.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getParkingBillReceivables(parkingId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingBillReceivables = []

    await this.http.get<IParkingBillReceivable[]>(API_URL+'/parkings/get_parking_bill_receivables?parking_id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.parkingBillReceivables.push(element)
          sn = sn + 1
        })
        this.qty = 0
        this.price = 0
        this.discount = 0
        this.parkingAmount = 0
        this.getParking(parkingId)
        console.log(data)
      }
    )
  }

  async getParking(parkingId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParking>(API_URL+'/parkings/get?id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        this.parkingId = data!.id
        this.billingType = data!.billingType
        this.price = data!.billingAmount
      }
    )
  }

  async createParkingBillReceivable(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parkingBillReceivable = {
      startedAt : this.startedAt,
      endedAt : this.endedAt,
      billingType : this.billingType,
      qty : this.qty,
      price : this.price,
      discount : this.discount,
      autoBilling : this.autoBilling,
      parkingId : this.parkingId
    }

    await this.http.post<IParkingBillReceivable>(API_URL+'/parkings/create_parking_bill_receivable', parkingBillReceivable, options)
    .toPromise()
    .then(
      data => {
        this.getParkingBillReceivables(this.parkingId)
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

  clearParkingBill(){
    this.startedAt = null
    this.endedAt = null
    this.qty = 0
    this.price = 0
    this.discount = 0
    this.parkingId = null
    this.autoBilling = 1
    this.parkingAmount = 0
  }

  doAutoBilling(){
    this.clearParkingBill()
  }

  doCustomBilling(){
    this.autoBilling = 0
  }

  refreshParkingAmounts(){
    this.parkingAmount = (this.price * this.qty) - this.discount
  }


  async vehicleEquipmentBilling(parkingId : any){

    localStorage.setItem('parking-id', '');
    localStorage.setItem('parking-id', parkingId);

    await this.router.navigate(['app/accounts-and-finance/vehicle-equipment-billing'], { 
      queryParams: { parking_id: parkingId}
    });

  }

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParking>(API_URL+'/parkings/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.startBillingAt = null
        this.showParkingData(data!)
        console.log(data)
      }
    )
  }

  billPaid : number = 0
  billGenerated : number = 0
  billUngenerated : number = 0
  billUnpaid : number = 0

  async getBillView(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.billPaid = 0
    this.billGenerated = 0
    this.billUngenerated = 0
    this.billUnpaid = 0

    await this.http.get<IBillView>(API_URL+'/parking_bill_receivables/get_bill_view?parking_id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.billPaid = data!.billPaid
        this.billGenerated = data!.billGenerated
        this.billUngenerated = data!.billUngenerated
        this.billUnpaid = data!.billUnpaid
        console.log(data)
      }
    )
  }

  showParkingData(data : IParking){

    this.parkingId = data?.id
    this.parkingNo = data?.no

    this.ownerFirstName = data?.ownerFirstName;
    this.ownerMiddleName = data?.ownerMiddleName;
    this.ownerLastName = data?.ownerLastName;
    this.ownerCompanyName = data?.ownerCompanyName;
    this.ownerIdNo = data?.ownerIdNo;
    this.ownerIdType = data?.ownerIdType;
    this.ownerPhoneNo = data?.ownerPhoneNo;
    this.ownerEmail = data?.ownerEmail;
    this.ownerAddress = data?.ownerAddress;

    // Agent Information
    this.agentName = data?.agentName;
    this.agentAddress = data?.agentAddress;
    this.agentPhoneNo = data?.agentPhoneNo;
    this.agentEmail = data?.agentEmail;
    this.tformNumber = data?.tformNumber;

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount
    this.billingStartAt = data?.billingStartAt

    // Vehicle or Equipment Information
    this.registrationNo = data?.registrationNo;
    this.chasisNo = data?.chasisNo;
    this.leftFrontLamp = data?.leftFrontLamp == true ? 'YES' : 'NO'
    this.rightFrontLamp = data?.rightFrontLamp == true ? 'YES' : 'NO'
    this.leftRearLamp = data?.leftRearLamp == true ? 'YES' : 'NO'
    this.rightRearLamp = data?.rightRearLamp == true ? 'YES' : 'NO'
    this.leftSideMirror = data?.leftSideMirror == true ? 'YES' : 'NO'
    this.rightSideMirror = data?.rightSideMirror == true ? 'YES' : 'NO'
    this.leftWiper = data?.leftWiper == true ? 'YES' : 'NO'
    this.rightWiper = data?.rightWiper == true ? 'YES' : 'NO'
    this.backWiper = data?.backWiper == true ? 'YES' : 'NO'
    this.fuelCap = data?.fuelCap == true ? 'YES' : 'NO'
    this.spareTire = data?.spareTire == true ? 'YES' : 'NO'
    this.battery = data?.battery == true ? 'YES' : 'NO'
    this.starter = data?.starter == true ? 'YES' : 'NO'
    this.aerial = data?.aerial == true ? 'YES' : 'NO'
    this.wheelCap = data?.wheelCap == true ? 'YES' : 'NO'
    this.roundMirror = data?.roundMirror == true ? 'YES' : 'NO'
    this.tireIndicator = data?.tireIndicator == true ? 'YES' : 'NO'
    this.hasKeys = data?.hasKeys == true ? 'YES' : 'NO'
    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName,

    this.vehicleEquipmentCategory = data!.vehicleEquipmentCategory

    this.parkingZoneName = data!.parkingZoneName,
     this.cardNo = data!.cardNo

     this.billingType = data!.billingType

     this.status = data!.status

     this.color = data!.vehicleEquipmentColor

     this.validUntilDate = null

     this.comments = data!.comments// check this

  }

  clearParkingData(){
    this.parkingId = null
    this.parkingNo = ''
    this.ownerFirstName = ''
    this.ownerMiddleName = ''
    this.ownerLastName = ''
    this.ownerCompanyName = ''
    this.ownerIdNo = ''
    this.ownerIdType = ''
    this.ownerPhoneNo = ''
    this.ownerEmail = ''
    this.ownerAddress = ''

    // Agent Information
    this.agentName = ''
    this.agentAddress = ''
    this.agentPhoneNo = ''
    this.agentEmail = ''
    this.tformNumber = ''

    this.billingType = ''
    this.billingAmount = 0
    this.billingStartAt = ''

    // Vehicle or Equipment Information
    this.registrationNo = ''
    this.chasisNo = ''
    this.leftFrontLamp = ''
    this.rightFrontLamp = ''
    this.leftRearLamp = ''
    this.rightRearLamp = ''
    this.leftSideMirror = ''
    this.rightSideMirror = ''
    this.leftWiper = ''
    this.rightWiper = ''
    this.backWiper = ''
    this.fuelCap = ''
    this.spareTire = ''
    this.battery = ''
    this.starter = ''
    this.aerial = ''
    this.wheelCap = ''
    this.roundMirror = ''
    this.tireIndicator = ''
    this.vehicleEquipmentTypeName = ''

    this.vehicleEquipmentCategory = ''

    this.parkingZoneName = ''

    this.hasKeys = ''

    this.billingType = ''

    this.color = ''
    this.comments = ''
  }

}
