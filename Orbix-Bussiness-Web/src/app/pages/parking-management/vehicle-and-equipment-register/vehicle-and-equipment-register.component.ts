import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IParking } from 'src/app/domain/parking';
import { IParkingZone } from 'src/app/domain/parking-zone';
import { IVehicleEquipment } from 'src/app/domain/vehicle-equipment';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-vehicle-and-equipment-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './vehicle-and-equipment-register.component.html',
  styleUrl: './vehicle-and-equipment-register.component.scss'
})
export class VehicleEquipmentRegisterComponent {

  mode : string = ''
  
  id : any = null
  no : string = ''

  ownerFirstName: string = ''
  ownerMiddleName: string = ''
  ownerLastName: string = ''
  ownerCompanyName: string = ''
  ownerIdNo: string = ''
  ownerIdType: string = ''
  ownerPhoneNo: string = ''
  ownerEmail: string = ''
  ownerAddress: string = ''
  registrationNo: string = ''
  chasisNo: string = ''
  cardNo: string = ''
  image : any = null

  comments : string = ''


  vehicleEquipmentTypeName : string = ''
  vehicleEquipmentName : string = ''
  vehicleEquipmentColor : string = ''
  active : string = 'Inactive'

  companyId : string = ''
  companyName : string = ''
  branchId : string = ''
  branchName : string = ''

  /**Colections */
  vehicleEquipments : IVehicleEquipment[] = []
  vehicleEquipmentTypes : IVehicleEquipmentType[] = []

  parkingId : any = null
  parkingNo : string = ''

  // Owner information
  // ownerFirstName: string = ''
  // ownerMiddleName: string = ''
  // ownerLastName: string = ''
  // ownerCompanyName: string = ''
  // ownerIdNo: string = ''
  // ownerIdType: string = ''
  // ownerPhoneNo: string = ''
  // ownerEmail: string = ''
  // ownerAddress: string = ''

  // Agent Information
  agentName: string = ''
  agentAddress: string = ''
  agentPhoneNo: string = ''
  agentEmail: string = ''
  tformNumber: string = ''

  // Vehicle or Equipment Information
  // registrationNo: string = ''
  // chasisNo: string = ''
  leftFrontLamp: string = 'YES'
  rightFrontLamp: string = 'YES'
  leftRearLamp: string = 'YES'
  rightRearLamp: string = 'YES'
  leftSideMirror: string = 'YES'
  rightSideMirror: string = 'YES'
  leftWiper: string = 'YES'
  rightWiper: string = 'YES'
  backWiper: string = 'YES'
  fuelCap: string = 'YES'
  spareTire: string = 'YES'
  battery: string = 'YES'
  starter: string = 'YES'
  aerial: string = 'YES'
  wheelCap: string = 'YES'
  roundMirror: string = 'YES'
  tireIndicator: string = 'YES'
  deviceStatus: string = 'ATTACHED'

  // cardNo : string = ''

  vehicleEquipmentCategory : string = 'IN-TRANSIT'

  billingType : string = 'DAILY'
  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  // Foreign keys
  // parkingId: any = ''
  vehicleEquipmentTypeId: any = ''
  // vehicleEquipmentTypeName : string = ''
  // branchId: any = ''
  // companyId: any = ''

  parkingZoneName : string = ''

  hasKeys : string = 'YES'

  

  /**Collections */
  parkings : IParking[] = []

  // vehicleEquipmentTypes  : IVehicleEquipmentType[] = []

