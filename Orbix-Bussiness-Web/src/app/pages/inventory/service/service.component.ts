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
import { IPage } from 'src/app/domain/page';
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
    SearchFilterPipe,
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
  // The list is loaded a page at a time; the whole list is loaded only when searching, so the search still covers every service
  allServicesLoaded: boolean = false
  requestedPage: number = 1
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

  async getAllServices() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.services = []

    await this.http.get<IService[]>(API_URL + '/services', options)
      .toPromise()
      .then(
        data => {
          // Built apart and assigned at the end, so two overlapping loads cannot mix their rows
          var services : IService[] = []
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            services.push(element)
            sn = sn + 1
          })
          this.services = services
          this.allServicesLoaded = true
          console.log(data)
        }
      )
  }

  async getServicePage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.requestedPage = page

    await this.http.get<IPage<IService>>(API_URL + '/services/get_page?page=' + (page - 1) + '&size=' + this.pageSize, options)
      .toPromise()
      .then(
        data => {
          // Ignore a page that arrives after another page or the whole list was requested
          if (this.allServicesLoaded || page != this.requestedPage) {
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
    if (this.allServicesLoaded) {
      this.page = page
    } else {
      this.getServicePage(page)
    }
  }

  searchServices(filter: string) {
    if (filter != '' && !this.allServicesLoaded) {
      // Set before loading, so a page that arrives meanwhile is ignored
      this.allServicesLoaded = true
      this.getAllServices()
    }
  }

  refreshServices() {
    if (this.allServicesLoaded) {
      this.getAllServices()
    } else {
      this.getServicePage(this.page)
    }
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