import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { IVehicleAndEquipmentType } from 'src/app/domain/vehicle-and-equipment-type';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-vehicle-and-equipment-type',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './vehicle-and-equipment-type.component.html',
  styleUrl: './vehicle-and-equipment-type.component.scss'
})
export class VehicleAndEquipmentTypeComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  active : string = 'Inactive'

  dailyPrice : number = 0;

  /**Collections */
  vehicleAndEquipmentTypes : IVehicleAndEquipmentType[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllVehicleAndEquipmentTypes()
  }

  async getAllVehicleAndEquipmentTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.vehicleAndEquipmentTypes = []

    await this.http.get<IVehicleAndEquipmentType[]>(API_URL+'/vehicle_and_equipment_types', options)
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

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IVehicleAndEquipmentType>(API_URL+'/vehicle_and_equipment_types/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showVehicleAndEquipmentTypeData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var vehicleAndEquipmentType = {
      id: this.id,
      code: this.code,
      name: this.name,
      dailyPrice : this.dailyPrice,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(vehicleAndEquipmentType.id === null){
      /**Create new vehicleAndEquipmentType */
      await this.http.post<IVehicleAndEquipmentType>(API_URL+'/vehicle_and_equipment_types/create', vehicleAndEquipmentType, options)
      .toPromise()
      .then(
        data => {
          this.showVehicleAndEquipmentTypeData(data!)

          console.log(data)

          this.getAllVehicleAndEquipmentTypes()

          alert('Type created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
    }else{
      /**Update an exiisting vehicleAndEquipmentType */
      await this.http.post<IVehicleAndEquipmentType>(API_URL+'/vehicle_and_equipment_types/update', vehicleAndEquipmentType, options)
      .toPromise()
      .then(
        data => {
          this.showVehicleAndEquipmentTypeData(data!)

          console.log(data)

          this.getAllVehicleAndEquipmentTypes()

          alert('Type updated successifully')
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

    var vehicleAndEquipmentType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/vehicle_and_equipment_types/activate', vehicleAndEquipmentType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllVehicleAndEquipmentTypes()

          alert('Type activated successifully')

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

    var vehicleAndEquipmentType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/vehicle_and_equipment_types/deactivate', vehicleAndEquipmentType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllVehicleAndEquipmentTypes()

          alert('VehicleAndEquipmentType deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
  }

  showVehicleAndEquipmentTypeData(data : IVehicleAndEquipmentType){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.dailyPrice = data!.dailyPrice
  }

  clearVehicleAndEquipmentTypeData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.dailyPrice = 0
  }
}
