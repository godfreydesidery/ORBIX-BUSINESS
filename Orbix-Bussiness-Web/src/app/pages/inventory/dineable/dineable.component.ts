import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IDineable } from 'src/app/domain/dineable';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-dineable',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './dineable.component.html',
  styleUrl: './dineable.component.scss'
})
export class DineableComponent {
  trackById = trackById
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
  pageSize: number = 15
  totalDineables: number = 0
  // The list is loaded a page at a time and searched on the server
  dineablesRequest: number = 0 // number of the latest list request; answers to older ones are ignored
  searchTimer: any = null
  filterRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getDineablePage(1)
  }

  async getDineablePage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var request = ++this.dineablesRequest

    await this.http.get<IPage<IDineable>>(API_URL + '/dineables/get_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
      .toPromise()
      .then(
        data => {
          if (request != this.dineablesRequest) {
            return
          }
          // The page no longer exists (rows were removed): show the one before it
          if (data!.content.length == 0 && page > 1) {
            this.getDineablePage(page - 1)
            return
          }
          var sn = (page - 1) * this.pageSize + 1
          data!.content.forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          this.dineables = data!.content
          this.totalDineables = data!.totalElements
          this.page = page
        }
      )
  }

  pageChanged(page: number) {
    this.getDineablePage(page)
  }

  searchDineables() {
    // Search on the server once the user pauses typing, starting again from the first page
    clearTimeout(this.searchTimer)
    this.searchTimer = setTimeout(() => {
      this.getDineablePage(1)
    }, 300)
  }

  refreshDineables() {
    this.getDineablePage(this.page)
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

            this.refreshDineables()

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

            this.refreshDineables()

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

          this.refreshDineables()

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

          this.refreshDineables()
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