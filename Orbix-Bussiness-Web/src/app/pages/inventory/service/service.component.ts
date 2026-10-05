import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IService } from 'src/app/domain/service';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-service',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './service.component.html',
  styleUrl: './service.component.scss'
})
export class ServiceComponent {
  trackById = trackById
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
  pageSize: number = 15
  totalServices: number = 0
  // The list is loaded a page at a time and searched on the server
  servicesRequest: number = 0 // number of the latest list request; answers to older ones are ignored
  searchTimer: any = null
  filterRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getServicePage(1)
  }

  async getServicePage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var request = ++this.servicesRequest

    await this.http.get<IPage<IService>>(API_URL + '/services/get_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
      .toPromise()
      .then(
        data => {
          if (request != this.servicesRequest) {
            return
          }
          // The page no longer exists (rows were removed): show the one before it
          if (data!.content.length == 0 && page > 1) {
            this.getServicePage(page - 1)
            return
          }
          var sn = (page - 1) * this.pageSize + 1
          data!.content.forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          this.services = data!.content
          this.totalServices = data!.totalElements
          this.page = page
        }
      )
  }

  pageChanged(page: number) {
    this.getServicePage(page)
  }

  searchServices() {
    // Search on the server once the user pauses typing, starting again from the first page
    clearTimeout(this.searchTimer)
    this.searchTimer = setTimeout(() => {
      this.getServicePage(1)
    }, 300)
  }

  refreshServices() {
    this.getServicePage(this.page)
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

            this.refreshServices()

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

            this.refreshServices()

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

          this.refreshServices()

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

          this.refreshServices()
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