import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IParkingZone } from 'src/app/domain/parking-zone';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-parking-zone',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './parking-zone.component.html',
  styleUrl: './parking-zone.component.scss'
})
export class ParkingZoneComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  noOfSlots : number = 0;
  active : string = 'Inactive'

  /**Collections */
  parkingZones : IParkingZone[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllParkingZones()
  }

  async getAllParkingZones(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingZones = []

    await this.http.get<IParkingZone[]>(API_URL+'/parking_zones', options)
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

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParkingZone>(API_URL+'/parking_zones/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showParkingZoneData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parkingZone = {
      id: this.id,
      code: this.code,
      name: this.name,
      noOfSlots : this.noOfSlots,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(parkingZone.id === null){
      /**Create new parkingZone */
      await this.http.post<IParkingZone>(API_URL+'/parking_zones/create', parkingZone, options)
      .toPromise()
      .then(
        data => {
          this.showParkingZoneData(data!)

          console.log(data)

          this.getAllParkingZones()

          this.msg.showSuccessMessage('ParkingZone created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an exiisting parkingZone */
      await this.http.post<IParkingZone>(API_URL+'/parking_zones/update', parkingZone, options)
      .toPromise()
      .then(
        data => {
          this.showParkingZoneData(data!)

          console.log(data)

          this.getAllParkingZones()

          this.msg.showSuccessMessage('ParkingZone updated successifully')
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

    var parkingZone = {
      id : id
    }

    await this.http.post<String>(API_URL+'/parking_zones/activate', parkingZone, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllParkingZones()

          this.msg.showSuccessMessage('ParkingZone activated successifully')


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

    var parkingZone = {
      id : id
    }

    await this.http.post<String>(API_URL+'/parking_zones/deactivate', parkingZone, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllParkingZones()

          this.msg.showSuccessMessage('ParkingZone deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showParkingZoneData(data : IParkingZone){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.noOfSlots = data!.noOfSlots
  }

  clearParkingZoneData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.noOfSlots = 0
  }
}
