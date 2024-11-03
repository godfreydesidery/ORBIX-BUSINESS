import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IParking } from 'src/app/domain/parking';
import { IParkingZone } from 'src/app/domain/parking-zone';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { IParkingBillReceivable } from 'src/app/domain/bill-receivable';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';

const API_URL = environment.apiUrl;


@Component({
  selector: 'az-parking-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './parking-billing.component.html',
  styleUrl: './parking-billing.component.scss'
})
export class ParkingBillingComponent {

  page: number = 1; // Initialize the current page to 1

  filterRecords : string = ''

  startedAt : Date | null
  endedAt : Date | null
  billingType : string
  qty : number
  price : number
  discount : number

  parkingId : any

  autoBilling : any = 1

  parkingAmount : number



  /**Collections */
  parkings : IParking[] = []
  parkingBillReceivables : IParkingBillReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router
  ) {}


  ngOnInit(){
    this.getAllCheckedInParkings()   
  }

  async getAllCheckedInParkings(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkings = []

    await this.http.get<IParking[]>(API_URL+'/parkings/get_all_checked_in', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.parkings.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getParkingBillReceivables(parkingId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingBillReceivables = []

    await this.http.get<IParkingBillReceivable[]>(API_URL+'/parkings/get_parking_bill_receivables?parking_id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.parkingBillReceivables.push(element)
          sn = sn + 1
        })
        this.qty = 0
        this.price = 0
        this.discount = 0
        this.parkingAmount = 0
        this.getParking(parkingId)
        console.log(data)
      }
    )
  }

  async getParking(parkingId : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IParking>(API_URL+'/parkings/get?id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        this.parkingId = data!.id
        this.billingType = data!.billingType
        this.price = data!.billingAmount
      }
    )
  }

  async createParkingBillReceivable(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var parkingBillReceivable = {
      startedAt : this.startedAt,
      endedAt : this.endedAt,
      billingType : this.billingType,
      qty : this.qty,
      price : this.price,
      discount : this.discount,
      autoBilling : this.autoBilling,
      parkingId : this.parkingId
    }

    await this.http.post<IParkingBillReceivable>(API_URL+'/parkings/create_parking_bill_receivable', parkingBillReceivable, options)
    .toPromise()
    .then(
      data => {
        this.getParkingBillReceivables(this.parkingId)
        console.log(data)
      }
    )
    .catch(
      error => {
        console.log(error)
      }
    )


  }

  clearParkingBill(){
    this.startedAt = null
    this.endedAt = null
    this.qty = 0
    this.price = 0
    this.discount = 0
    this.parkingId = null
    this.autoBilling = 1
    this.parkingAmount = 0
  }

  doAutoBilling(){
    this.clearParkingBill()
  }

  doCustomBilling(){
    this.autoBilling = 0
  }

  refreshParkingAmounts(){
    this.parkingAmount = (this.price * this.qty) - this.discount
  }


  async vehicleEquipmentBilling(parkingId : any){

    localStorage.setItem('parking-id', '');
    localStorage.setItem('parking-id', parkingId);

    await this.router.navigate(['app/accounts-and-finance/vehicle-equipment-billing'], { 
      queryParams: { parking_id: parkingId}
    });

  }

}
