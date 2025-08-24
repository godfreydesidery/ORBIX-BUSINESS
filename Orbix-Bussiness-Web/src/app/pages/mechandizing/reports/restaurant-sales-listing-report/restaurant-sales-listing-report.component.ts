import { CommonModule, formatDate } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
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
import { IRestaurant } from 'src/app/domain/restaurant';
import { IRestaurantSalesListingReport } from 'src/app/domain/restaurant-sales-listing-report';


var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-restaurant-sales-listing-report',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-sales-listing-report.component.html',
  styleUrl: './restaurant-sales-listing-report.component.scss'
})
export class RestaurantSalesListingReportComponent {
  documentHeader!: any

  from: Date | string | null = null
  to: Date | string | null = null

  nickname = ''
  agentName = ''

  restaurantId: any = null

  selectedRestaurant: IRestaurant

  restaurantName: string = ''

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

    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    this.loadSelectedRestaurant()

    // this.getSalesListingByDate(this.from, this.to);
    this.getBranchUserNames();
    this.getBranchAgentNames();
  }

  salesListings: IRestaurantSalesListingReport[] = []
  totalCost: number = 0
  totalDiscount: number = 0
  totalAmount: number = 0
  totalProfit: number = 0



  async getSalesListingByDate11(from: Date | string | null, to: Date | string | null) {
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
    }

    this.salesListings = []
    this.totalCost = 0
    this.totalDiscount = 0
    this.totalAmount = 0
    this.totalProfit = 0

    var restId = ''

    if (this.restaurantId != null) restId = this.restaurantId


    await this.http.get<IRestaurantSalesListingReport[]>(API_URL +
      '/get_restaurant_sales_listing_report?start_date=' + from + 'T00:00:00' + '&end_date=' + to + 'T23:59:59' + '&restaurant_id=' + restId + '&agent_name=' + this.nickname + '&nickname=' + this.nickname,
      options)
      .toPromise()
      .then(
        data => {

          this.salesListings = data!

          var sn = 1
          this.totalCost = 0
          this.totalDiscount = 0
          this.totalAmount = 0
          this.totalProfit = 0
          // this.salesListings.forEach(element => {
          //   element.sn = sn
          //   this.totalCost = this.totalCost + (+element.cost)
          //   this.totalDiscount = this.totalDiscount + (+element.discount)
          //   this.totalAmount = this.totalAmount + (+element.amount)
          //   this.totalProfit = this.totalProfit + (+element.profit)
          //   sn = sn + 1
          // })



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

  async getSalesListingByDate(from: Date | string | null, to: Date | string | null) {
    if (!from) from = new Date();
    if (!to) to = new Date();

    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    };

    // Convert dates to ISO string with time
    const startDate = (from instanceof Date ? from : new Date(from)).toISOString().split('T')[0] + 'T00:00:00';
    const endDate = (to instanceof Date ? to : new Date(to)).toISOString().split('T')[0] + 'T23:59:59';

    let params = new HttpParams()
      .set('start_date', startDate)
      .set('end_date', endDate);

    // Only set optional params if they have a value
    if (this.restaurantId) {
      params = params.set('restaurant_id', this.restaurantId.toString());
    }
    if (this.nickname?.trim()) {
      //params = params.set('agent_name', this.agentName);
      params = params.set('nickname', this.nickname);
    }

    if (this.agentName?.trim()) {
      params = params.set('agent_name', this.agentName);
      //params = params.set('nickname', this.nickname);
    }

    try {
      const data = await this.http.get<IRestaurantSalesListingReport[]>(
        API_URL + '/get_restaurant_sales_listing_report',
        { headers: options.headers, params }
      ).toPromise();

      this.salesListings = data ?? [];

      var sn = 1

      this.salesListings.forEach(element => {
        element.sn = sn
        sn++
      })
      this.totalCost = 0;
      this.totalDiscount = 0;
      this.totalAmount = 0;
      this.totalProfit = 0;

      console.log(data);
    } catch (error) {
      this.msg.showErrorMessage(error, 'Error');
      console.error(error);
    }

    return 0;
  }




  loadSelectedRestaurant = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurant>(API_URL + '/restaurants/get_selected_restaurant?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.restaurantName = data!.name
          this.selectedRestaurant = data!

        }
      )
      .catch(error => {
        console.log(error)

      }
      )
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

  agentNames: string[] = []
  async getBranchAgentNames() {
    this.agentNames = []

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<string[]>(API_URL + '/users/get_branch_agent_names', options)
      .toPromise()
      .then(
        data => {
          this.agentNames = data!
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
    var title = 'Restaurant Sales Listing Report'
    const from = this.from?.toString();
    const to = this.to?.toString();
    var logo: any = ''
    var totalAmount: number = 0
    var totalCost: number = 0
    var totalDiscount: number = 0
    var totalProfit: number = 0
    var discount: number = 0
    var tax: number = 0



    const report = [];

    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Product Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Qty', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Time', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Confirmed By', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Agent Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },

    ]);

    // Add rows dynamically


    this.salesListings.forEach(element => {
      totalAmount += Number(element.amount) || 0;
      // totalProfit += Number(element.profit) || 0;
      // totalCost += Number(element.cost) || 0;
      // totalDiscount += Number(element.discount) || 0;

      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.dineableName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: (Number(element.amount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: element.dateTime || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.confirmedBy || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.agentName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ])
    })

    report.push([
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: 'Total', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: (Number(totalAmount) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: true },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      { text: '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
    ])

    const docDefinition: any = {
      header: '',
      pageOrientation: 'potrait', // Set the orientation to landscape
      footer: function (currentPage: { toString: () => string; }, pageCount: string) {
        return currentPage.toString() + " of " + pageCount;
      },
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
      content: [
        {
          columns:
            [
              this.documentHeader
            ]
        },
        { text: title, fontSize: 14, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        { text: 'From: ' + from + ' To: ' + to, fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        {
          //layout : 'noBorders',
          table: {
            headerRows: 1,
            widths: [30, 120, 40, 70, 50, 70, 80, 70, 80],
            body: report
          }
        },
      ]
    };
    pdfMake.createPdf(docDefinition).print()
  }
}

export interface ISalesListing {
  sn: any
  productName: string
  qty: string
  amount: string
  cost: string
  discount: string
  profit: string
  createdBy: string
  timeDate: string
}