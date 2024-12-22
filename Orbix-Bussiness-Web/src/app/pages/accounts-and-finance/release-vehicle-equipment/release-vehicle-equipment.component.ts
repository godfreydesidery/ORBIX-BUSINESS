import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IParking, IServiceBillItem } from 'src/app/domain/parking';
import { IParkingZone } from 'src/app/domain/parking-zone';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';
import { Byte, error } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import * as pdfMake from 'pdfmake/build/pdfmake';

import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';


const API_URL = environment.apiUrl;

@Component({
  selector: 'az-release-vehicle-equipment',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule
  ],
  templateUrl: './release-vehicle-equipment.component.html',
  styleUrl: './release-vehicle-equipment.component.scss'
})
export class ReleaseVehicleEquipmentComponent {
  documentHeader! : any

  page: number = 1; // Initialize the current page to 1

  filterRecords : string = ''

  id : any = null
  no : string = ''

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

  billingType : string = ''
  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  

  startBillingAt : Date | null

  // Foreign keys
  parkingId: any = ''
  vehicleEquipmentTypeId: any = ''
  vehicleEquipmentTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  parkingZoneName : string = ''

  

  /**Collections */
  parkings : IParking[] = []

  vehicleEquipmentTypes  : IVehicleEquipmentType[] = []

