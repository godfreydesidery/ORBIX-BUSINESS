import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IGoodType } from 'src/app/domain/good-type';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-good-type',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './good-type.component.html',
  styleUrl: './good-type.component.scss'
})
export class GoodTypeComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  active : string = 'Inactive'

  dailyPrice : number = 0;

  /**Collections */
  goodTypes : IGoodType[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllGoodTypes()
  }

  async getAllGoodTypes(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.goodTypes = []

    await this.http.get<IGoodType[]>(API_URL+'/good_types', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.goodTypes.push(element)
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
    await this.http.get<IGoodType>(API_URL+'/good_types/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showGoodTypeData(data!)
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

    var goodType = {
      id: this.id,
      code: this.code,
      name: this.name,
      dailyPrice : this.dailyPrice,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(goodType.id === null){
      /**Create new goodType */
      await this.http.post<IGoodType>(API_URL+'/good_types/create', goodType, options)
      .toPromise()
      .then(
        data => {
          this.showGoodTypeData(data!)

          console.log(data)

          this.getAllGoodTypes()

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
      /**Update an exiisting goodType */
      await this.http.post<IGoodType>(API_URL+'/good_types/update', goodType, options)
      .toPromise()
      .then(
        data => {
          this.showGoodTypeData(data!)

          console.log(data)

          this.getAllGoodTypes()

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

    var goodType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/good_types/activate', goodType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllGoodTypes()
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

    var goodType = {
      id : id
    }

    await this.http.post<String>(API_URL+'/good_types/deactivate', goodType, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllGoodTypes()
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

  showGoodTypeData(data : IGoodType){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.dailyPrice = data!.dailyPrice
  }

  clearGoodTypeData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.dailyPrice = 0
  }
}