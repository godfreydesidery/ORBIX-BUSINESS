import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { HttpHeaders } from '@angular/common/http';

import { environment } from 'src/environments/environment';
import { ICashCollection, IParkingCashCollection, IParkingServiceCashCollection } from 'src/app/domain/cash-collection';
import { MsgBoxService } from '@services/custom/msg-box.service';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-cash-collection',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './cash-collection.component.html',
  styleUrl: './cash-collection.component.scss'
})
export class CashCollectionComponent {

  from : Date | string | null = null
  to : Date | string | null = null

  nickname = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

    ngOnInit() {
      const today = new Date();
      this.from = today.toISOString().split('T')[0];
      this.to = today.toISOString().split('T')[0];

      this.getTotalsByDates(this.from, this.to);
    }

    cashCollections : ICashCollection[] = []
    totalCollections : number = 0

  async getTotalsByDates(from : Date | string | null, to : Date | string | null) {
    if(from == null || to == null) {
      from = new Date()
      to = new Date()
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var args = {
      from : from,
      to : to,
    }

    this.cashCollections = []
    this.totalCollections = 0
    

    await this.http.post<ICashCollection[]>(API_URL+'/finance_reports/get_cash_collections_by_dates?nickname=' + this.nickname, args, options)
        .toPromise()
        .then(
          data => {

            this.cashCollections = data!

            var sn = 1
            this.totalCollections = 0
            this.cashCollections.forEach(element => {
              element.sn = sn
              this.totalCollections = this.totalCollections + (+element.amount)
              sn = sn + 1
            })


            
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            
            console.log(error)
          }
        )


    return 0; 
  }

  parkingCashCollections : IParkingCashCollection[] = []
    totalParkingCashCollections : number = 0

  async getParkingDetailedTotalsByDates(from : Date | string | null, to : Date | string | null) {
    if(from == null || to == null) {
      from = new Date()
      to = new Date()
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var args = {
      from : from,
      to : to,
    }

    this.parkingCashCollections = []
    this.totalParkingCashCollections = 0
    

    await this.http.post<IParkingCashCollection[]>(API_URL+'/finance_reports/get_parking_detailed_collections_by_dates', args, options)
        .toPromise()
        .then(
          data => {

            this.parkingCashCollections = data!

            var sn = 1
            this.totalParkingCashCollections = 0
            this.parkingCashCollections.forEach(element => {
              element.sn = sn
              this.totalParkingCashCollections = this.totalParkingCashCollections + (+element.amount)
              sn = sn + 1
            })


            
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            
            console.log(error)
          }
        )


    return 0; 
  }


  parkingServiceCashCollections : IParkingServiceCashCollection[] = []
    totalParkingServiceCashCollections : number = 0

  async getParkingServiceDetailedTotalsByDates(from : Date | string | null, to : Date | string | null) {
    if(from == null || to == null) {
      from = new Date()
      to = new Date()
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var args = {
      from : from,
      to : to,
    }

    this.parkingServiceCashCollections = []
    this.totalParkingServiceCashCollections = 0
    

    await this.http.post<IParkingServiceCashCollection[]>(API_URL+'/finance_reports/get_parking_service_detailed_collections_by_dates', args, options)
        .toPromise()
        .then(
          data => {

            this.parkingServiceCashCollections = data!

            var sn = 1
            this.totalParkingServiceCashCollections = 0
            this.parkingServiceCashCollections.forEach(element => {
              element.sn = sn
              this.totalParkingServiceCashCollections = this.totalParkingServiceCashCollections + (+element.amount)
              sn = sn + 1
            })


            
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            
            console.log(error)
          }
        )


    return 0; 
  }

}


