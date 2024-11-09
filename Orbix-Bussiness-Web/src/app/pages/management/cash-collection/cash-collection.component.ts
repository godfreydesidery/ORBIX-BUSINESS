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
import { ICashCollection } from 'src/app/domain/cash-collection';


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
    private printer : PosReceiptPrinterService
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
            alert('An error has occured')
            
            console.log(error)
          }
        )


    return 0; 
  }

}


