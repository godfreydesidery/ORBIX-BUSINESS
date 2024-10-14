import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { IInvoiceReceivable } from 'src/app/domain/invoice-receivable';
import { IParkingZone } from 'src/app/domain/parking-zone';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-receivable-invoice-list',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './receivable-invoice-list.component.html',
  styleUrl: './receivable-invoice-list.component.scss'
})
export class ReceivableInvoiceListComponent {

 invoiceReceivables : IInvoiceReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllInvoiceReceivables()
  }

  async getAllInvoiceReceivables(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.invoiceReceivables = []

    await this.http.get<IInvoiceReceivable[]>(API_URL+'/invoice_receivables', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.invoiceReceivables.push(element)
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  // async get(id : any){

  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }
  //   await this.http.get<IParkingZone>(API_URL+'/parking_zones/get?id=' + id, options)
  //   .toPromise()
  //   .then(
  //     data => {
  //       this.showParkingZoneData(data!)
  //       console.log(data)
  //     }
  //   )
  // }



}
