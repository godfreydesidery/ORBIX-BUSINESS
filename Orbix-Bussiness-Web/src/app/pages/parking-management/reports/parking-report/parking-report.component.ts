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
  selector: 'az-parking-report',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './parking-report.component.html',
  styleUrl: './parking-report.component.scss'
})
export class ParkingReportComponent {
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

      this.getRegistationByDate(this.from, this.to);
      this.getBranchUserNames();
    }

    registrations : IRegistration[] = []
    parkingReports : IParkingReport[] = []

  async getRegistationByDate(from : Date | string | null, to : Date | string | null) {
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

    this.parkingReports = []
    

    await this.http.post<IParkingReport[]>(API_URL+'/parking_reports/get_parking_report?nickname=' + this.nickname, args, options)
        .toPromise()
        .then(
          data => {

            this.parkingReports = data!

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

  print = async () => {
    this.documentHeader = await this.data.getDocumentHeader();
    const title = 'Vehicle Registration Report';
    const fromTo = 'From: ' +this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;
  
    const report: any[] = [];
  
    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Chassis No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Vehicle', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Key Status', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Date Registered', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Registered By', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);
  
    // Add rows dynamically
    this.parkingReports.forEach((element) => {
      // total += parseFloat(element.amount) || 0;
      // discount += parseFloat(element.discount) || 0;

    
  
      report.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.chasisNo || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentTypeName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.keyStatus || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedInAt.substring(0, 10), fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.createdBy || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
      ]);
    });
  
    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'potrait', // landscape for Landscape
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
            widths: [25, 100, 100, 60, 80, 90],
            body: report,
          },
        },
      ],
    };
  
    pdfMake.createPdf(docDefinition).print();
  };


  printParkingReport = async () => {
    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Vehicle Parking Report';
    const fromTo = 'From: ' +this.from?.toString() + ' To: ' + this.to?.toString();
    let total: number = 0;
    let discount: number = 0;
  
    const report: any[] = [];
  
    // Add header row
    report.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Category', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Vehicle Type', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Owner Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Phone', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Card No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Chassis No', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Sub T1 Form', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Device Status', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Price', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Start Date', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'End Date', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      // { text: 'Remarks', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);

    
  
    // Add rows dynamically
    this.parkingReports.forEach((element) => {
      // total += parseFloat(element.amount) || 0;
      // discount += parseFloat(element.discount) || 0;

    
  
      report.push([
        { text: element.sn || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentCategory || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.vehicleEquipmentTypeName || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.ownerFirstName + ' ' + element.ownerLastName || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.ownerPhoneNo || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.cardNo || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.chasisNo || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.tformNumber || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.deviceStatus || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.billingAmount || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedInAt.substring(0, 10) || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.checkedOutAt.substring(0, 10) || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
        // { text: element.status || '', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: false },
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
            widths: [25, 50, 80, 90, 80, 60, 50, 50, 30, 50, 50, 50],
            body: report,
          },
        },
      ],
    };
  
    pdfMake.createPdf(docDefinition).print();
  };



}

export interface IRegistration{
  sn : string;
  chassisNo : string;
  vehicleType : string;
  keyStatus : string;
  registeredDate : string;
  registeredBy : string;
}

export interface IParkingReport{
  sn : string;
  vehicleEquipmentCategory : string,
  vehicleEquipmentTypeName : string,
  ownerFirstName : string,
  ownerLastName : string,
  ownerPhoneNo : string,
  cardNo : string,
  chasisNo : string,
  tformNumber : string,
  deviceStatus : string,
  billingAmount : number
  checkedInAt : string,
  checkedOutAt : string,
  status : string,
  keyStatus : string,
  createdBy : string
}


