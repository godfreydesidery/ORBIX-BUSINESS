import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';

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
export class VehicleEquipmentTypeComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  active : string = 'Inactive'

  dailyPrice : number = 0;

  /**Collections */
  vehicleEquipmentTypes : IVehicleEquipmentType[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllVehicleEquipmentTypes()
  }

  async getAllVehicleEquipmentTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.vehicleEquipmentTypes = []

    await this.http.get<IVehicleEquipmentType[]>(API_URL+'/vehicle_equipment_types', options)
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

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IVehicleEquipmentType>(API_URL+'/vehicle_equipment_tyeps/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showVehicleEquipmentTypeData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var vehicleEquipmentType = {
      id: this.id,
      code: this.code,
      name: this.name,
      dailyPrice : this.dailyPrice,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(vehicleEquipmentType.id === null){
      /**Create new vehicleEquipmentType */
      await this.http.post<IVehicleEquipmentType>(API_URL+'/vehicle_equipment_types/create', vehicleEquipmentType, options)
      .toPromise()
      .then(
        data => {
          this.showVehicleEquipmentTypeData(data!)

          console.log(data)

          this.getAllVehicleEquipmentTypes()

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
      /**Update an exiisting vehicleEquipmentType */
      await this.http.post<IVehicleEquipmentType>(API_URL+'/vehicle_equipment_types/update', vehicleEquipmentType, options)
      .toPromise()
      .then(
        data => {
          this.showVehicleEquipmentTypeData(data!)

          console.log(data)

          this.getAllVehicleEquipmentTypes()

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

    var vehicleEquipmentType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/vehicle_equipment_types/activate', vehicleEquipmentType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllVehicleEquipmentTypes()

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

    var vehicleEquipmentType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/vehicle_equipment_types/deactivate', vehicleEquipmentType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllVehicleEquipmentTypes()

          alert('VehicleEquipmentType deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
  }

  showVehicleEquipmentTypeData(data : IVehicleEquipmentType){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.dailyPrice = data!.dailyPrice
  }

  clearVehicleEquipmentTypeData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.dailyPrice = 0
  }
}
