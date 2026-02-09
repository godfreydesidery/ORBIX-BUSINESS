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
  selector: 'az-vehicle-equipment-removed-report',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule,
    TimePipe
  ],
  templateUrl: './vehicle-equipment-removed-report.component.html',
  styleUrl: './vehicle-equipment-removed-report.component.scss'
})
export class VehicleEquipmentRemovedReportComponent {
documentHeader!: any;

  from: Date | string | null = null;
  to: Date | string | null = null;

  nickname = '';
  status = '--All--';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
  ) {}

  ngOnInit() {}

  vehicleEquipmentRemovedReports: IVehicleEquipmentRemoved[] = [];

  async getRegistationByDate(from: Date | string | null, to: Date | string | null) {
    if (from == null || to == null) {
      const today = new Date();
      from = today;
      to = today;
      this.from = today.toISOString().split('T')[0];
      this.to = today.toISOString().split('T')[0];
    }

    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token),
    };

    const args = {
      from: from,
      to: to,
    };

    this.vehicleEquipmentRemovedReports = [];

    await this.http
      .post<IVehicleEquipmentRemoved[]>(
        API_URL + '/parking_reports/get_vehicle_equipment_removed_report',
        args,
        options
      )
      .toPromise()
      .then((data) => {
        this.vehicleEquipmentRemovedReports = data || [];
        console.log(data);
      })
      .catch((error) => {
        this.msg.showErrorMessage(error, 'Error');
        console.log(error);
      });

    return 0;
  }

  printStorageReport = async () => {
    if (this.vehicleEquipmentRemovedReports.length === 0) {
      this.msg.showErrorMessage3('No data to export');
      return;
    }

    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Vehicle Equipment Archived Report';
    const fromTo = 'From: ' + this.from?.toString() + ' To: ' + this.to?.toString();

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

    report.push([
      { text: 'SN', fontSize: 8, bold: true },
      { text: 'Owner Name', fontSize: 8, bold: true },
      { text: 'Phone No', fontSize: 8, bold: true },
      { text: 'Vehicle Name', fontSize: 8, bold: true },
      { text: 'Price', fontSize: 8, bold: true },
      { text: 'Chasis/Reg No', fontSize: 8, bold: true },
      { text: 'Reason', fontSize: 8, bold: true },
      { text: 'Date Time', fontSize: 8, bold: true },
      { text: 'Registered By', fontSize: 8, bold: true },
      { text: 'Archived By', fontSize: 8, bold: true },
    ]);

    this.vehicleEquipmentRemovedReports.forEach((element) => {
      report.push([
        { text: element.sn || '', fontSize: 8 },
        { text: element.ownerName || '', fontSize: 8 },
        { text: element.phoneNo || '', fontSize: 8 },
        { text: element.vehicleName || '', fontSize: 8 },
        { text: element.price || '', fontSize: 8 },
        { text: element.regNo || '', fontSize: 8 },
        { text: element.reason || '', fontSize: 8 },
        { text: element.dateTime || '', fontSize: 8 },
        { text: element.registeredBy || '', fontSize: 8 },
        { text: element.removedBy || '', fontSize: 8 },
      ]);
    });

    const docDefinition: any = {
      header: '',
      pageOrientation: 'landscape',
      footer: (currentPage: any, pageCount: any) => ({
        text: `${currentPage} of ${pageCount}`,
        alignment: 'center',
        fontSize: 8,
      }),
      content: [
        { columns: [this.documentHeader] },
        { text: ' ' },
        { text: title, fontSize: 14, bold: true, margin: [0, 10, 0, 10] },
        { text: fromTo, fontSize: 10, bold: true, margin: [0, 10, 0, 10] },
        {
          table: {
            widths: [30, 90, 60, 100, 50, 60, 100, 90, 90, 90],
            body: report,
          },
        },
      ],
    };

    pdfMake.createPdf(docDefinition).print();
  };

  exportToExcel(): void {
    if (this.vehicleEquipmentRemovedReports.length === 0) {
      this.msg.showErrorMessage3('No data to export');
      return;
    }

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(
      this.vehicleEquipmentRemovedReports.map((item) => ({
        'S/N': item.sn,
        'Owner Name': item.ownerName,
        'Phone No': item.phoneNo,
        'Vehicle Name': item.vehicleName,
        'Price': item.price,
        'Chasis/Reg No': item.regNo,
        'Reason': item.reason,
        'Date Time': item.dateTime,
        'Registered By': item.registeredBy,
        'Archived By': item.removedBy,
      }))
    );

    const workbook: XLSX.WorkBook = {
      Sheets: { Report: worksheet },
      SheetNames: ['Report'],
    };

    const excelBuffer: any = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
    });

    const fileName = 'Vehicle Equipment Archived Report ' + this.from + ' - ' + this.to + '.xlsx';
    this.saveAsExcelFile(excelBuffer, fileName);
  }

  private saveAsExcelFile(buffer: any, fileName: string): void {
    const data: Blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
    });
    FileSaver.saveAs(data, fileName);
  }
}

export interface IVehicleEquipmentRemoved {
  sn: string;
  ownerName: string;
  phoneNo: string;
  vehicleName: string;
  price: string;
  regNo: string;
  reason: string;
  dateTime: string;
  registeredBy: string;
  removedBy: string;
}