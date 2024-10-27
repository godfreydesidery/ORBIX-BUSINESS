import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/auth.service';
import { IBillReceivable } from 'src/app/domain/bill-receivable';
import { IInvoiceReceivable, IInvoiceReceivableDetail } from 'src/app/domain/invoice-receivable';
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
  id : any

  no : string = ''

  invoiceReceivable : IInvoiceReceivable
  
  constructor(
    private route: ActivatedRoute,
    private http :HttpClient,
    private auth : AuthService,
    private router : Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.id = this.get(params['id']) // Get invoice with specified id
    })
    this.get(this.id)
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
        this.no = data!.no
        
        console.log(data)      
      }
    )
    .catch(error => {
      console.log(error)
    })
  }

  billReceivables : IBillReceivable[] = []
  total : number = 0
  amountReceived : number = 0


  listBill(detail : IInvoiceReceivableDetail){

    if(detail.checked === true){
      var present : boolean = false
      this.billReceivables.forEach(element => {
        if(element.id === detail.billReceivable.id){
          present = true
        }
      })
      if(present === false){
        this.billReceivables.push(detail.billReceivable)
      }
    }else if(detail.checked === false){
      var tempBills : IBillReceivable[] = []
      this.billReceivables.forEach(element => {
        if(element.id != detail.billReceivable.id){
          tempBills.push(element)
        }
      })
      this.billReceivables = tempBills      
    }

    this.total = 0
    this.amountReceived = 0
    this.invoiceReceivable.invoiceReceivableDetails.forEach(element => {
      if(element.billReceivable.status === 'UNPAID'){
        this.billReceivables.forEach(e => {
          if(e.id === element.billReceivable.id){
            element.checked = true
            this.total = this.total + (+element.billReceivable.amount)
          }
        })
      }else{
        element.checked = false
      }
    })
    console.log(detail)
  }


  async confirmBillsPayment(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    //this.spinner.show()
    await this.http.post<IBillReceivable>(API_URL+'/bill_receivables/confirm_bills_payment?total_amount='+this.total, this.billReceivables, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {
        console.log(data)
        //this.msgBox.showSuccessMessage('Payment successiful')
        alert('Payment successiful')
      }
    )
    .catch(
      error => {
        console.log(error)
        //this.msgBox.showErrorMessage(error, 'Could not confirm payment')
        alert('An error occured')
      }
    )
  }


}
