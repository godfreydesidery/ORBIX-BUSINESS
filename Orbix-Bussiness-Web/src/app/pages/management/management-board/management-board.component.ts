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


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-management-board',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './management-board.component.html',
  styleUrl: './management-board.component.scss'
})
export class ManagementBoardComponent {

  from : Date | string | null = null
  to : Date | string | null = null

  registered : string = ''
  paid : string= ''
  checkedOut : string = ''

  currentUnpaid : string = ''
  currentTotalInYards : string = ''


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

    await this.http.post<IParkingTotalsByDates>(API_URL+'/parking_reports/get_totals_by_dates', args, options)
        .toPromise()
        .then(
          data => {

            this.registered = data!.registered
            this.paid = data!.paid
            this.checkedOut = data!.checkedOut

            this.currentUnpaid = data!.currentUnpaid
            this.currentTotalInYards = data!.currentTotalInYards 
            
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

interface IParkingTotalsByDates {
  from : string;
  to : string;
  registered : string
  paid : string
  checkedOut : string
  currentUnpaid : string
  currentTotalInYards : string
}
