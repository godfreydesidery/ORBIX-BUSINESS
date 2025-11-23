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
import { SettingsService } from '@services/settings.service';

import { ViewEncapsulation } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

import { environment } from 'src/environments/environment';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 


import * as pdfMake from 'pdfmake/build/pdfmake';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { ChartConfiguration, ChartOptions } from 'chart.js';


const API_URL = environment.apiUrl;

@Component({
  selector: 'az-management-board',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule,
    NgChartsModule,
    DirectivesModule
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


  ///////////////////////////////////////

  public settings: any; 
  
    public verticalBarChartType: any = 'bar';
    public verticalBarChartLegend: boolean = true;
    public verticalBarChartPlugins = [];
    public verticalBarChartData: ChartConfiguration<'bar'>['data'] = { datasets: [] };
    public verticalBarChartOptions: ChartOptions<'bar'>;
  
    public horizontalBarChartType: any = 'bar';
    public horizontalBarChartLegend: boolean = true;
    public horizontalBarChartPlugins = [];
    public horizontalBarChartData: ChartConfiguration<'bar'>['data'] = { datasets: [] };
    public horizontalBarChartOptions: ChartOptions<'bar'>;
  
    public lineChartType: any = 'line';
    public lineChartLegend: boolean = true;
    public lineChartData: ChartConfiguration<'line'>['data'] = { datasets: [] };
    public lineChartOptions: ChartOptions<'line'>;
  
    public doughnutChartType: any = 'doughnut';
    public pieChartType: any = 'pie';
    public pieChartData: ChartConfiguration<'pie'>['data'] = { datasets: [] };
    public pieChartOptions: ChartOptions<'pie'>;
    public pieChartLegend: boolean = true;
  
    public radarChartType: any = 'radar';
    public radarChartLegend: boolean = true;
    public radarChartData: ChartConfiguration<'radar'>['data'] = { datasets: [] };
    public radarChartOptions: ChartOptions<'radar'>;
  
    public polarAreaChartType: any = 'polarArea';
    public polarAreaChartLegend: boolean = true;
    public polarAreaChartData: ChartConfiguration<'polarArea'>['data'] = { datasets: [] };
    public polarAreaChartOptions: ChartOptions<'polarArea'>;



  ////////////////////////////////////////


  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private data : DataService,
    private msg : MsgBoxService,
    private _settingsService: SettingsService
    ){
        this.settings = this._settingsService.settings;
    } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}


  async ngOnInit() {

    const today = new Date();
    this.from = today.toISOString().split('T')[0];
    this.to = today.toISOString().split('T')[0];

    this.getTotalsByDates(this.from, this.to);
    this.getStorageTotalsByDates(this.from, this.to);

    await this.getParkingSummary()
    await this.getStorageSummary()


    /////////////////////////////////////

    //--- Vertical Bar Chart --- 
    //this.verticalBarChartData.labels = ['2007', '2008', '2009', '2010', '2011', '2012'];
    this.verticalBarChartData.labels = this.parkingSummary.map((item: IParkingSummary) => item.monthName);
    this.verticalBarChartData.datasets = [
      {
        data: this.parkingSummary.map((item: IParkingSummary) => item.checkedIn),
        label: 'Checked In',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        hoverBackgroundColor: this.settings.colors.info
      },
      {
        data: this.parkingSummary.map((item: IParkingSummary) => item.checkedOut),
        label: 'Checked Out',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        hoverBackgroundColor: this.settings.colors.danger
      }
    ];
    this.verticalBarChartOptions = {
      responsive: true,
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 5,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.6)
        }
      }
    };

    //--- Horizontal  Bar Chart --- 
    this.horizontalBarChartData.labels = ['2007', '2008', '2009', '2010', '2011', '2012'];
    this.horizontalBarChartData.datasets = [
      {
        data: [59, 80, 72, 56, 55, 40],
        label: 'Series A',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        hoverBackgroundColor: this.settings.colors.danger
      },
      {
        data: [48, 40, 19, 75, 27, 80],
        label: 'Series B',
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        hoverBackgroundColor: this.settings.colors.info
      }
    ];
    this.horizontalBarChartOptions = {
      responsive: true,
      indexAxis: 'y',
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 10,
            font: {
              size: 14
            }
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.6)
        }
      }
    };

    //--- Line Chart ---
    this.lineChartData.labels = this.storageSummary.map((item: IParkingSummary) => item.monthName);
    this.lineChartData.datasets = [
      {
        data: this.storageSummary.map((item: IParkingSummary) => item.checkedIn),
        label: 'Checked In',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.info, 0.5),
        borderColor: this.settings.colors.info,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.info,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.info
      },
      {
        data: this.storageSummary.map((item: IParkingSummary) => item.checkedOut),
        label: 'Checked Out',
        fill: true,
        tension: 0.5,
        borderWidth: 2,
        backgroundColor: this._settingsService.rgba(this.settings.colors.danger, 0.5),
        borderColor: this.settings.colors.danger,
        pointBorderColor: this.settings.colors.default,
        pointHoverBorderColor: this.settings.colors.danger,
        pointHoverBackgroundColor: this.settings.colors.default,
        hoverBackgroundColor: this.settings.colors.danger
      },
      // {
      //   data: this.summary.map((item: IParkingSummary) => item.checkedIn),
      //   label: 'Series C',
      //   fill: true,
      //   tension: 0.5,
      //   borderWidth: 2,
      //   backgroundColor: this._settingsService.rgba(this.settings.colors.primary, 0.5),
      //   borderColor: this.settings.colors.primary,
      //   pointBorderColor: this.settings.colors.default,
      //   pointHoverBorderColor: this.settings.colors.primary,
      //   pointHoverBackgroundColor: this.settings.colors.default,
      //   hoverBackgroundColor: this.settings.colors.primary
      // }
    ];
    this.lineChartOptions = {
      scales: {
        y: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7),
            stepSize: 5
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        },
        x: {
          display: true,
          beginAtZero: true,
          ticks: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.7)
          },
          grid: {
            display: true,
            color: this._settingsService.rgba(this.settings.colors.gray, 0.1)
          }
        }
      },
      plugins: {
        legend: {
          display: true,
          labels: {
            color: this._settingsService.rgba(this.settings.colors.gray, 0.9),
          }
        },
        tooltip: {
          enabled: true,
          backgroundColor: this._settingsService.rgba(this.settings.colors.main, 0.7)
        }
      }
    };




    /////////////////////////////////////
  }

  public chartClicked(e: any): void {
    //console.log(e);
  }

  public chartHovered(e: any): void {
    //console.log(e);
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


  storageCheckedIn : any = 0
  async getStorageTotalsByDates(from : Date | string | null, to : Date | string | null) {

    this.storageCheckedIn = 0

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

    await this.http.post<IStorageTotalsByDates>(API_URL+'/storage_reports/get_totals_by_dates', args, options)
        .toPromise()
        .then(
          data => {
            this.storageCheckedIn = data!.checkedIn
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


  parkingSummary : IParkingSummary[] = []

  async getParkingSummary() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingSummary = []
    await this.http.get<IParkingSummary[]>(API_URL+'/parkings/get_parking_summary?year=2025', options)
        .toPromise()
        .then(
          data => {
            this.parkingSummary = data!
            console.log(this.parkingSummary)
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

  storageSummary : IStorageSummary[] = []

  async getStorageSummary() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.storageSummary = []
    await this.http.get<IStorageSummary[]>(API_URL+'/storages/get_storage_summary?year=2025', options)
        .toPromise()
        .then(
          data => {
            this.storageSummary = data!
            console.log(this.storageSummary)
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

interface IStorageTotalsByDates {
  from : string;
  to : string;
  checkedIn : string
}

interface IParkingSummary{
  month : number
  checkedIn : number
  checkedOut : number
  monthName : string
}

interface IStorageSummary{
  month : number
  checkedIn : number
  checkedOut : number
  monthName : string
}

