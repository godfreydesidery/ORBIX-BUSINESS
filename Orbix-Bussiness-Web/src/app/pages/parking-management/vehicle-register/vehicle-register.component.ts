import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { IParking } from 'src/app/domain/parking';
import { IParkingZone } from 'src/app/domain/parking-zone';
import { IVehicleAndEquipmentType } from 'src/app/domain/vehicle-and-equipment-type';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-vehicle-register',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './vehicle-register.component.html',
  styleUrl: './vehicle-register.component.scss'
})
export class VehicleRegisterComponent {

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

  vehicleAndEquipmentCategory : string = ''

  billingType : string = ''
  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  // Foreign keys
  parkingId: any = ''
  vehicleAndEquipmentTypeId: any = ''
  vehicleAndEquipmentTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  parkingZoneName : string = ''

  

  /**Collections */
  parkings : IParking[] = []

  vehicleAndEquipmentTypes  : IVehicleAndEquipmentType[] = []

  parkingZones : IParkingZone[] = []

  
  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllParkings()   
    this.getAllCompanyActiveVehicleAndEquipmentTypes()
    this.getAllBranchActiveParkingZones();
  }

  async getAllParkings(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkings = []

    await this.http.get<IParking[]>(API_URL+'/parkings', options)
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

  

  async get(id : any){

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

  async getAllCompanyActiveVehicleAndEquipmentTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.vehicleAndEquipmentTypes = []

    await this.http.get<IVehicleAndEquipmentType[]>(API_URL+'/vehicle_and_equipment_types/get_all_company_active', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.vehicleAndEquipmentTypes.push(element)
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
      leftFrontLamp: this.leftFrontLamp,
      rightFrontLamp: this.rightFrontLamp,
      leftRearLamp: this.leftRearLamp,
      rightRearLamp: this.rightRearLamp,
      leftSideMirror: this.leftSideMirror,
      rightSideMirror: this.rightSideMirror,
      leftWiper: this.leftWiper,
      rightWiper: this.rightWiper,
      backWiper: this.backWiper,
      fuelCap: this.fuelCap,
      spareTire: this.spareTire,
      battery: this.battery,
      starter: this.starter,
      aerial: this.aerial,
      wheelCap: this.wheelCap,
      roundMirror: this.roundMirror,
      tireIndicator: this.tireIndicator,
      vehicleAndEquipmentTypeName : this.vehicleAndEquipmentTypeName,

      vehicleAndEquipmentCategory : this.vehicleAndEquipmentCategory,
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

          this.getAllParkings()

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

          this.getAllParkings()

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

          this.getAllParkings()

          alert('Parking activated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
  }

  async checkIn(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parking = {
      id : this.id,
      parkingZoneName : this.parkingZoneName
    }

    await this.http.post<IParking>(API_URL+'/parkings/check_in', parking, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllParkings()

          alert('Checked in Successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
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

          this.getAllParkings()

          alert('Parking deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
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
    this.leftFrontLamp = data?.leftFrontLamp;
    this.rightFrontLamp = data?.rightFrontLamp;
    this.leftRearLamp = data?.leftRearLamp;
    this.rightRearLamp = data?.rightRearLamp;
    this.leftSideMirror = data?.leftSideMirror;
    this.rightSideMirror = data?.rightSideMirror;
    this.leftWiper = data?.leftWiper;
    this.rightWiper = data?.rightWiper;
    this.backWiper = data?.backWiper;
    this.fuelCap = data?.fuelCap;
    this.spareTire = data?.spareTire;
    this.battery = data?.battery;
    this.starter = data?.starter;
    this.aerial = data?.aerial;
    this.wheelCap = data?.wheelCap;
    this.roundMirror = data?.roundMirror;
    this.tireIndicator = data?.tireIndicator;
    this.vehicleAndEquipmentTypeName = data!.vehicleAndEquipmentTypeName,

    this.vehicleAndEquipmentCategory = data!.vehicleAndEquipmentCategory

    this.parkingZoneName = data!.parkingZoneName

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
    this.vehicleAndEquipmentTypeName = ''

    this.vehicleAndEquipmentCategory = ''

    this.parkingZoneName = ''
  }
}
