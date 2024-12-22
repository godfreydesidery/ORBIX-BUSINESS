import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IUom } from 'src/app/domain/uom';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-uom',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './uom.component.html',
  styleUrl: './uom.component.scss'
})
export class UomComponent {
/**Data */
id : any = null
code : string = ''
name : string = ''
type : string = ''
active : string = 'Inactive'

uom : IUom

/**Collections */

uoms : IUom[] = []

/**Identifiers */
uomId : string = ''

constructor(
  private http :HttpClient,
  private auth : AuthService,
  private msg : MsgBoxService
) {}

ngOnInit(){
  this.getAllUoms()
}

async getAllUoms(){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }
  this.uoms = []

  await this.http.get<IUom[]>(API_URL+'/uoms', options)
  .toPromise()
  .then(
    data => {
      var sn = 1
      data?.forEach(element => {
        element.sn = sn
        this.uoms.push(element)
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
  await this.http.get<IUom>(API_URL+'/uoms/get?id=' + id, options)
  .toPromise()
  .then(
    data => {
      this.showUomData(data!)
      console.log(data)
    }
  )
}



public async save(){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }

  var uom = {
    id : this.id,
    code : this.code,
    name : this.name,
    type : this.type,
  }

  if(uom.id === null){
    /**Create new uom */
    await this.http.post<IUom>(API_URL+'/uoms/create', uom, options)
    .toPromise()
    .then(
      data => {
        this.showUomData(data!)

        console.log(data)

        this.getAllUoms()

        this.msg.showSuccessMessage('UOM created successifully')

      }

    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )
  }else{
    /**Update an existing uom */
    await this.http.post<IUom>(API_URL+'/uoms/update', uom, options)
    .toPromise()
    .then(
      data => {
        this.showUomData(data!)

        console.log(data)

        this.getAllUoms()

        this.msg.showSuccessMessage('UOM updated successifully')
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

  var company = {
    id : id
  }

  await this.http.post<String>(API_URL+'/uoms/activate', company, options)
    .toPromise()
    .then(
      data => {

        console.log(data)

        this.getAllUoms()

        this.msg.showSuccessMessage('Uom activated successifully')

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

  var company = {
    id : id
  }

  await this.http.post<String>(API_URL+'/uoms/deactivate', company, options)
    .toPromise()
    .then(
      data => {

        console.log(data)

        this.getAllUoms()
        this.msg.showSuccessMessage('Uom deactivated successifully')


      }

    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )
}

showUomData(data : IUom){
  this.id = data?.id
  this.code = data!.code
  this.name = data!.name
  this.type = data!.type
  this.active = data!.active
  
}

clearUomData(){
  this.id = null
  this.code = ''
  this.name = ''
  this.type = ''
  this.active = 'Inactive'

}
}