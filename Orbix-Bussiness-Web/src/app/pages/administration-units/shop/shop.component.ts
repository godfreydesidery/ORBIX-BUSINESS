import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IShop } from 'src/app/domain/shop';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-shop',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './shop.component.html',
  styleUrl: './shop.component.scss'
})
export class ShopComponent {
  /**Data */
  id : any = null
  code : string = ''
  name : string = ''
  locationName : string = ''
  shopCategory : string = 'NORMAL'
  active : string = 'Inactive'

  shop : IShop

  /**Collections */

  shops : IShop[] = []

  /**Identifiers */
  shopId : string = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllShops()
  }

  async getAllShops(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.shops = []

    await this.http.get<IShop[]>(API_URL+'/shops', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.shops.push(element)
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
    await this.http.get<IShop>(API_URL+'/shops/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showShopData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var shop = {
      id : this.id,
      code : this.code,
      name : this.name,
      locationName : this.locationName,
      shopCategory : this.shopCategory
    }

    if(shop.id === null){
      /**Create new shop */
      await this.http.post<IShop>(API_URL+'/shops/create', shop, options)
      .toPromise()
      .then(
        data => {
          this.showShopData(data!)

          console.log(data)

          this.getAllShops()

          this.msg.showSuccessMessage('Shop created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an existing shop */
      await this.http.post<IShop>(API_URL+'/shops/update', shop, options)
      .toPromise()
      .then(
        data => {
          this.showShopData(data!)

          console.log(data)

          this.getAllShops()

          this.msg.showSuccessMessage('Shop updated successifully')
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

    await this.http.post<String>(API_URL+'/shops/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllShops()

          this.msg.showSuccessMessage('Shop activated successifully')

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

    await this.http.post<String>(API_URL+'/shops/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllShops()
          this.msg.showSuccessMessage('Shop deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showShopData(data : IShop){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.locationName = data!.locationName
    this.shopCategory = data!.shopCategory
    
  }

  clearCompanyData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.locationName = ''
  
  }
}