  parkingZones : IParkingZone[] = []












  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private msg : MsgBoxService
  ) {}

  ngOnInit(): void {
    this.getAllActiveVehicleEquipments()
    this.getAllCompanyActiveVehicleEquipmentTypes()
    this.getAllBranchActiveParkingZones()
    this.getAllVehicleEquipmentChasisNos()
  }



  async getAllActiveVehicleEquipments() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.vehicleEquipments = []

    await this.http.get<IVehicleEquipment[]>(API_URL+'/vehicle_equipments/get_all_active', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.vehicleEquipments.push(element)
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
    await this.http.get<IVehicleEquipment>(API_URL+'/vehicle_equipments/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showVehicleEquipmentData(data!)
        console.log(data)
      }
    )
  }


  showParking : boolean = false

  async searchVehicleEquipmentByChasisNo(chasisNo : string){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IVehicleEquipment>(API_URL+'/vehicle_equipments/get_by_chasis_no?chasis_no=' + chasisNo, options)
    .toPromise()
    .then(
      data => {
        this.showVehicleEquipmentData(data!)
        console.log(data)
        if(data!.parkingId != null){
          this.showParking = true
          this.getParking(data!.parkingId)
        }else{
          this.showParking = false
        }
      }
    )
  }


  chasisNos : String[] = []
  async getAllVehicleEquipmentChasisNos(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.chasisNos = []
    await this.http.get<string[]>(API_URL+'/vehicle_equipments/get_chasis_nos', options)
    .toPromise()
    .then(
      data => {
        this.chasisNos = data!
        console.log(data)        
      }
    )
  }


  sendToParking(id : any){
    if(this.parkingId === null){
      // send to parking
      this.saveParking()
    }else{
      // already sent to parking
    }
  }


  public async save() {
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var vehicleEquipment = {
      id : this.id,
      no : this.no,
      ownerFirstName : this.ownerFirstName,
      ownerMiddleName : this.ownerMiddleName,
      ownerLastName : this.ownerLastName,
      ownerCompanyName : this.ownerCompanyName,
      ownerIdNo : this.ownerIdNo,
      ownerIdType : this.ownerIdType,
      ownerPhoneNo : this.ownerPhoneNo,
      ownerEmail : this.ownerEmail,
      ownerAddress : this.ownerAddress,
      registrationNo : this.registrationNo,
      chasisNo : this.chasisNo,
      cardNo : this.cardNo,
      vehicleEquipmentTypeName : this.vehicleEquipmentTypeName,
      vehicleEquipmentColor : this.vehicleEquipmentColor,
      vehicleEquipmentName : this.vehicleEquipmentName,
      active : this.active,
      companyId : this.companyId,
      companyName : this.companyName,
      branchId : this.branchId,
      comments : this.comments,
      hasKeys : this.hasKeys === 'YES' ? 1 : 0,
      tformNumber : this.tformNumber,

      agentName : this.agentName,
      agentPhoneNo : this.agentPhoneNo,
      agentEmail : this.agentEmail,
      agentAddress : this.agentAddress,
      deviceStatus : this.deviceStatus === 'YES' ? 1 : 0


      
  }


  if(this.ownerIdType != 'NONE' && this.ownerIdType === ''){
    this.msg.showErrorMessage3('ID No is required')
  }
  if(this.ownerPhoneNo === ''){
    this.msg.showErrorMessage3('Phone No is required')
  }

  if(this.id == null){

    await this.http.post<IVehicleEquipment>(API_URL+'/vehicle_equipments/create', vehicleEquipment, options)
    .toPromise()
    .then(
      data => {
        console.log(data)
        this.showVehicleEquipmentData(data!)
        // this.getAllActiveVehicleEquipments()

        this.msg.showSuccessMessage('Saved Successfully')

        this.getParking(data!.parkingId)
        this.mode = ''


      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )

  }else{

    await this.http.post<IVehicleEquipment>(API_URL+'/vehicle_equipments/update', vehicleEquipment, options)
    .toPromise()
    .then(
      data => {
        console.log(data)
        this.showVehicleEquipmentData(data!)
        this.msg.showSuccessMessage('Updated Successfully')

        this.getParking(data!.parkingId)

        this.mode = ''
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

async getAllCompanyActiveVehicleEquipmentTypes(){
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




  showVehicleEquipmentData(data : IVehicleEquipment){
    this.id = data.id
    this.no = data.no
    this.ownerFirstName = data.ownerFirstName
    this.ownerMiddleName = data.ownerMiddleName
    this.ownerLastName = data.ownerLastName
    this.ownerCompanyName = data.ownerCompanyName
    this.ownerIdNo = data.ownerIdNo
    this.ownerIdType = data.ownerIdType
    this.ownerPhoneNo = data.ownerPhoneNo
    this.ownerEmail = data.ownerEmail
    this.ownerAddress = data.ownerAddress
    this.registrationNo = data.registrationNo
    this.chasisNo = data.chasisNo
    this.cardNo = data.cardNo
    this.vehicleEquipmentName = data.vehicleEquipmentName
    this.vehicleEquipmentColor = data.vehicleEquipmentColor
    this.vehicleEquipmentTypeName = data.vehicleEquipmentTypeName

    this.tformNumber = data.tformNumber

    this.agentName = data.agentName
    this.agentPhoneNo = data.agentPhoneNo
    this.agentEmail = data.agentEmail
    this.agentAddress = data.agentAddress

    this.deviceStatus = data.deviceStatus
    this.active = data.active
    this.companyId = data.companyId
    this.companyName = data.companyName
    this.branchId = data.branchId

    this.comments = data.comments

    this.parkingId = data!.parkingId

    

    console.log(data)
  }


  clear(){
    this.id = null
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
    this.registrationNo = ''
    this.chasisNo = ''
    this.cardNo = ''
    this.vehicleEquipmentName = ''
    this.vehicleEquipmentColor = ''
    this.vehicleEquipmentTypeName = ''
    this.active = ''

    this.tformNumber = ''

    this.agentName = ''
    this.agentPhoneNo = ''
    this.agentEmail = ''
    this.agentAddress = ''

    this.companyId = ''
    this.companyName = ''
    this.branchId = ''
    this.branchName = ''
    this.hasKeys = 'YES'

    this.comments = ''
    this.deviceStatus = 'ATTACHED'
  }

  setNewMode(){
    this.clearParkingData()
    this.clear()
    this.mode = 'new'

    
  }

  setExistingMode(){
    this.clearParkingData()
    this.clear()
    this.mode = 'existing'    
  }




  async getParking(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParking>(API_URL+'/parkings/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showParkingData(data!)
        console.log(data)
      }
    )
  }


  showParkingData(data : IParking){
    this.parkingId = data?.id;
    this.parkingNo = data!.no;
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
    this.deviceStatus = data?.deviceStatus == true ? 'ATTACHED' : 'NOT-ATTACHED'
    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName,
    this.vehicleEquipmentColor = data!.vehicleEquipmentColor,
    this.hasKeys = data!.hasKeys == true ? 'YES' : 'NO'

    this.vehicleEquipmentName = data!.vehicleEquipmentName,

    this.vehicleEquipmentCategory = data!.vehicleEquipmentCategory

    this.parkingZoneName = data!.parkingZoneName,
    this.cardNo = data!.cardNo

    this.comments = data!.comments

    this.billingType = data!.billingType


  }

  clearParkingData(){
    this.showParking = false
    this.parkingId = null;
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

    this.billingType = 'DAILY'
    this.billingAmount = 0

    // Vehicle or Equipment Information
    this.registrationNo = ''
    this.chasisNo = ''
    this.leftFrontLamp = 'YES'
    this.rightFrontLamp = 'YES'
    this.leftRearLamp = 'YES'
    this.rightRearLamp = 'YES'
    this.leftSideMirror = 'YES'
    this.rightSideMirror = 'YES'
    this.leftWiper = 'YES'
    this.rightWiper = 'YES'
    this.backWiper = 'YES'
    this.fuelCap = 'YES'
    this.spareTire = 'YES'
    this.battery = 'YES'
    this.starter = 'YES'
    this.aerial = 'YES'
    this.wheelCap = 'YES'
    this.roundMirror = 'YES'
    this.tireIndicator = 'YES'
    this.deviceStatus = 'ATTACHED'
    this.vehicleEquipmentTypeName = ''
    this.hasKeys = 'YES'

    this.comments = ''



    this.vehicleEquipmentCategory = 'IN-TRANSIT'

    this.vehicleEquipmentName = ''
    this.vehicleEquipmentColor = ''

    this.parkingZoneName = ''

    this.billingType = 'DAILY'
  }




  public async saveParking(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id: this.parkingId,
      no: this.parkingNo,
      vehicleEquipmentId : this.id,
      ownerFirstName: this.ownerFirstName,
      ownerMiddleName: this.ownerMiddleName,
      ownerLastName: this.ownerLastName,
      ownerCompanyName: this.ownerCompanyName,
      ownerIdNo: this.ownerIdNo,
      ownerIdType: this.ownerIdType,
      ownerPhoneNo: this.ownerPhoneNo,
      ownerEmail: this.ownerEmail,
      ownerAddress: this.ownerAddress,

      parkingZoneName : this.parkingZoneName,

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
      deviceStatus: this.deviceStatus === 'ATTACHED' ? 1 : 0,
      vehicleEquipmentTypeName : this.vehicleEquipmentTypeName,
      hasKeys : this.hasKeys === 'YES' ? 1 : 0,

      comments : this.comments,

      vehicleEquipmentCategory : this.vehicleEquipmentCategory,

      vehicleEquipmentName : this.vehicleEquipmentName,

      vehicleEquipmentColor : this.vehicleEquipmentColor,

      cardNo : this.cardNo,

      billintType : this.billingType
    }

    console.log(parking)

    if(this.parkingId === null || this.parkingId === undefined || this.parkingId === ''){
      /**Create new parking */
      await this.http.post<IParking>(API_URL+'/parkings/create', parking, options)
      .toPromise()
      .then(
        data => {
          this.showParkingData(data!)

          console.log(data)

          // this.getAllPendingOrCheckedInParkings()

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

          // this.getAllPendingOrCheckedInParkings()

          this.msg.showSuccessMessage('Parking updated successifully, Vehicle available for check in')

          this.setNewMode()
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
}
function then(arg0: (data: any) => void): PromiseConstructor {
  throw new Error('Function not implemented.');
}

