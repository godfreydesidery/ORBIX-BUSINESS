import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { ICompany } from 'src/app/domain/company';
import { IDineable } from 'src/app/domain/dineable';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-dineable',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule
  ],
  templateUrl: './dineable.component.html',
  styleUrl: './dineable.component.scss'
})
export class DineableComponent {
/**Data */
  id: any = null
  code: string = ''
  name: string = ''
  description: string = ''
  baseUom: string = ''
  active: string = 'Inactive'

  dineable: IDineable

  /**Collections */

  dineables: IDineable[] = []

  /**Identifiers */
  dineableId: string = ''


  page: number = 1; // Initialize the current page to 1
  filterRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getAllDineables()
  }

  async getAllDineables() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.dineables = []

    await this.http.get<IDineable[]>(API_URL + '/dineables', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.dineables.push(element)
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
    await this.http.get<IDineable>(API_URL + '/dineables/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showDineableData(data!)
          console.log(data)
        }
      )
  }



  public async save() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var dineable = {
      id: this.id,
      code: this.code,
      name: this.name,
      description: this.description,
      baseUom: this.baseUom
    }

    if (dineable.id === null) {
      /**Create new dineable */
      await this.http.post<IDineable>(API_URL + '/dineables/create', dineable, options)
        .toPromise()
        .then(
          data => {
            this.showDineableData(data!)

            console.log(data)

            this.getAllDineables()

            this.msg.showSuccessMessage('Dineable created successifully')

          }

        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an existing dineable */
      await this.http.post<IDineable>(API_URL + '/dineables/update', dineable, options)
        .toPromise()
        .then(
          data => {
            this.showDineableData(data!)

            console.log(data)

            this.getAllDineables()

            this.msg.showSuccessMessage('Dineable updated successifully')
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

    await this.http.post<String>(API_URL + '/dineables/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllDineables()

          this.msg.showSuccessMessage('Dineable activated successifully')

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

    await this.http.post<String>(API_URL + '/dineables/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllDineables()
          this.msg.showSuccessMessage('Dineable deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showDineableData(data: IDineable) {
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.description = data!.description
    this.baseUom = data!.baseUom

  }

  clearDineableData() {
    this.id = null
    this.code = ''
    this.name = ''
    this.description = ''
    this.baseUom = ''

  }
}