import { CommonModule, formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { HttpHeaders } from '@angular/common/http';

import * as pdfMake from 'pdfmake/build/pdfmake';

import { environment } from 'src/environments/environment';
import { ICashCollection, IParkingCashCollection, IParkingServiceCashCollection } from 'src/app/domain/cash-collection';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-shop-product-stock-log',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './shop-product-stock-log.component.html',
  styleUrl: './shop-product-stock-log.component.scss'
})
export class ShopProductStockLogComponent {
documentHeader! : any

  from : Date | string | null = null
  to : Date | string | null = null

  nickname = ''

  shopId : any = null

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

    ngOnInit() {
      this.route.queryParams.subscribe(params => {
        this.shopId = params['shop_id'];
        console.log('Shop ID:', this.shopId);
      });
      const today = new Date();
      this.from = today.toISOString().split('T')[0];
      this.to = today.toISOString().split('T')[0];

      // this.getFastMovingProductsByDate(this.from, this.to);
      // this.getBranchUserNames();
    }

    fastMovingProducts : IFastMovingProducts[] = []
    total : number = 0

  async getStockLogByDate(from : Date | string | null, to : Date | string | null) {
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

    this.fastMovingProducts = []
    this.total = 0
    

    await this.http.post<IFastMovingProducts[]>(API_URL+'/shop_stock_logs/get_stock_logs_report_by_dates?shop_id=' + this.shopId, args, options)
        .toPromise()
        .then(
          data => {

            this.fastMovingProducts = data!

            var sn = 1
            this.total = 0
            this.fastMovingProducts.forEach(element => {
              element.sn = sn
              this.total = this.total + (+element.amount)
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



  




  userNames : string[] = []
  async getBranchUserNames(){
    this.userNames = []

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<string[]>(API_URL+'/users/get_branch_user_names', options)
      .toPromise()
      .then(
        data => {
          this.userNames = data!
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )

  }









  printReport = async () => {
    this.documentHeader = await this.data.getDocumentHeader()
    var header = ''
    var footer = ''
    var title  = 'Fast Moving Products Report'
    const from = this.from?.toString();
    const to = this.to?.toString();
    var logo : any = ''
    var total : number = 0
    var discount : number = 0
    var tax : number = 0

    

    const report = [];

    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Product Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Qty', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);

    // Add rows dynamically


    this.fastMovingProducts.forEach(element => {
      total += Number(element.amount) || 0;

      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.productName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
      ])
    })
    
    report.push([
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: 'Total', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: (Number(total) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },       
    ])

    const docDefinition : any = {
      header: '',
      pageOrientation: 'potrait', // Set the orientation to landscape
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
        {text: title, fontSize: 14, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {text: 'From: '+ from + ' To: ' + to , fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {
          //layout : 'noBorders',
          table : {
              headerRows : 1,
              widths : [30, 140, 40, 70],
              body : report
          }
        },                   
      ]     
    };
    pdfMake.createPdf(docDefinition).print()
  }
}

export interface IFastMovingProducts {
  sn : any
  dateTime : string
  productName : string
  qtyIn : string
  qtyOut : string
  balance : string
  nickname : string
  reference : string
  qty : string
  amount : string
}