import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IBondItemType } from 'src/app/domain/bond-item-type';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-bond-item-type',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './bond-item-type.component.html',
  styleUrl: './bond-item-type.component.scss'
})
export class BondItemTypeComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  active : string = 'Inactive'

  dailyPrice : number = 0;

  /**Collections */
  bondItemTypes : IBondItemType[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllBondItemTypes()
  }

  async getAllBondItemTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.bondItemTypes = []

    await this.http.get<IBondItemType[]>(API_URL+'/bond_item_types', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.bondItemTypes.push(element)
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
    await this.http.get<IBondItemType>(API_URL+'/bond_item_types/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showBondItemTypeData(data!)
        console.log(data)
      }
    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var bondItemType = {
      id: this.id,
      code: this.code,
      name: this.name,
      dailyPrice : this.dailyPrice,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(bondItemType.id === null){
      /**Create new bondItemType */
      await this.http.post<IBondItemType>(API_URL+'/bond_item_types/create', bondItemType, options)
      .toPromise()
      .then(
        data => {
          this.showBondItemTypeData(data!)

          console.log(data)

          this.getAllBondItemTypes()

          this.msg.showSuccessMessage('Type created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an exiisting bondItemType */
      await this.http.post<IBondItemType>(API_URL+'/bond_item_types/update', bondItemType, options)
      .toPromise()
      .then(
        data => {
          this.showBondItemTypeData(data!)

          console.log(data)

          this.getAllBondItemTypes()

          this.msg.showSuccessMessage('Type updated successifully')
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

    var bondItemType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/bond_item_types/activate', bondItemType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBondItemTypes()
          this.msg.showSuccessMessage('Type activated successifully')


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

    var bondItemType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/bond_item_types/deactivate', bondItemType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBondItemTypes()
          this.msg.showSuccessMessage('Type deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showBondItemTypeData(data : IBondItemType){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.dailyPrice = data!.dailyPrice
  }

  clearBondItemTypeData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.dailyPrice = 0
  }
}