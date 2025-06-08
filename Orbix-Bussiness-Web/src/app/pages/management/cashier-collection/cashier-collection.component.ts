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
  selector: 'az-cashier-collection',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './cashier-collection.component.html',
  styleUrl: './cashier-collection.component.scss'
})
export class CashierCollectionComponent {
  documentHeader! : any

  from : Date | string | null = null
  to : Date | string | null = null

  nickname = ''

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
      const today = new Date();
      this.from = today.toISOString().split('T')[0];
      this.to = today.toISOString().split('T')[0];

      this.getTotalsByDates(this.from, this.to);
      this.getBranchUserNames();
    }

    cashierCollections : ICashierCollection[] = []
    totalAmount : number = 0
    totalCollections : number = 0
    totalDiscounts : number = 0

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

    this.cashierCollections = []
    this.totalAmount = 0
    this.totalCollections = 0
    this.totalDiscounts = 0

    if(this.nickname === '--All--') this.nickname = ''
    
    await this.http.post<ICashierCollection[]>(API_URL+'/finance_reports/get_cashier_collections_by_dates?nickname=' + this.nickname, args, options)
        .toPromise()
        .then(
          data => {

            this.cashierCollections = data!

            var sn = 1
            this.totalCollections = 0
            this.cashierCollections.forEach(element => {
              element.sn = sn
              this.totalAmount = this.totalAmount + ((+element.amount) + (+element.discount))
              this.totalCollections = this.totalCollections + (+element.amount)
              this.totalDiscounts = this.totalDiscounts + (+element.discount)
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









  printCashierCollectionReport = async () => {
    this.documentHeader = await this.data.getDocumentHeaderLandScape()
    var header = ''
    var footer = ''
    var title  = 'Cashier Collection Report'
    const from = this.from?.toString();
    const to = this.to?.toString();
    var logo : any = ''
    var totalUndiscounted : number = 0
    var totalCollections : number = 0
    var totalDiscounts : number = 0
    var discount : number = 0
    var tax : number = 0

    

    const report = [];

    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Chasis No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Vehicle Type', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Days', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Checkin Date', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Time', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Checkout Date', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Time', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Discount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Pay Code', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Cashier Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Discount Approved', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);

    // Add rows dynamically


    this.cashierCollections.forEach(element => {
      totalUndiscounted += Number((+element.amount) + (+element.discount)) || 0;
      totalCollections += Number(element.amount) || 0;
      totalDiscounts += Number(element.discount) || 0;

      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.chasisNo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentTypeName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedInDate || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedInTime || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedOutDate || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedOutTime || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: (Number(element.discount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: element.payCode || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.cashierName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: (element.discountApprovedBy || '') + ' ' + (element.discountApprovedDate || ''), fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ])
    })
    
    report.push([
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: 'Totals', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: (Number(totalDiscounts) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: (Number(totalCollections) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },        
    ])
    report.push([
      { text: 'Total before discount', colSpan: 9, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      {}, {}, {}, {}, {}, {}, {}, {},  // Empty cells for colSpan
      { text: (Number(totalUndiscounted) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false }, 
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },       
    ])

    report.push([
      { text: 'Total discount', colSpan: 9, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      {}, {}, {}, {}, {}, {}, {}, {},  // Empty cells for colSpan
      { text: (Number(totalDiscounts) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },  
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },      
    ])
    report.push([
      { text: 'Total after discount', colSpan: 9, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      {}, {}, {}, {}, {}, {}, {}, {},  // Empty cells for colSpan
      { text: (Number(totalCollections) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false }, 
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },       
    ])

    const docDefinition : any = {
      header: '',
      pageOrientation: 'landscape', // Set the orientation to landscape
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
              widths : [20, 50, 70, 30, 50, 50, 50, 50, 60, 50, 70, 70, 60],                
              body : report
          }
        },                   
      ]     
    };
    pdfMake.createPdf(docDefinition).print()
  }

  printParkingCollectionReport = async () => {
    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Parking Collection Report';
    const fromTo = 'From: ' +this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;
  
    const report: any[] = [];
  
    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Category', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Vehicle', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Chassis No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Registered', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Days', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Cashier', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);
  
    // Add rows dynamically
    this.parkingCashCollections.forEach((element) => {
      // total += parseFloat(element.amount) || 0;
      // discount += parseFloat(element.discount) || 0;

      total += Number(element.amount) || 0;
      discount += Number(element.discount) || 0;
  
      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentCategory || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: `${element.ownerFirstName || ''} ${element.ownerLastName || ''}`, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.chasisNo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.createdDateTime.substring(0, 10), fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.days || '', fontSize: 9, alignment: 'center', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: element.cashierName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ]);
    });
  
    // Add summary row
    report.push([
      { text: '', colSpan: 7 },
      {},
      {},
      {},
      {},
      {},
      { text: 'Total', fontSize: 9, alignment: 'right', bold: true },
      { text: total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
      { text: '', fontSize: 9, alignment: 'left' },
    ]);
  
    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'landscape',
      footer: (currentPage: any, pageCount: any) => ({
        text: `${currentPage} of ${pageCount}`,
        alignment: 'center',
        fontSize: 8,
      }),
      content: [
        {
          columns: [
            this.documentHeader,
          ],
        },
        {text : ' '},
        {text: title, fontSize: 14, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {text: fromTo , fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {
          table: {
            widths: [25, 75, 100, 100, 100, 60, 50, 80, 80],
            body: report,
          },
        },
      ],
    };
  
    pdfMake.createPdf(docDefinition).print();
  };



  printParkingServiceCollectionReport = async () => {
    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Vehicle Services Collection Report';
    const fromTo = 'From: ' +this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;
  
    const report: any[] = [];
  
    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Category', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Vehicle', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Chassis No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Registered', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Service', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Qty', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },      
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Cashier', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);
  
    // Add rows dynamically
    this.parkingServiceCashCollections.forEach((element) => {
      // total += parseFloat(element.amount) || 0;
      // discount += parseFloat(element.discount) || 0;

      total += Number(element.amount) || 0;
      discount += Number(element.discount) || 0;
  
      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentCategory || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: `${element.ownerFirstName || ''} ${element.ownerLastName || ''}`, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.chasisNo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.createdDateTime.substring(0, 10), fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.serviceDescription || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 9, alignment: 'center', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: element.cashierName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ]);
    });
  
    // Add summary row
    report.push([
      { text: '', colSpan: 7 },
      {},
      {},
      {},
      {},
      {},
      {},
      { text: 'Total', fontSize: 9, alignment: 'right', bold: true },
      { text: total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
      { text: '', fontSize: 9, alignment: 'left' },
    ]);
  
    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'landscape',
      footer: (currentPage: any, pageCount: any) => ({
        text: `${currentPage} of ${pageCount}`,
        alignment: 'center',
        fontSize: 8,
      }),
      content: [
        {
          columns: [
            this.documentHeader,
          ],
        },
        {text : ' '},
        {text: title, fontSize: 14, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {text: fromTo , fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {
          table: {
            widths: [25, 70, 100, 100, 100, 60, 60, 30, 65, 80],
            body: report,
          },
        },
      ],
    };
  
    pdfMake.createPdf(docDefinition).print();
  };




}


export interface ICashierCollection {
  sn : number
  chasisNo : string
  checkedInDateTime : string
  checkedOutDateTime : string
  checkedInDate : string
  checkedInTime : string
  checkedOutDate : string
  checkedOutTime : string
  vehicleEquipmentTypeName : string
  qty : string
  serviceType : string
  amount : string
  discount : string
  payCode : string
  cashierName : string
  discountApprovedBy : string
  discountApprovedDateTime : string
  discountApprovedDate : string
  discountApprovedTime : string
}


