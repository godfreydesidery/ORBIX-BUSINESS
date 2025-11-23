import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { ICompany } from 'src/app/domain/company';
import { IService } from 'src/app/domain/service';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-service',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule
  ],
  templateUrl: './service.component.html',
  styleUrl: './service.component.scss'
})
export class ServiceComponent {
/**Data */
  id: any = null
  code: string = ''
  name: string = ''
  description: string = ''
  baseUom: string = ''
  price: number = 0
  active: string = 'Inactive'

  service: IService

  /**Collections */

  services: IService[] = []

  /**Identifiers */
  serviceId: string = ''


  page: number = 1; // Initialize the current page to 1
  filterRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getAllServices()
  }

  async getAllServices() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.services = []

    await this.http.get<IService[]>(API_URL + '/services', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.services.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }



  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IService>(API_URL + '/services/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showServiceData(data!)
          console.log(data)
        }
      )
  }



  public async save() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var service = {
      id: this.id,
      code: this.code,
      name: this.name,
      description: this.description,
      baseUom: this.baseUom,
      price: this.price
    }

    if (service.id === null) {
      /**Create new service */
      await this.http.post<IService>(API_URL + '/services/create', service, options)
        .toPromise()
        .then(
          data => {
            this.showServiceData(data!)

            console.log(data)

            this.getAllServices()

            this.msg.showSuccessMessage('Service created successifully')

          }

        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an existing service */
      await this.http.post<IService>(API_URL + '/services/update', service, options)
        .toPromise()
        .then(
          data => {
            this.showServiceData(data!)

            console.log(data)

            this.getAllServices()

            this.msg.showSuccessMessage('Service updated successifully')
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

  async activate(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var company = {
      id: id
    }

    await this.http.post<String>(API_URL + '/services/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllServices()

          this.msg.showSuccessMessage('Service activated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async deactivate(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var company = {
      id: id
    }

    await this.http.post<String>(API_URL + '/services/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllServices()
          this.msg.showSuccessMessage('Service deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showServiceData(data: IService) {
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.description = data!.description
    this.baseUom = data!.baseUom
    this.price = data!.price

  }

  clearServiceData() {
    this.id = null
    this.code = ''
    this.name = ''
    this.description = ''
    this.baseUom = ''

  }
}