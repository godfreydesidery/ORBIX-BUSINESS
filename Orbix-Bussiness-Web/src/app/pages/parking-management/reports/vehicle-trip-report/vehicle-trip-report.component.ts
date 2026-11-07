import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IParkingTrip, tripDateTime, tripDuration, tripReasonLabel, tripStatusLabel } from 'src/app/domain/parking-trip';

import * as XLSX from 'xlsx';
import * as FileSaver from 'file-saver';

// pdfmake and its fonts are loaded globally (angular.json scripts); use that instance instead of bundling a second copy
declare var pdfMake: any;

const API_URL = environment.apiUrl;

/**
 * Log of vehicles sent out of the yard temporarily and back: trips sent out in the period,
 * plus trips still outside that started earlier.
 */
@Component({
  selector: 'az-vehicle-trip-report',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './vehicle-trip-report.component.html',
  styleUrl: './vehicle-trip-report.component.scss'
})
export class VehicleTripReportComponent {
  trackById = trackById
  tripReasonLabel = tripReasonLabel
  tripStatusLabel = tripStatusLabel
  tripDuration = tripDuration
  tripDateTime = tripDateTime

  from: string = ''
  to: string = ''
  statusFilter: string = 'ALL'   // ALL, OUT, OVERDUE, RETURNED, NOT_RETURNED

  trips: IParkingTrip[] = []
  loaded: boolean = false

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService,
    private data: DataService
  ) {}

  ngOnInit() {
    const today = this.today()
    this.from = today
    this.to = today
  }

  private today(): string {
    const d = new Date()
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
    return d.toISOString().substring(0, 10)
  }

  get shownTrips(): IParkingTrip[] {
    switch (this.statusFilter) {
      case 'ALL': return this.trips
      case 'OVERDUE': return this.trips.filter(t => t.overdue)
      default: return this.trips.filter(t => t.status === this.statusFilter)
    }
  }

  async runReport() {
    if (!this.from || !this.to) {
      this.msg.showErrorMessage3('Select the period')
      return
    }
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token),
    }
    await this.http.post<IParkingTrip[]>(API_URL + '/parking_reports/get_trip_report', { from: this.from, to: this.to }, options)
      .toPromise()
      .then(data => {
        this.trips = data || []
        this.loaded = true
      })
      .catch(error => {
        this.msg.showErrorMessage(error, 'Error')
      })
  }

  private rows() {
    return this.shownTrips.map((t, i) => ({
      sn: i + 1,
      passNo: t.no,
      chasisNo: t.chasisNo,
      vehicle: t.vehicleEquipmentTypeName,
      owner: t.ownerName,
      driver: t.driverName,
      driverPhone: t.driverPhoneNo,
      driverId: t.driverIdType + ' ' + t.driverIdNo,
      reason: tripReasonLabel(t.reason) + (t.reasonDetails ? ' - ' + t.reasonDetails : ''),
      documentsLeft: t.documentsLeft ? 'Yes' : 'No',
      documentsReturned: t.documentsLeft ? (t.documentsReturned ? 'Yes' : 'No') : '',
      sentOutAt: tripDateTime(t.sentOutAt),
      sentOutBy: t.sentOutBy,
      expectedReturnAt: tripDateTime(t.expectedReturnAt),
      receivedAt: tripDateTime(t.receivedAt),
      receivedBy: t.receivedBy,
      timeOutside: tripDuration(t.minutesOut),
      status: tripStatusLabel(t.status) + (t.overdue ? ' (Overdue)' : ''),
      notes: t.returnNotes,
    }))
  }

  printReport = async () => {
    const rows = this.rows()
    if (rows.length === 0) {
      this.msg.showErrorMessage3('No data to export')
      return
    }
    const documentHeader = await this.data.getDocumentHeaderLandScape()
    const head = ['SN', 'Pass No', 'Chasis No', 'Vehicle', 'Driver', 'Driver ID', 'Reason', 'Docs Left', 'Time Out', 'Out By', 'Expected Back', 'Back At', 'Received By', 'Time Outside', 'Status']
    const body: any[] = [head.map(h => ({ text: h, fontSize: 7, bold: true }))]
    rows.forEach(r => {
      body.push([
        r.sn.toString(), r.passNo, r.chasisNo, r.vehicle, r.driver + '\n' + r.driverPhone, r.driverId, r.reason,
        r.documentsLeft, r.sentOutAt, r.sentOutBy, r.expectedReturnAt, r.receivedAt, r.receivedBy, r.timeOutside, r.status
      ].map(v => ({ text: v ?? '', fontSize: 7 })))
    })

    const docDefinition: any = {
      header: '',
      pageOrientation: 'landscape',
      footer: (currentPage: any, pageCount: any) => ({ text: `${currentPage} of ${pageCount}`, alignment: 'center', fontSize: 8 }),
      content: [
        { columns: [documentHeader] },
        { text: ' ' },
        { text: 'Vehicle Trips Out of the Yard', fontSize: 14, bold: true, margin: [0, 10, 0, 5] },
        { text: 'From: ' + this.from + '  To: ' + this.to + (this.statusFilter !== 'ALL' ? '  (' + this.filterLabel() + ')' : ''), fontSize: 10, bold: true, margin: [0, 0, 0, 10] },
        { table: { headerRows: 1, widths: [18, 45, 55, 50, 60, 55, 60, 25, 45, 35, 45, 45, 35, 35, 45], body: body } },
      ],
    }
    pdfMake.createPdf(docDefinition).print()
  }

  exportToExcel(): void {
    const rows = this.rows()
    if (rows.length === 0) {
      this.msg.showErrorMessage3('No data to export')
      return
    }
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(rows.map(r => ({
      'S/N': r.sn,
      'Pass No': r.passNo,
      'Chasis No': r.chasisNo,
      'Vehicle': r.vehicle,
      'Owner': r.owner,
      'Driver': r.driver,
      'Driver Phone': r.driverPhone,
      'Driver ID': r.driverId,
      'Reason': r.reason,
      'Documents Left': r.documentsLeft,
      'Documents Returned': r.documentsReturned,
      'Time Out': r.sentOutAt,
      'Sent Out By': r.sentOutBy,
      'Expected Back': r.expectedReturnAt,
      'Back At': r.receivedAt,
      'Received By': r.receivedBy,
      'Time Outside': r.timeOutside,
      'Status': r.status,
      'Return Notes': r.notes,
    })))
    const workbook: XLSX.WorkBook = { Sheets: { Report: worksheet }, SheetNames: ['Report'] }
    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8' })
    FileSaver.saveAs(blob, 'Vehicle Trips Report ' + this.from + ' - ' + this.to + '.xlsx')
  }

  private filterLabel(): string {
    switch (this.statusFilter) {
      case 'OUT': return 'Still outside'
      case 'OVERDUE': return 'Overdue'
      case 'RETURNED': return 'Returned'
      case 'NOT_RETURNED': return 'Not returned'
      default: return 'All'
    }
  }
}
