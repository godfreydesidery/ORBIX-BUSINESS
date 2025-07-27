import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { IWeighBillReceivable } from 'src/app/domain/bill-receivable';
import { IBillView } from 'src/app/domain/bill-view';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem } from 'src/app/domain/maintenance';
import * as pdfMake from 'pdfmake/build/pdfmake';
import { IWeigh } from 'src/app/domain/weigh';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-weigh-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './weigh-billing.component.html',
  styleUrl: './weigh-billing.component.scss'
})
export class WeighBillingComponent {
page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  startedAt: Date | null
  endedAt: Date | null
  billingType: string
  qty: number
  price: number
  discount: number

  weighId: any

  autoBilling: any = 1

  weighAmount: number

  billingStartAt: string = ''
  ////////////////////////////////////////////

  vehicleEquipmentTypeName: string = ''


  // Weigh attributes
  // weighId : any = null
  weighNo: string = ''
  weighBillReceivableId: any = null
  weighBillReceivableDescription: string = ''
  weighBillReceivableStartingDate: Date | null
  weighBillReceivableEndingDate: Date | null
  weighBillReceivablePrice: number = 0
  weighBillReceivableQty: number = 0
  weighBillReceivableDiscount: number = 0
  weighBillReceivableAmount: number = 0
  weighBillReceivableStatus: string = ''

  // Service attributes
  serviceBillReceivableId: any = null
  serviceBillReceivableDate: Date | null
  serviceBillReceivableDescription: string = ''
  serviceBillReceivablePrice: number = 0
  serviceBillReceivableQty: number = 0
  serviceBillReceivableDiscount: number = 0
  serviceBillReceivableStatus: string = ''
  serviceBillReceivableAmount: number = 0

  //Weigh and bills Collections attributes
  weighBillReceivables: IWeighBillReceivable[] = []
  // serviceBillReceivables : IServiceBillReceivable[] = []
  // billReceivables : IBillReceivable[] = []
  documentHeader: any;
  invoice: any;

  cash: number = 0

  mpesa: number = 0
  mpesaRefNo: string = ''


  /////////////////////////////////////////////


  // Owner information
  ownerFirstName: string = ''
  ownerMiddleName: string = ''
  ownerLastName: string = ''
  ownerCompanyName: string = ''
  ownerIdNo: string = ''
  ownerIdType: string = ''
  ownerPhoneNo: string = ''
  ownerEmail: string = ''
  ownerAddress: string = ''

  color: string = ''

  validUntilDate: Date | null = new Date()

  comments: string = ''

  cardNo: string = ''

  goodCategory: string = ''

  billingAmount: number = 0
  //image: Byte[]

  status: string = "PENDING"



  startBillingAt: Date | null


  goodName: string = ''
  goodTypeId: any = ''
  goodTypeName: string = ''
  branchId: any = ''
  companyId: any = ''

  warehouseName: string = ''

  initialQty: number = 0
  currentQty: number = 0





  ////////////////////////////////////////




