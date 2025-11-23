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
import { IWeighCashCollection } from 'src/app/domain/cash-collection';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-weigh-cash-collection',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './weigh-cash-collection.component.html',
  styleUrl: './weigh-cash-collection.component.scss'
})
export class WeighCashCollectionComponent {
  documentHeader!: any

  from: Date | string | null = null
  to: Date | string | null = null

  nickname = ''

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
  ) { } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    const today = new Date();
    this.from = today.toISOString().split('T')[0];
    this.to = today.toISOString().split('T')[0];
    this.getBranchUserNames();
  }

  totalCollections: number = 0

  weighCashCollections: IWeighCashCollection[] = []
  totalWeighCashCollections: number = 0

  async getWeighDetailedTotalsByDates(from: Date | string | null, to: Date | string | null) {
    if (from == null || to == null) {
      from = new Date()
      to = new Date()
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var args = {
      from: from,
      to: to,
      nickname: this.nickname
    }

    this.weighCashCollections = []
    this.totalWeighCashCollections = 0

    await this.http.post<IWeighCashCollection[]>(API_URL + '/finance_reports/get_weigh_detailed_collections_by_dates', args, options)
      .toPromise()
      .then(
        data => {
          this.weighCashCollections = data!
          var sn = 1
          this.totalWeighCashCollections = 0
          this.weighCashCollections.forEach(element => {
            element.sn = sn
            this.totalWeighCashCollections = this.totalWeighCashCollections + (+element.amount)
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

  userNames: string[] = []
  async getBranchUserNames() {
    this.userNames = []

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<string[]>(API_URL + '/users/get_branch_user_names', options)
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

  printWeighCollectionReport = async () => {
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

    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Weigh Bridge Collection Report';
    const fromTo = 'From: ' + this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;

    const report: any[] = [];

    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Plate No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Registered', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Axle One', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Axle Two', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Axle Three', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Axle Four', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Cashier', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);

    // Add rows dynamically
    this.weighCashCollections.forEach((element) => {

      total += Number(element.amount) || 0;
      discount += Number(element.discount) || 0;

      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: `${element.ownerFirstName || ''}`, fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.regNo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.createdDateTime.substring(0, 10), fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: element.weightOne || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.weightTwo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.weightThree || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.weightFour || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.cashierName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ]);
    });

    // Add summary row
    report.push([
      { text: '' },
      {},
      {},
      { text: 'Total', fontSize: 9, alignment: 'right', bold: true },
      { text: total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
      {},
      {},
      {},
      {},
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
        { text: ' ' },
        { text: title, fontSize: 14, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        { text: fromTo, fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {
          table: {
            widths: [25, 100, 100, 60, 80, 50, 50, 50, 50, 80],
            body: report,
          },
        },
      ],
    };

    pdfMake.createPdf(docDefinition).print();
  };
}
