import { Component } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { HttpHeaders } from '@angular/common/http';

import * as pdfMake from 'pdfmake/build/pdfmake';

import { environment } from 'src/environments/environment';
import { ICashCollection, IStorageCashCollection } from 'src/app/domain/cash-collection';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { TimePipe } from 'src/app/custom-pipes/time.pipe';

import * as XLSX from 'xlsx';
import * as FileSaver from 'file-saver';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-goods-removed-report',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule,
    TimePipe
  ],
  templateUrl: './goods-removed-report.component.html',
  styleUrl: './goods-removed-report.component.scss'
})
export class GoodsRemovedReportComponent {
documentHeader! : any

  from : Date | string | null = null
  to : Date | string | null = null

  nickname = ''
  status = '--All--'

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
      // const today = new Date();
      // this.from = today.toISOString().split('T')[0];
      // this.to = today.toISOString().split('T')[0];

      //this.getRegistationByDate(this.from, this.to);
    }

    // registrations : IRegistration[] = []
    goodsRemovedReports : IGoodRemoved[] = []

  async getRegistationByDate(from : Date | string | null, to : Date | string | null) {
    if(from == null || to == null) {
      from = new Date()
      to = new Date()
      const today = new Date();
      this.from = today.toISOString().split('T')[0];
      this.to = today.toISOString().split('T')[0];
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var args = {
      from : from,
      to : to,
    }

    this.goodsRemovedReports = []
    

    await this.http.post<IGoodRemoved[]>(API_URL+'/storage_reports/get_goods_removed_report', args, options)
        .toPromise()
        .then(
          data => {

            this.goodsRemovedReports = data!

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


  printStorageReport = async () => {
    if (this.goodsRemovedReports.length === 0) {
      this.msg.showErrorMessage3('No data to export');
      return;
    }
    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Godds Archived Report';
    const fromTo = 'From: ' +this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;

    // Set up VFS for pdfMake - try different approaches
    try {
      const vfsFonts = require('pdfmake/build/vfs_fonts.js');
      // Try different possible structures
      if (vfsFonts.pdfMake && vfsFonts.pdfMake.vfs) {
        (window as any).pdfMake.vfs = vfsFonts.pdfMake.vfs;
      } else if (vfsFonts.vfs) {
        (window as any).pdfMake.vfs = vfsFonts.vfs;
      } else {
        (window as any).pdfMake.vfs = vfsFonts;
      }
    } catch (error) {
      console.log('VFS setup failed, continuing without custom fonts:', error);
    }
  
    const report: any[] = [];
  
    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Customer name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Phone No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Good Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Qty', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Reason', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Time', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Registered By', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Archived By', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);
 
    // Add rows dynamically
    this.goodsRemovedReports.forEach((element) => {
      // total += parseFloat(element.amount) || 0;
      // discount += parseFloat(element.discount) || 0;

      report.push([
        { text: element.sn || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.customerName || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.phoneNo || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.goodName || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.reason || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.dateTime || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.registeredBy || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.removedBy || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
      ]);
    });
  
    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'landscape', // landscape for Landscape
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
            widths: [30, 100, 50, 100, 30, 100, 100, 90, 90],
            body: report,
          },
        },
      ],
    };
  
    pdfMake.createPdf(docDefinition).print();
  };

  

  

  exportToExcel(): void {
    if (this.goodsRemovedReports.length === 0) {
      this.msg.showErrorMessage3('No data to export');
      return;
    }

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.goodsRemovedReports.map((item)=>({
    'S/N': item.sn,
    'Customer Name': item.customerName,
    'Phone No': item.phoneNo,
    'Good Name': item.goodName,
    'Qty': item.qty,
    'Reason': item.reason,
    'Date Time': item.dateTime,
    'Registered By': item.registeredBy,
    'Archved By': item.removedBy
    })));
    const workbook: XLSX.WorkBook = {
      Sheets: { 'Report': worksheet },
      SheetNames: ['Report']
    };

    const excelBuffer: any = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array'
    });

    const fileName = 'Goods Archived Report ' + this.from + ' - ' + this.to + '.xlsx';
    this.saveAsExcelFile(excelBuffer, fileName);

    // const blob = new Blob([excelBuffer], {
    //   type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
    // });
  
    // const url = window.URL.createObjectURL(blob);
    // window.open(url); // Try to open in new tab (Excel MIME handler might catch it)
  
    // saveAs(blob, 'vehicle-report.xlsx'); // Also prompt user to save/download
  }

  private saveAsExcelFile(buffer: any, fileName: string): void {
    const data: Blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
    });
    FileSaver.saveAs(data, fileName);
  }


}

// export interface IRegistration{
//   sn : string;
//   chassisNo : string;
//   vehicleType : string;
//   keyStatus : string;
//   registeredDate : string;
//   registeredBy : string;
// }

export interface IGoodRemoved{
  sn : string;
  customerName: string;
  phoneNo : string
  goodName : string;
  qty : string;
  reason : string
  dateTime : string;
  registeredBy: string;
  removedBy: string
}
