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


import * as pdfMake from 'pdfmake/build/pdfmake';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';


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

  documentHeader! : any


  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private data : DataService,
    private msg : MsgBoxService
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
            this.msg.showErrorMessage(error, 'Error')
            
            console.log(error)
          }
        )


    return 0; 
  }


  








  exportToPdf = async () => {
    this.documentHeader = await this.data.getDocumentHeader()
    var header = ''
    var footer = ''
    var title  = 'Report Template'
    var logo : any = ''
    var total : number = 0
    var discount : number = 0
    var tax : number = 0
    
    /*this.report.forEach((element) => {
      total = total + element.amount
      discount = discount + element.discount
      tax = tax + element.tax
      var detail = [
        {text : formatDate(element.date, 'yyyy-MM-dd', 'en-US'), fontSize : 9, fillColor : '#ffffff'}, 
        {text : element.amount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},
        {text : element.discount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},  
        {text : element.tax.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},
      ]
      report.push(detail)
    })*/
    /*var detailSummary = [
      {text : 'Total', fontSize : 9, fillColor : '#CCCCCC'}, 
      {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},
      {text : discount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},  
      {text : tax.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},        
    ]
    report.push(detailSummary)*/
    const docDefinition : any = {
      header: '',
      footer: function (currentPage: { toString: () => string; }, pageCount: string) {
        return currentPage.toString() + " of " + pageCount;
      },
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
        content : [
          {
            columns : 
            [
              this.documentHeader
            ]
          },
          '  ',
          '  ',
          {text : title, fontSize : 14, bold : true, alignment : 'center'},
          this.data.getHorizontalLine(),
          '  ',
          '  ',
          '  ',
          {text : title, fontSize : 12, bold : true},
          '  ',
          {
            layout : 'noBorders',
            table : {
              widths : [75, 300],
              body : [
                [
                  {text : 'From', fontSize : 9}, 
                  {text : '', fontSize : 9} 
                ],
                [
                  {text : 'To', fontSize : 9}, 
                  {text : '', fontSize : 9} 
                ],
                [
                  {text : 'Agent/Route', fontSize : 9}, 
                  {text : "", fontSize : 9} 
                ],
              ]
            },
          },
          '  ',
          //{
            //layout : 'noBorders',
            //table : {
                //headerRows : 1,
                //widths : [100, 100, 100, 100, 100],
                //body : report
            //}
        //},                   
      ]     
    };
    pdfMake.createPdf(docDefinition).print()
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
