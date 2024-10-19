import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
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


  vehicleEquipmentTypeName : string = ''
  vehicleEquipmentName : string = ''
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

  // cardNo : string = ''

  vehicleEquipmentCategory : string = ''

  billingType : string = ''
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

  

  /**Collections */
  parkings : IParking[] = []

  // vehicleEquipmentTypes  : IVehicleEquipmentType[] = []

  parkingZones : IParkingZone[] = []














  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(): void {
    this.getAllActiveVehicleEquipments()
    this.getAllCompanyActiveVehicleEquipmentTypes()
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
      vehicleEquipmentName : this.vehicleEquipmentName,
      active : this.active,
      companyId : this.companyId,
      companyName : this.companyName,
      branchId : this.branchId,

      
  }

  if(this.id == null){

    await this.http.post<IVehicleEquipment>(API_URL+'/vehicle_equipments/create', vehicleEquipment, options)
    .toPromise()
    .then(
      data => {
        console.log(data)
        this.showVehicleEquipmentData(data!)
        // this.getAllActiveVehicleEquipments()

        alert('Saved Successfully')

        this.getParking(data!.parkingId)
        this.mode = ''


      }
    )
    .catch(
      error => {
        console.log(error)
        alert('An error has occured')
      }
    )

  }else{

    await this.http.post<IVehicleEquipment>(API_URL+'/vehicle_equipments/update', vehicleEquipment, options)
    .toPromise()
    .then(
      data => {
        console.log(data)
        this.showVehicleEquipmentData(data!)
        // this.getAllActiveVehicleEquipments()
        alert('Updated Successfully')

        this.getParking(data!.parkingId)

        this.mode = ''
      }
    )
    .catch(
      error => {
        console.log(error)
        alert('An error has occured')
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
    this.vehicleEquipmentTypeName = data.vehicleEquipmentTypeName
    this.active = data.active
    this.companyId = data.companyId
    this.companyName = data.companyName
    this.branchId = data.branchId

    this.parkingId = data!.parkingId
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
    this.vehicleEquipmentTypeName = ''
    this.active = ''

    this.companyId = ''
    this.companyName = ''
    this.branchId = ''
    this.branchName = ''
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
    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName,

    this.vehicleEquipmentCategory = data!.vehicleEquipmentCategory

    this.parkingZoneName = data!.parkingZoneName,
    this.cardNo = data!.cardNo

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

    this.billingType = ''
  }




  public async saveParking(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id: this.parkingId,
      no: this.parkingNo,
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
      vehicleEquipmentTypeName : this.vehicleEquipmentTypeName,

      vehicleEquipmentCategory : this.vehicleEquipmentCategory,

      cardNo : this.cardNo,

      billintType : this.billingType
    }

    console.log(parking)

    if(this.parkingId === null){
      /**Create new parking */
      await this.http.post<IParking>(API_URL+'/parkings/create', parking, options)
      .toPromise()
      .then(
        data => {
          this.showParkingData(data!)

          console.log(data)

          // this.getAllPendingOrCheckedInParkings()

          alert('Parking created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
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

          alert('Parking updated successifully')
        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
    }
  }





}
