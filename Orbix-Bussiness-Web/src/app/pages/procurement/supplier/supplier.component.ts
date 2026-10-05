import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { ISupplier } from 'src/app/domain/supplier';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-supplier',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './supplier.component.html',
  styleUrl: './supplier.component.scss'
})
export class SupplierComponent {
  trackById = trackById
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
  pageSize: number = 15
  totalSuppliers: number = 0
  // The list is loaded a page at a time and searched on the server
  suppliersRequest: number = 0 // number of the latest list request; answers to older ones are ignored
  searchTimer: any = null
  filterRecords : string = ''
  selectedOption: string = '';

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getSupplierPage(1)
  }

  async getSupplierPage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var request = ++this.suppliersRequest

    await this.http.get<IPage<ISupplier>>(API_URL + '/suppliers/get_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
      .toPromise()
      .then(
        data => {
          if (request != this.suppliersRequest) {
            return
          }
          // The page no longer exists (rows were removed): show the one before it
          if (data!.content.length == 0 && page > 1) {
            this.getSupplierPage(page - 1)
            return
          }
          var sn = (page - 1) * this.pageSize + 1
          data!.content.forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          this.suppliers = data!.content
          this.totalSuppliers = data!.totalElements
          this.page = page
        }
      )
  }

  pageChanged(page: number) {
    this.getSupplierPage(page)
  }

  searchSuppliers() {
    // Search on the server once the user pauses typing, starting again from the first page
    clearTimeout(this.searchTimer)
    this.searchTimer = setTimeout(() => {
      this.getSupplierPage(1)
    }, 300)
  }

  refreshSuppliers() {
    this.getSupplierPage(this.page)
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

          this.refreshSuppliers()

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

          this.refreshSuppliers()

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

          this.refreshSuppliers()

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

          this.refreshSuppliers()
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