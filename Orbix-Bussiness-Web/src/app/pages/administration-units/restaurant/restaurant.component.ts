import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IRestaurant } from 'src/app/domain/restaurant';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-restaurant',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './restaurant.component.html',
  styleUrl: './restaurant.component.scss'
})
export class RestaurantComponent {
/**Data */
  id : any = null
  code : string = ''
  name : string = ''
  locationName : string = ''
  restaurantCategory : string = 'NORMAL'
  active : string = 'Inactive'

  restaurant : IRestaurant

  /**Collections */

  restaurants : IRestaurant[] = []

  /**Identifiers */
  restaurantId : string = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService
  ) {}

  ngOnInit(){
    this.getAllRestaurants()
  }

  async getAllRestaurants(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.restaurants = []

    await this.http.get<IRestaurant[]>(API_URL+'/restaurants', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.restaurants.push(element)
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
    await this.http.get<IRestaurant>(API_URL+'/restaurants/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showRestaurantData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var restaurant = {
      id : this.id,
      code : this.code,
      name : this.name,
      locationName : this.locationName,
      restaurantCategory : this.restaurantCategory
    }

    if(restaurant.id === null){
      /**Create new restaurant */
      await this.http.post<IRestaurant>(API_URL+'/restaurants/create', restaurant, options)
      .toPromise()
      .then(
        data => {
          this.showRestaurantData(data!)

          console.log(data)

          this.getAllRestaurants()

          this.msg.showSuccessMessage('Restaurant created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }else{
      /**Update an existing restaurant */
      await this.http.post<IRestaurant>(API_URL+'/restaurants/update', restaurant, options)
      .toPromise()
      .then(
        data => {
          this.showRestaurantData(data!)

          console.log(data)

          this.getAllRestaurants()

          this.msg.showSuccessMessage('Restaurant updated successifully')
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

    await this.http.post<String>(API_URL+'/restaurants/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllRestaurants()

          this.msg.showSuccessMessage('Restaurant activated successifully')

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

    await this.http.post<String>(API_URL+'/restaurants/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllRestaurants()
          this.msg.showSuccessMessage('Restaurant deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showRestaurantData(data : IRestaurant){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.locationName = data!.locationName
    this.restaurantCategory = data!.restaurantCategory
    
  }

  clearCompanyData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.locationName = ''
  
  }
}