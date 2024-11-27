import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/auth.service';
import { IInvoiceReceivable } from 'src/app/domain/invoice-receivable';
import { IParkingZone } from 'src/app/domain/parking-zone';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-parking-receivable-invoice-list',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './parking-receivable-invoice-list.component.html',
  styleUrl: './parking-receivable-invoice-list.component.scss'
})
export class ParkingReceivableInvoiceListComponent {

  invoiceReceivables : IInvoiceReceivable[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router
  ) {}

  ngOnInit(){
    this.getPendingParkingInvoiceReceivables()
  }

  async getPendingParkingInvoiceReceivables(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.invoiceReceivables = []

    await this.http.get<IInvoiceReceivable[]>(API_URL+'/invoice_receivables/get_pending_parking_invoice_receivables', options)
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

  async loadInvoice(id : any){

    localStorage.setItem('receivable-invoice-id', '');
    localStorage.setItem('receivable-invoice-id', id);

    this.router.navigate(['app/accounts-and-finance/invoices/receivable-invoice'], { 
      queryParams: { id: id }
    });

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
