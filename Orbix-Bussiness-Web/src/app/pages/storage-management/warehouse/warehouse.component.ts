import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IWarehouse } from 'src/app/domain/warehouse';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-warehouse',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './warehouse.component.html',
  styleUrl: './warehouse.component.scss'
})
export class WarehouseComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = '' 
  noOfSections : number = 0;
  active : string = 'Inactive'

  /**Collections */
  warehouses : IWarehouse[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllWarehouses()
  }

  async getAllWarehouses(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.warehouses = []

    await this.http.get<IWarehouse[]>(API_URL+'/warehouses', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.warehouses.push(element)
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
    await this.http.get<IWarehouse>(API_URL+'/warehouses/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showWarehouseData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var warehouse = {
      id: this.id,
      code: this.code,
      name: this.name,
      noOfSections : this.noOfSections,

      active: this.active,

      company : {id : "" },

      branch : {id : ""},

      sn : 0
    }

    if(warehouse.id === null){
      /**Create new warehouse */
      await this.http.post<IWarehouse>(API_URL+'/warehouses/create', warehouse, options)
      .toPromise()
      .then(
        data => {
          this.showWarehouseData(data!)

          console.log(data)

          this.getAllWarehouses()

          this.msg.showSuccessMessage('Warehouse created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an exiisting warehouse */
      await this.http.post<IWarehouse>(API_URL+'/warehouses/update', warehouse, options)
      .toPromise()
      .then(
        data => {
          this.showWarehouseData(data!)

          console.log(data)

          this.getAllWarehouses()

          this.msg.showSuccessMessage('Warehouse updated successifully')
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

    var warehouse = {
      id : id
    }

    await this.http.post<String>(API_URL+'/warehouses/activate', warehouse, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllWarehouses()

          this.msg.showSuccessMessage('Warehouse activated successifully')


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

    var warehouse = {
      id : id
    }

    await this.http.post<String>(API_URL+'/warehouses/deactivate', warehouse, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllWarehouses()

          this.msg.showSuccessMessage('Warehouse deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showWarehouseData(data : IWarehouse){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.noOfSections = data!.noOfSections
  }

  clearWarehouseData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.noOfSections = 0
  }
}
