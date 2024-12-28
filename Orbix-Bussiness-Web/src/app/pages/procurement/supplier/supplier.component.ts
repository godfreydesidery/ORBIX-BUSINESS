import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { ICompany } from 'src/app/domain/company';
import { ISupplier } from 'src/app/domain/supplier';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-supplier',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule
  ],
  templateUrl: './supplier.component.html',
  styleUrl: './supplier.component.scss'
})
export class SupplierComponent {
  /**Data */
  id : any = null
  code : string = ''
  name : string = ''
  contactName : string = ''
  address : string = ''
  phoneNo : string = ''

  active : string = 'Inactive'

  supplier : ISupplier

  /**Collections */

  suppliers : ISupplier[] = []

  /**Identifiers */
  supplierId : string = ''


  page: number = 1; // Initialize the current page to 1
  filterRecords : string = ''
  selectedOption: string = '';

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllSuppliers()
  }

  async getAllSuppliers(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.suppliers = []

    await this.http.get<ISupplier[]>(API_URL+'/suppliers', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.suppliers.push(element)
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
    await this.http.get<ISupplier>(API_URL+'/suppliers/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showSupplierData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var supplier = {
      id : this.id,
      code : this.code,
      name : this.name,
      contactName : this.contactName,
      address : this.address,
      phoneNo : this.phoneNo,
    }

    if(supplier.id === null){
      /**Create new supplier */
      await this.http.post<ISupplier>(API_URL+'/suppliers/create', supplier, options)
      .toPromise()
      .then(
        data => {
          this.showSupplierData(data!)

          console.log(data)

          this.getAllSuppliers()

          this.msg.showSuccessMessage('Supplier created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an existing supplier */
      await this.http.post<ISupplier>(API_URL+'/suppliers/update', supplier, options)
      .toPromise()
      .then(
        data => {
          this.showSupplierData(data!)

          console.log(data)

          this.getAllSuppliers()

          this.msg.showSuccessMessage('Supplier updated successifully')
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

    await this.http.post<String>(API_URL+'/suppliers/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllSuppliers()

          this.msg.showSuccessMessage('Supplier activated successifully')

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

    await this.http.post<String>(API_URL+'/suppliers/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllSuppliers()
          this.msg.showSuccessMessage('Supplier deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showSupplierData(data : ISupplier){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.contactName = data!.contactName
    this.address = data!.address
    this.phoneNo = data!.phoneNo
  }

  clearSupplierData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.contactName = ''
    this.address = ''
    this.phoneNo = ''
  }
}