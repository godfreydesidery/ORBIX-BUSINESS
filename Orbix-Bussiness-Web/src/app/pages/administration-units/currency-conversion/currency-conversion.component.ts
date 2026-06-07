import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { ICurrencyConversion } from 'src/app/domain/currency-conversion';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-currency-conversion',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './currency-conversion.component.html',
  styleUrl: './currency-conversion.component.scss'
})
export class CurrencyConversionComponent {
  /**Data */
  id: any = null
  sourceCurrencyCode: string = ''
  sourceCurrencyValue: number = 0
  finalCurrencyCode: string = ''
  finalCurrencyValue: number = 0
  active: string = 'Inactive'

  currencyConversion: ICurrencyConversion

  /**Collections */

  currencyConversions: ICurrencyConversion[] = []

  /**Identifiers */
  currencyConversionId: string = ''

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getAllCurrencyConversions()
  }

  async getAllCurrencyConversions() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.currencyConversions = []

    await this.http.get<ICurrencyConversion[]>(API_URL + '/currency_conversions', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.currencyConversions.push(element)
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
    await this.http.get<ICurrencyConversion>(API_URL + '/currency_conversions/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showCurrencyConversionData(data!)
          console.log(data)
        }
      )
  }



  public async save() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var currencyConversion = {
      id: this.id,
      sourceCurrencyCode: this.sourceCurrencyCode,
      sourceCurrencyValue: this.sourceCurrencyValue,
      finalCurrencyCode: this.finalCurrencyCode,
      finalCurrencyValue: this.finalCurrencyValue,
    }

    if (currencyConversion.id === null) {
      /**Create new currencyConversion */
      await this.http.post<ICurrencyConversion>(API_URL + '/currency_conversions/create', currencyConversion, options)
        .toPromise()
        .then(
          data => {
            this.showCurrencyConversionData(data!)

            console.log(data)

            this.getAllCurrencyConversions()

            this.msg.showSuccessMessage('CurrencyConversion created successifully')

          }

        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an existing currencyConversion */
      await this.http.post<ICurrencyConversion>(API_URL + '/currency_conversions/update', currencyConversion, options)
        .toPromise()
        .then(
          data => {
            this.showCurrencyConversionData(data!)

            console.log(data)

            this.getAllCurrencyConversions()

            this.msg.showSuccessMessage('CurrencyConversion updated successifully')
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

    await this.http.post<String>(API_URL + '/currency_conversions/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllCurrencyConversions()

          this.msg.showSuccessMessage('CurrencyConversion activated successifully')

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

    await this.http.post<String>(API_URL + '/currency_conversions/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllCurrencyConversions()
          this.msg.showSuccessMessage('CurrencyConversion deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showCurrencyConversionData(data: ICurrencyConversion) {
    this.id = data?.id
    this.sourceCurrencyCode = data!.sourceCurrencyCode
    this.sourceCurrencyValue = data!.sourceCurrencyValue
    this.finalCurrencyCode = data!.finalCurrencyCode
    this.finalCurrencyValue = data!.finalCurrencyValue

  }

  clearCompanyData() {
    this.id = null
    this.sourceCurrencyCode = ''
    this.sourceCurrencyValue = 0
    this.finalCurrencyCode = ''
    this.finalCurrencyValue = 0
  }
}