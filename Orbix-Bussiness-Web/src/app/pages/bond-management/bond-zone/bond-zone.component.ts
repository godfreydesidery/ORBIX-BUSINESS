import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IBondZone } from 'src/app/domain/bond-zone';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-bond-zone',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './bond-zone.component.html',
  styleUrl: './bond-zone.component.scss'
})
export class BondZoneComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  noOfSections : number = 0;
  active : string = 'Inactive'

  /**Collections */
  bondZones : IBondZone[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllBondZones()
  }

  async getAllBondZones(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.bondZones = []

    await this.http.get<IBondZone[]>(API_URL+'/bond_zones', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.bondZones.push(element)
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
    await this.http.get<IBondZone>(API_URL+'/bond_zones/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showBondZoneData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var bondZone = {
      id: this.id,
      code: this.code,
      name: this.name,
      noOfSections : this.noOfSections,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(bondZone.id === null){
      /**Create new bondZone */
      await this.http.post<IBondZone>(API_URL+'/bond_zones/create', bondZone, options)
      .toPromise()
      .then(
        data => {
          this.showBondZoneData(data!)

          console.log(data)

          this.getAllBondZones()

          this.msg.showSuccessMessage('BondZone created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an exiisting bondZone */
      await this.http.post<IBondZone>(API_URL+'/bond_zones/update', bondZone, options)
      .toPromise()
      .then(
        data => {
          this.showBondZoneData(data!)

          console.log(data)

          this.getAllBondZones()

          this.msg.showSuccessMessage('BondZone updated successifully')
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

    var bondZone = {
      id : id
    }

    await this.http.post<String>(API_URL+'/bond_zones/activate', bondZone, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBondZones()

          this.msg.showSuccessMessage('BondZone activated successifully')


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

    var bondZone = {
      id : id
    }

    await this.http.post<String>(API_URL+'/bond_zones/deactivate', bondZone, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBondZones()

          this.msg.showSuccessMessage('BondZone deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showBondZoneData(data : IBondZone){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.noOfSections = data!.noOfSections
  }

  clearBondZoneData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.noOfSections = 0
  }
}