  parkingZones : IParkingZone[] = []

  
  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private data : DataService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getTodayCheckedOut()
    this.getAllCompanyActiveVehicleAndEquipmentTypes()
    this.getAllBranchActiveParkingZones()
  }

  async getAllClearedParkings(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkings = []

    await this.http.get<IParking[]>(API_URL+'/parkings/get_all_cleared', options)
    .toPromise()
    .then(
      data => {
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

  async getTodayCheckedOut(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkings = []

    await this.http.get<IParking[]>(API_URL+'/parkings/get_today_checked_out', options)
    .toPromise()
    .then(
      data => {
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

  async printGatePass(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParking>(API_URL+'/parkings/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        console.log(data)
        this.parkingId = data!.id
        this.printGatePassRcpt(data!.serviceBillItems, '', 0);
      }
    )
    .catch(error => {
      console.log(error)
    })
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

  async getAllCompanyActiveVehicleAndEquipmentTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.vehicleEquipmentTypes = []

    await this.http.get<IVehicleEquipmentType[]>(API_URL+'/vehicle_equipment_types/get_all_company_active', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.vehicleEquipmentTypes.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getAllBranchActiveParkingZones(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingZones = []

    await this.http.get<IParkingZone[]>(API_URL+'/parking_zones/get_all_branch_active', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.parkingZones.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  selectedOption: string = '';
  options: string[] = ['Option 1', 'Option 2', 'Option 3'];

  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id: this.id,
      no: this.no,
      ownerFirstName: this.ownerFirstName,
      ownerMiddleName: this.ownerMiddleName,
      ownerLastName: this.ownerLastName,
      ownerCompanyName: this.ownerCompanyName,
      ownerIdNo: this.ownerIdNo,
      ownerIdType: this.ownerIdType,
      ownerPhoneNo: this.ownerPhoneNo,
      ownerEmail: this.ownerEmail,
      ownerAddress: this.ownerAddress,

      // Agent Information
      agentName: this.agentName,
      agentAddress: this.agentAddress,
      agentPhoneNo: this.agentPhoneNo,
      agentEmail: this.agentEmail,
      tformNumber: this.tformNumber,

      billingType : this.billingType,

      billingAmount : this.billingAmount,

      // Vehicle or Equipment Information
      registrationNo: this.registrationNo,
      chasisNo: this.chasisNo,

      leftFrontLamp: this.leftFrontLamp === 'YES' ? 1 : 0,
      rightFrontLamp: this.rightFrontLamp === 'YES' ? 1 : 0,
      leftRearLamp: this.leftRearLamp === 'YES' ? 1 : 0,
      rightRearLamp: this.rightRearLamp === 'YES' ? 1 : 0,
      leftSideMirror: this.leftSideMirror === 'YES' ? 1 : 0,
      rightSideMirror: this.rightSideMirror === 'YES' ? 1 : 0,
      leftWiper: this.leftWiper === 'YES' ? 1 : 0,
      rightWiper: this.rightWiper === 'YES' ? 1 : 0,
      backWiper: this.backWiper === 'YES' ? 1 : 0,
      fuelCap: this.fuelCap === 'YES' ? 1 : 0,
      spareTire: this.spareTire === 'YES' ? 1 : 0,
      battery: this.battery === 'YES' ? 1 : 0,
      starter: this.starter === 'YES' ? 1 : 0,
      aerial: this.aerial === 'YES' ? 1 : 0,
      wheelCap: this.wheelCap === 'YES' ? 1 : 0,
      roundMirror: this.roundMirror === 'YES' ? 1 : 0,
      tireIndicator: this.tireIndicator === 'YES' ? 1 : 0,
      hasKeys : this.hasKeys === 'YES' ? 1 : 0,
      vehicleEquipmentTypeName : this.vehicleEquipmentTypeName,

      vehicleEquipmentCategory : this.vehicleEquipmentCategory,

      cardNo : this.cardNo,

      billintType : this.billingType,

      parkingZoneName : this.parkingZoneName
    }

    console.log(parking)

    if(parking.id === null){
      /**Create new parking */
      await this.http.post<IParking>(API_URL+'/parkings/create', parking, options)
      .toPromise()
      .then(
        data => {
          this.showParkingData(data!)

          console.log(data)

          this.getAllClearedParkings()

          this.msg.showSuccessMessage('Parking created successifully')
        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an exiisting parking */
      await this.http.post<IParking>(API_URL+'/parkings/update', parking, options)
      .toPromise()
      .then(
        data => {
          this.showParkingData(data!)

          console.log(data)

          this.getAllClearedParkings()

          this.msg.showSuccessMessage('Parking updated successifully')

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

  

  async activate(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id : id
    }

    await this.http.post<String>(API_URL+'/parkings/activate', parking, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllClearedParkings()

          this.msg.showSuccessMessage('Parking activated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async checkIn(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id : this.id,
      cardNo : this.cardNo,
      hasKeys : this.hasKeys === 'YES' ? 1 : 0,
      parkingZoneName : this.parkingZoneName,
      startBillingAt : this.startBillingAt
    }

    await this.http.post<IParking>(API_URL+'/parkings/check_in', parking, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllClearedParkings()

          this.msg.showSuccessMessage('Checked in Successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  lastBillingDate : string = ''
  async getLastBillingDate(parkingId : any){
    // this.lastBillingDate = ''
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IModel>(API_URL+'/parkings/get_last_parking_bill_date?id=' + parkingId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          // this.lastBillingDate = data!.stringData
          this.lastBillingDate = data!.stringData
        }
      )
      .catch(
        error => {
          console.log(error)
          // this.lastBillingDate = ''
          // this.msg.showErrorMessage(error, 'Error')
          this.lastBillingDate = ''
        }
      )
  }

/**
 * Check out the vehicle/equipment
 * @returns {Promise<void>}
 */
  async checkOut(): Promise<void>{


    if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false){
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id : this.id,
      cardNo : this.cardNo,
      parkingZoneName : this.parkingZoneName,
      startBillingAt : this.startBillingAt
    }

    await this.http.post<IParking>(API_URL+'/parkings/check_out', parking, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllClearedParkings()
          this.msg.showSuccessMessage('Checked out Successifully')
          
          this.printGatePassRcpt(data!.serviceBillItems, '', 0);
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

    var parking = {
      id : id
    }

    await this.http.post<String>(API_URL+'/parkings/deactivate', parking, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllClearedParkings()

          this.msg.showSuccessMessage('Parking deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showParkingData(data : IParking){
    this.id = data?.id;
    this.no = data!.no;
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

     this.comments = '' // check this

  }

  clearParkingData(){
    this.id = null;
    this.no = ''
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
  }

  printGatePassRcpt = async (billItems : IServiceBillItem[], receiptNo :string, cash : number) => {
  
      await this.get(this.parkingId)
      await this.getLastBillingDate(this.parkingId)
  
      var companyName = localStorage.getItem('company-name')!
  
      var header = ''
      var footer = ''
      var title  = 'Gate Pass(Reprinted)'
      var total : number = 0
      var discount : number = 0
      var tax : number = 0
  
      // var address : any = await this.data.getReceiptHeader(receiptNo)
      var address : any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(receiptNo)
     
      var receipt = [
        [
          {text : 'SN', fontSize : 8, bold : true}, 
          {text : 'Item', fontSize : 8, bold : true},
          {text : 'Qty', fontSize : 8, bold : true},
          {text : 'Amount', fontSize : 8, bold : true},
        ]
      ] 
      
      var sn = 0
  
      billItems.forEach((element) => {
        total = total + (+element.amount)
        sn = sn + 1
        var item = [
          {text : sn.toString(), fontSize : 8, bold : false}, 
          {text : element.item, fontSize : 8, bold : false},
          {text : element.qty.toString(), fontSize : 8, bold : false},
          {text : (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 8, alignment : 'right', bold : false},
        ]
        receipt.push(item)
      })
      var detailSummary = [
        {text : ' ', fontSize : 8, bold : false},
        {text : 'Total', fontSize : 9, bold : true},
        {text : ' ', fontSize : 8, bold : false},
        {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', bold : true},
      ]
      receipt.push(detailSummary)
      
  
      const docDefinition = {
        header: '',
        
        //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
          content : [
            {
              layout : 'noBorders',
              table : address
            }, 
            
            
            
            {
              layout : 'noBorders',
              table : {
                headerRows : 0,
                widths : [210],
                body : [
                  [{text : '=============================='}],
                ]
              }
            },          
            {
              layout : 'noBorders',
              table : {
                headerRows : 0,
                widths : [200],
                body : [
                  [{text : title, alignment : 'center', fontSize : 9, bold : true}],
                  [{text : 'Vehicle Name: ' + this.vehicleEquipmentTypeName, alignment : 'left', fontSize : 9, bold : false}],
                  [{text : 'Vehicle Color: ' + this.color, alignment : 'left', fontSize : 9, bold : false}],
                  [{text : 'Chassis No: ' + this.chasisNo, alignment : 'left', fontSize : 9, bold : false}],
                  [{text : '________________________________'}],
                  [{text : 'Payment Details', alignment : 'center', fontSize : 9, bold : true}],
                  [{text : ' ', alignment : 'center', fontSize : 9, bold : true}],
                ]
              }
            },   
            {
              layout : 'noBorders',
              table : {
                  headerRows : 1,
                  widths : [15, 100, 15, 50],
                  body : receipt
              }
            },
            {
              layout : 'noBorders',
              table : {
                headerRows : 0,
                widths : [200],
                body : [
                  [{text : ' '}],
                  [{text : 'Cashier Comments', alignment : 'left', fontSize : 9, bold : true}],
                  [{text : this.comments, alignment : 'left', fontSize : 9, bold : false}],
                  [{text : ' '}],
                  [{text : ' '}],
                  [{text : 'Issued At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                  [{text : 'Checkout At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                  [{text : 'Valid Until: ' + this.lastBillingDate, alignment : 'left', fontSize : 9, bold : true}],
                  [{text : ' '}],
                  [{text : 'Gate Pass issued By: ' + localStorage.getItem('user-name'), alignment : 'left', fontSize : 9, bold : true}],
                  [{text : ' '}],
                  [{text : 'Signature: ......................'}],
                ]
              }
            },   
            {
              layout : 'noBorders',
              table : {
                headerRows : 0,
                widths : [210],
                body : [
                  [{text : '=============================='}],
                  [{text : 'Developed By @Davaghana', fontSize : 10, bold : true, alignment : 'center'}],
                  [{text : '***End of Document***', fontSize : 9, alignment : 'center'}]
                ]
              }
            },
          ],
          pageMargins: 10,
        }
        const win = window.open('', "tempWinForPdf")
        pdfMake.createPdf(docDefinition).print({}, win)
        //win!.onfocus = function () { setTimeout(function () { win!.close(); }, 10000); } //set to 10 seconds
    }


  printGatePass1() {
    const documentDefinition = {
      content: [
        { text: 'Davagan', fontSize: 18, bold: true },
        { text: 'Gate Pass', fontSize: 18, bold: true },
        { text: 'Vehicle Name', fontSize: 18, bold: true },
        { text: 'Color', fontSize: 18, bold: true },
        { text: 'Chasis No', fontSize: 18, bold: true },
        { text: 'Reg No', fontSize: 18, bold: true },

        { text: 'Payments: OK', fontSize: 18, bold: true },
        { text: 'Cashier Coments: OK', fontSize: 18, bold: true },

        { text: 'Issue Date: ', fontSize: 18, bold: true },

        { text: 'Gate Pass issued By:', fontSize: 18, bold: true },

      ]
    };
    pdfMake.createPdf(documentDefinition).open();
  }

  printGatePassRcpt111 = async (billItems : IServiceBillItem[], receiptNo :string, cash : number) => {

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title  = 'Gate Pass'
    var total : number = 0
    var discount : number = 0
    var tax : number = 0

    var address : any = await this.data.getReceiptHeader(receiptNo)
   
    var receipt = [
      [
        {text : 'SN', fontSize : 8, bold : true}, 
        {text : 'Item', fontSize : 8, bold : true},
        {text : 'Qty', fontSize : 8, bold : true},
        {text : 'Amount', fontSize : 8, bold : true},
      ]
    ] 
    
    var sn = 0

    billItems.forEach((element) => {
      total = total + (+element.amount)
      sn = sn + 1
      var item = [
        {text : sn.toString(), fontSize : 8, bold : false}, 
        {text : element.item, fontSize : 8, bold : false},
        {text : element.qty.toString(), fontSize : 8, bold : false},
        {text : (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 8, alignment : 'right', bold : false},
      ]
      receipt.push(item)
    })
    var detailSummary = [
      {text : ' ', fontSize : 8, bold : false},
      {text : 'Total', fontSize : 9, bold : true},
      {text : ' ', fontSize : 8, bold : false},
      {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', bold : true},
    ]
    receipt.push(detailSummary)
    

    const docDefinition = {
      header: '',
      
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
        content : [
          {
            layout : 'noBorders',
            table : address
          }, 
          
          
          
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
              ]
            }
          },          
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                [{text : 'Gate Pass', alignment : 'center', fontSize : 9, bold : true}],
                [{text : 'Vehicle Name: ' + this.vehicleEquipmentTypeName, alignment : 'left', fontSize : 9, bold : false}],
                [{text : 'Vehicle Color: ' + this.color, alignment : 'left', fontSize : 9, bold : false}],
                [{text : 'Chassis No: ' + this.chasisNo, alignment : 'left', fontSize : 9, bold : false}],
                [{text : '________________________________'}],
                [{text : 'Payment Details', alignment : 'center', fontSize : 9, bold : true}],
                [{text : ' ', alignment : 'center', fontSize : 9, bold : true}],
              ]
            }
          },   
          {
            layout : 'noBorders',
            table : {
                headerRows : 1,
                widths : [15, 100, 15, 50],
                body : receipt
            }
          },
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                [{text : ' '}],
                [{text : 'Cashier Comments', alignment : 'left', fontSize : 9, bold : true}],
                [{text : this.comments, alignment : 'left', fontSize : 9, bold : false}],
                [{text : ' '}],
                [{text : ' '}],
                [{text : 'Issued At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                [{text : 'Checkout At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                [{text : 'Valid Until: ' + this.lastBillingDate, alignment : 'left', fontSize : 9, bold : true}],
                [{text : ' '}],
                [{text : 'Gate Pass issued By: ' + localStorage.getItem('user-name'), alignment : 'left', fontSize : 9, bold : true}],
                [{text : ' '}],
                [{text : 'Signature: ......................'}],
              ]
            }
          },   
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
                [{text : 'Developed By @Davaghana', fontSize : 10, bold : true, alignment : 'center'}],
                [{text : '***End of Document***', fontSize : 9, alignment : 'center'}]
              ]
            }
          },
        ],
        pageMargins: 10,
      }
      const win = window.open('', "tempWinForPdf")
      pdfMake.createPdf(docDefinition).print({}, win)
      //win!.onfocus = function () { setTimeout(function () { win!.close(); }, 10000); } //set to 10 seconds
  }

}


interface IModel{
  stringData : string
}