  /**Collections */
  weighs: IWeigh[] = []
  // weighBillReceivables : IWeighBillReceivable[] = []

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private msg: MsgBoxService,
    private data: DataService
  ) { }


  ngOnInit() {
    this.getAllCheckedInWeighs()
  }

  async getAllCheckedInWeighs() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.weighs = []

    await this.http.get<IWeigh[]>(API_URL + '/weighs/recent', options)
      .toPromise()
      .then(
        data => {
          data?.reverse()
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.weighs.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  async getWeighBillReceivables(weighId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.weighBillReceivables = []

    await this.http.get<IWeighBillReceivable[]>(API_URL + '/weighs/get_weigh_bill_receivables?weigh_id=' + weighId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.weighBillReceivables.push(element)
            sn = sn + 1
          })
          this.qty = 0
          this.price = 0
          this.discount = 0
          this.weighAmount = 0
          this.getWeigh(weighId)
          console.log(data)
        }
      )
  }

  async getWeigh(weighId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWeigh>(API_URL + '/weighs/get?id=' + weighId, options)
      .toPromise()
      .then(
        data => {
          this.weighId = data!.id
          this.billingType = data!.billingType
          this.price = data!.billingAmount
        }
      )
  }

  async createWeighBillReceivable() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var weighBillReceivable = {
      startedAt: this.startedAt,
      endedAt: this.endedAt,
      billingType: this.billingType,
      qty: this.qty,
      price: this.price,
      discount: this.discount,
      autoBilling: this.autoBilling,
      weighId: this.weighId
    }

    await this.http.post<IWeighBillReceivable>(API_URL + '/weighs/create_weigh_bill_receivable', weighBillReceivable, options)
      .toPromise()
      .then(
        data => {
          this.getWeighBillReceivables(this.weighId)
          console.log(data)
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Error')
          console.log(error)
        }
      )


  }

  clearWeighBill() {
    this.startedAt = null
    this.endedAt = null
    this.qty = 0
    this.price = 0
    this.discount = 0
    this.weighId = null
    this.autoBilling = 1
    this.weighAmount = 0
  }

  doAutoBilling() {
    this.clearWeighBill()
  }

  doCustomBilling() {
    this.autoBilling = 0
  }

  refreshWeighAmounts() {
    this.weighAmount = (this.price * this.qty) - this.discount
  }


  async weighBilling(weighId: any) {

    localStorage.setItem('weigh-id', '');
    localStorage.setItem('weigh-id', weighId);

    await this.router.navigate(['app/accounts-and-finance/weight-billing'], {
      queryParams: { weigh_id: weighId }
    });

  }

  async get(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWeigh>(API_URL + '/weighs/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.startBillingAt = null
          this.showWeighData(data!)
          console.log(data)
        }
      )
  }

  showWeighData(data: IWeigh) {

    this.weighId = data?.id
    this.weighNo = data?.no

    this.ownerFirstName = data?.ownerFirstName
    this.ownerMiddleName = data?.ownerMiddleName
    this.ownerLastName = data?.ownerLastName
    this.ownerCompanyName = data?.ownerCompanyName
    this.ownerIdNo = data?.ownerIdNo
    this.ownerIdType = data?.ownerIdType
    this.ownerPhoneNo = data?.ownerPhoneNo
    this.ownerEmail = data?.ownerEmail
    this.ownerAddress = data?.ownerAddress

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount
    this.billingStartAt = data?.billingStartAt

    this.initialQty = data?.initialQty
    this.currentQty = data?.currentQty

    this.goodName = data!.goodName

    this.goodTypeName = data!.goodTypeName
    this.warehouseName = data!.warehouseName
    this.billingType = data!.billingType
    this.status = data!.status
    this.validUntilDate = null
    this.comments = data!.comments// check this

  }

  clearWeighData() {
    this.weighId = null
    this.weighNo = ''
    this.ownerFirstName = ''
    this.ownerMiddleName = ''
    this.ownerLastName = ''
    this.ownerCompanyName = ''
    this.ownerIdNo = ''
    this.ownerIdType = ''
    this.ownerPhoneNo = ''
    this.ownerEmail = ''
    this.ownerAddress = ''

    this.billingType = ''
    this.billingAmount = 0
    this.billingStartAt = ''
    this.goodTypeName = ''

    this.initialQty = 0
    this.currentQty = 0

    this.goodName = ''

    this.goodCategory = ''

    this.warehouseName = ''
    this.billingType = ''

    this.color = ''
    this.comments = ''
  }




  /////////////////////////



  originalQty: number = 0
  availableQty: number = 0
  releasedQty: number = 0
  availableForRelease: number = 0

  qtyToRelease: number = 0

  currentWeighId: any = null

  async getWeighGoodReleaseDetail(weighId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.originalQty = 0
    this.availableQty = 0
    this.releasedQty = 0
    this.availableForRelease = 0

    this.currentWeighId = null

    this.qtyToRelease = 0

    await this.http.get<IWeighGoodReleaseDetail>(API_URL + '/weigh_good_releases/get_weigh_good_release_detail?weigh_id=' + weighId, options)
      .toPromise()
      .then(
        data => {

          this.currentWeighId = weighId

          this.originalQty = data!.initialQty
          this.availableQty = data!.currentQty
          this.releasedQty = data!.releasedQty
          this.availableForRelease = data!.availableForRelease

          this.qtyToRelease = 0

          console.log(data)
        }
      )
  }


  async createWeighGoodRelease() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var weighGoodRelease = {
      weighId: this.currentWeighId,
      qty: this.qtyToRelease,

    }

    await this.http.post<IWeighGoodReleaseDetail>(API_URL + '/weigh_good_releases/create_weigh_good_release', weighGoodRelease, options)
      .toPromise()
      .then(
        data => {
          //this.getWeighBillReceivables(this.weighId)
          this.msg.showSuccessMessage('Success')
          console.log(data)
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Error')
          console.log(error)
        }
      )


  }


  billPaid: number = 0
  billGenerated: number = 0
  billUngenerated: number = 0
  billUnpaid: number = 0

  async getBillView(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.billPaid = 0
    this.billGenerated = 0
    this.billUngenerated = 0
    this.billUnpaid = 0

    await this.http.get<IBillView>(API_URL + '/weigh_bill_receivables/get_bill_view?weigh_id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.billPaid = data!.billPaid
          this.billGenerated = data!.billGenerated
          this.billUngenerated = data!.billUngenerated
          this.billUnpaid = data!.billUnpaid
          console.log(data)
        }
      )
  }

  async checkOut(id: any): Promise<void> {


    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var weigh = {
      id: id
    }

    await this.http.post<IWeigh>(API_URL + '/weighs/check_out', weigh, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.msg.showSuccessMessage('Checked out Successifully')

          this.printGatePassRcpt(data!.serviceBillItems, '', 0);
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
      this.getAllCheckedInWeighs()
  }

  lastBillingDate: string = ''

  async getLastBillingDate(weighId: any) {
    // this.lastBillingDate = ''
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IModel>(API_URL + '/weighs/get_last_weigh_bill_date?id=' + weighId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          // this.lastBillingDate = data!.stringData
          this.lastBillingDate = data!.stringData
        }
      )
      .catch(
        error => {
          console.log(error)
          // this.lastBillingDate = ''
          // this.msg.showErrorMessage(error, 'Error')
          this.lastBillingDate = ''
        }
      )
  }

  printGatePassRcpt = async (billItems: IServiceBillItem[], receiptNo: string, cash: number) => {

    await this.get(this.weighId)
    await this.getLastBillingDate(this.weighId)

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title = 'Gate Pass'
    var total: number = 0
    var discount: number = 0
    var tax: number = 0

    // var address : any = await this.data.getReceiptHeader(receiptNo)
    var address: any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(receiptNo)

    var receipt = [
      [
        { text: 'SN', fontSize: 8, bold: true },
        { text: 'Item', fontSize: 8, bold: true },
        { text: 'Qty', fontSize: 8, bold: true },
        { text: 'Amount', fontSize: 8, bold: true },
      ]
    ]

    var sn = 0

    billItems.forEach((element) => {
      total = total + (+element.amount)
      sn = sn + 1
      var item = [
        { text: sn.toString(), fontSize: 8, bold: false },
        { text: element.item, fontSize: 8, bold: false },
        { text: element.qty.toString(), fontSize: 8, bold: false },
        { text: (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 8, alignment: 'right', bold: false },
      ]
      receipt.push(item)
    })
    var detailSummary = [
      { text: ' ', fontSize: 8, bold: false },
      { text: 'Total', fontSize: 9, bold: true },
      { text: ' ', fontSize: 8, bold: false },
      { text: total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
    ]
    receipt.push(detailSummary)


    const docDefinition = {
      header: '',

      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
      content: [
        {
          layout: 'noBorders',
          table: address
        },



        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [210],
            body: [
              [{ text: '==============================' }],
            ]
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [200],
            body: [
              [{ text: 'Gate Pass', alignment: 'center', fontSize: 9, bold: true }],
              [{ text: 'Vehicle Name: ' + this.vehicleEquipmentTypeName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: '________________________________' }],
              [{ text: 'Payment Details', alignment: 'center', fontSize: 9, bold: true }],
              [{ text: ' ', alignment: 'center', fontSize: 9, bold: true }],
            ]
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 1,
            widths: [15, 100, 15, 50],
            body: receipt
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [200],
            body: [
              [{ text: ' ' }],
              [{ text: 'Cashier Comments', alignment: 'left', fontSize: 9, bold: true }],
              [{ text: this.comments, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: ' ' }],
              [{ text: ' ' }],
              [{ text: 'Issued At: ' + new Date().toString(), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Checkout At: ' + new Date().toString(), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Valid Until: ' + this.lastBillingDate, alignment: 'left', fontSize: 9, bold: true }],
              [{ text: ' ' }],
              [{ text: 'Gate Pass issued By: ' + localStorage.getItem('user-name'), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: ' ' }],
              [{ text: 'Signature: ......................' }],
            ]
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [210],
            body: [
              [{ text: '==============================' }],
              [{ text: 'Developed By @Davaghana', fontSize: 10, bold: true, alignment: 'center' }],
              [{ text: '***End of Document***', fontSize: 9, alignment: 'center' }]
            ]
          }
        },
      ],
      pageMargins: 10,
    }
    const win = window.open('', "tempWinForPdf")
    pdfMake.createPdf(docDefinition).print({}, win)
    //win!.onfocus = function () { setTimeout(function () { win!.close(); }, 10000); } //set to 10 seconds
  }

}


interface IWeighGoodReleaseDetail {
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IWeighGoodRelease {
  id: any
  no: string
  qty: number
  status: string
  weighId: any
  releaseDate: string
  clientName: string
  clientAddress: string
  clientPhoneNo: string
  goodName: string
  unitPrice: string
  total: string
}

interface IModel {
  stringData: string
}


