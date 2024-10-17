import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/auth.service';
import { IInvoiceReceivable } from 'src/app/domain/invoice-receivable';
import { IParkingZone } from 'src/app/domain/parking-zone';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-receivable-invoice',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './receivable-invoice.component.html',
  styleUrl: './receivable-invoice.component.scss'
})
export class ReceivableInvoiceComponent {
  id: string;

  invoiceReceivable : IInvoiceReceivable
  
  constructor(
    private route: ActivatedRoute,
    private http :HttpClient,
    private auth : AuthService,
    private router : Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.get(params['id']) // Get invoice with specified id
    })
  }

  async get(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IInvoiceReceivable>(API_URL+'/invoice_receivables/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.invoiceReceivable = data! 
        this.id = data!.id 
        console.log(data)      
      }
    )
  }
}
