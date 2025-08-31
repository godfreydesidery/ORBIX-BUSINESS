import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IBillReceivable, IMachineServiceBillReceivable, IServiceBillReceivable } from 'src/app/domain/bill-receivable';
import { environment } from 'src/environments/environment';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem, IWeigh } from 'src/app/domain/weigh';
import { ICustomer } from 'src/app/domain/customer';
import { IMachine } from 'src/app/domain/machine';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl; 
@Component({
  selector: 'az-machine-service-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './machine-service-billing.component.html',
  styleUrl: './machine-service-billing.component.scss'
})
export class MachineServiceBillingComponent {
// Weigh attributes
  machineId: any = null
  machineNo: string = ''
  machineServiceBillReceivableId: any = null
  machineServiceBillReceivableDescription: string = ''
  machineServiceBillReceivableStartingDate: Date | null
  machineServiceBillReceivableEndingDate: Date | null
  machineServiceBillReceivablePrice: number = 0
  machineServiceBillReceivableQty: number = 0
  machineServiceBillReceivableNoOfDays: number = 0
  machineServiceBillReceivableDiscount: number = 0
  machineServiceBillReceivableAmount: number = 0
  machineServiceBillReceivableStatus: string = ''

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
  machineServiceBillReceivables: IMachineServiceBillReceivable[] = []
  serviceBillReceivables: IServiceBillReceivable[] = []
  billReceivables: IBillReceivable[] = []
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

  validUntilDate: Date | null = new Date()

  comments: string = ''

  cardNo: string = ''

  billingType: string = ''
  billingAmount: number = 0
  //image: Byte[]

  status: string = "PENDING"

  startBillingAt: Date | null

  vehicleEquipmentTypeId: any = ''
  vehicleEquipmentTypeName: string = ''
  branchId: any = ''
  companyId: any = ''

  warehouseId: any = null
  warehouseName: string = ''

  goodTypeName: string = ''
  goodTypeId: any = null


  ////////////////////////////////////////



  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private data: DataService,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService
  ) { } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.machineId = params['machine_id']
    })
    this.getMachineServiceBillReceivables(this.machineId)
  }


  async getMachineServiceBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IMachineServiceBillReceivable>(API_URL + '/machine_service_bill_receivables/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.machineServiceBillReceivableId = data!.id
          this.machineServiceBillReceivableDescription = data!.description
          this.machineServiceBillReceivableStartingDate = data!.startedAt
          this.machineServiceBillReceivableEndingDate = data!.endedAt
          this.machineServiceBillReceivablePrice = data!.price
          this.machineServiceBillReceivableQty = data!.qty
          this.machineServiceBillReceivableNoOfDays = data!.noOfDays
          this.machineServiceBillReceivableDiscount = data!.discount
          this.machineServiceBillReceivableAmount = data!.amount
          this.machineServiceBillReceivableStatus = data!.payStatus
          this.billingType = data!.billingType

          console.log(data)


        }
      )

  }


  qty: number = 0
  

  async getServiceBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IServiceBillReceivable>(API_URL + '/weigh_service_bill_receivables/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.serviceBillReceivableId = data!.id
          this.serviceBillReceivableDate = data!.serviceDate
          this.serviceBillReceivableDescription = data!.description
          this.serviceBillReceivablePrice = data!.price
          this.serviceBillReceivableQty = data!.qty
          this.serviceBillReceivableDiscount = data!.discount
          console.log(data)
        }
      )

  }

  async getMachineServiceBillReceivables(machineId: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.machineServiceBillReceivables = []

    await this.http.get<IMachineServiceBillReceivable[]>(API_URL + '/machine_service_bill_receivables/get_all_by_machine?machine_id=' + machineId, options)
      .toPromise()
      .then(
        data => {

          data?.forEach(element => {
            this.machineServiceBillReceivables.push(element)
          })
          var sn = 1
          this.machineServiceBillReceivables.reverse().forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  

  totalBillReceivable: number = 0
  balance: number = 0
  tender: number = 0
  async getBillReceivables(machineId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.billReceivables = []
    this.totalBillReceivable = 0

    await this.http.get<IBillReceivable[]>(API_URL + '/bill_receivables/get_all_by_machine?machine_id=' + machineId, options)
      .toPromise()
      .then(
        data => {
          data?.forEach(element => {
            if (element.payStatus == 'UNPAID') {
              this.billReceivables.push(element)
              this.totalBillReceivable = this.totalBillReceivable + (+element.due)
            }
          })
          var sn = 1
          this.billReceivables.reverse().forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        }
      )

    this.refreshAmounts()

  }

  refresh() {
    this.getMachineServiceBillReceivables(this.machineId)
  }

  getUnpaidBills() { }

  confirmBillPayment() { }

  async refreshBillReceivables() {
    this.billReceivables = []
    await this.getBillReceivables(this.machineId)
  }

  refreshAmounts() {
    this.balance = this.tender - this.totalBillReceivable
  }

  generateReceipt() { }

  onInput(event: Event) {
    const input = event.target as HTMLInputElement;
    if (parseFloat(input.value) < 0) {
      input.value = input.value.replace('-', '');
    }
  }

  toPrintReceipt: boolean = false

  receiptData: IBillReceivable[] = []

  billReceivableCollections: IBillReceivableCollection[] = []

  payCode = 'CASH'
  payRefNo = ''
  async confirmBillsPayment() {

    this.toPrintReceipt = false

    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to confirm this payment?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.receiptData = this.billReceivables


    //this.spinner.show()
    await this.http.post<IBillReceivable>(API_URL + '/bill_receivables/confirm_bills_payment?total_amount=' + this.totalBillReceivable + '&pay_code=' + this.payCode + '&pay_ref_no=' + this.payRefNo, this.billReceivables, options)
      //.pipe(finalize(() => this.spinner.hide()))
      .toPromise()
      .then(
        data => {
          console.log(data)
          //this.msgBox.showSuccessMessage('Payment successiful')
          this.msg.showSuccessMessage('Payment successiful')
          this.printReceipt()

          this.getMachineServiceBillReceivables(this.machineId)
          this.refreshBillReceivables()
          this.toPrintReceipt = true
        }
      )
      .catch(
        error => {
          console.log(error)
          //this.msgBox.showErrorMessage(error, 'Could not confirm payment')
          this.receiptData = []
          this.msg.showErrorMessage(error, 'Could not confirm payment')
        }
      )
  }

  clearMachineServiceBill() {
    this.machineServiceBillReceivableId = null
    this.machineServiceBillReceivableDescription = ''
    this.machineServiceBillReceivableAmount = 0
    this.machineServiceBillReceivableQty = 0
    this.machineServiceBillReceivableNoOfDays = 0
    this.machineServiceBillReceivableDiscount = 0
    this.machineServiceBillReceivableStartingDate = null
    this.machineServiceBillReceivableEndingDate = null
  }

  clearServiceBill() {
    this.serviceBillReceivableId = null
    this.serviceBillReceivableDate = null
    this.serviceBillReceivableDescription = ''
    this.serviceBillReceivableAmount = 0
    this.serviceBillReceivablePrice = 0
    this.serviceBillReceivableQty = 0
    this.serviceBillReceivableDiscount = 0
  }


  async printReceipt() {

    // if (this.toPrintReceipt == false) {
    //   return
    // }

    if (this.receiptData.length == 0) {
      this.msg.showErrorMessage3('No data to print')
      return
    }

    var items: ReceiptItem[] = []
    var item: ReceiptItem

    this.receiptData.forEach(element => {
      item = new ReceiptItem()
      item.code = element.id
      item.name = element.summary
      item.amount = element.amount
      item.qty = parseFloat(element.qty)
      items.push(item)
    })

    var customer: ICustomer = {
      name: '',
      address: '',
      phone: ''
    }

    this.printer.print(items, '', 0, customer)
    this.toPrintReceipt = false
  }

  clearReceipt() {
    this.receiptData = []
  }






  generatePDF() {
    const documentDefinition = {
      content: [
        { text: 'Hello, World!', fontSize: 18, bold: true },
        { text: 'This is a sample PDF generated using pdfMake in Angular.' }
      ]
    };
    pdfMake.createPdf(documentDefinition).open();
  }









  // print = async (items : ReceiptItem[], receiptNo :string, cash : number, patient : IPatient) => {
  print = async (receiptNo: string, cash: number) => {

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title = 'Receipt'
    var total: number = 0
    var discount: number = 0
    var tax: number = 0

    //var address : any = await this.data.getReceiptHeader(receiptNo)
    var address: any = await this.data.getBranchReceiptHeader(receiptNo)

    var receipt = [
      [
        { text: 'SN', fontSize: 8, bold: true },
        { text: 'Item', fontSize: 8, bold: true },
        { text: 'Qty', fontSize: 8, bold: true },
        { text: 'Amount', fontSize: 8, bold: true },
      ]
    ]

    var sn = 0

    this.billReceivables.forEach((element) => {
      total = total + (+element.amount)
      sn = sn + 1
      var item = [
        { text: sn.toString(), fontSize: 8, bold: false },
        { text: element.summary, fontSize: 8, bold: false },
        { text: element.qty.toString(), fontSize: 8, bold: false },
        { text: (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 8, alignment: 'right', bold: false },
      ]
      receipt.push(item)
    })
    var detailSummary = [
      { text: ' ', fontSize: 8, bold: false },
      { text: 'Total', fontSize: 9, bold: true },
      { text: ' ', fontSize: 8, bold: false },
      { text: (+total).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
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
              // [{text : patient?.firstName + ' '+ patient?.middleName + ' '+ patient?.lastName , fontSize : 8}],
              // [{text : patient?.no, fontSize : 8}],
              // [{text : patient?.address, fontSize : 8}],
              // [{text : patient?.phoneNo, fontSize : 8}],
              // [{text : '________________________________',alignment : 'center',}],
              [{ text: '', fontSize: 8 }],
              [{ text: '', fontSize: 8 }],
              [{ text: '', fontSize: 8 }],
              [{ text: '', fontSize: 8 }],
              [{ text: '________________________________', alignment: 'center', }],
            ]
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [200],
            body: [
              [{ text: 'Receipt', alignment: 'center', fontSize: 9, bold: true }],
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
            widths: [210],
            body: [
              [{ text: '==============================' }],
              [{ text: 'Served By : ' + localStorage.getItem('user-name'), fontSize: 9, alignment: 'left' }],
              [{ text: 'Developed By @Orbix Systems', fontSize: 10, bold: true, alignment: 'center' }],
              [{ text: '***End of Receipt***', fontSize: 9, alignment: 'center' }]
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

  async get(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IMachine>(API_URL + '/machines/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.startBillingAt = null
          this.showWeighData(data!)
          console.log(data)
        }
      )
  }


  ///////////////////////////////////////
  showWeighData(data: IMachine) {
    this.machineId = data?.id
    this.machineNo = data?.no
  }

  clearWeighData() {
    this.machineId = null
    this.machineNo = ''
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

    this.goodTypeId = null
    this.goodTypeName = ''

    this.warehouseId = null
    this.warehouseName = ''

    this.billingType = ''
  }

  totalQty: number = 0
  billedQty: number = 0
  unbilledQty: number = 0
  billingRate: number = 0
  noOfDays: number = 0

  totalBillingAmount: number = 0

  async calcBillAmount() {
    if (this.qty > this.unbilledQty) {
      this.qty = 0
    } else if (this.qty < 0) {
      this.qty = 0
    }
    this.totalBillingAmount = this.qty * this.billingRate * this.noOfDays
    if (this.billingType == 'FLAT-RATE') {
      this.totalBillingAmount = this.qty * this.billingRate
    }

  }


  ////////////////////////////////////////



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

          this.qtyToRelease = this.availableForRelease

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
          //this.getMachineServiceBillReceivables(this.weighId)
          this.msg.showSuccessMessage('Success')
          console.log(data)
          this.printReleaseNote(data!.id)
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Error')
          console.log(error)
        }
      )


  }




  ///////////////////////////////////


  printGatePassRcpt = async (billItems: IServiceBillItem[], receiptNo: string, cash: number) => {

    await this.get(this.machineId)
    await this.getLastBillingDate(this.machineId)

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
              [{ text: 'Client Name: ' + this.weighGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.ownerAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Phone: ' + this.ownerPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
              [{ text: 'Number of Days: ' + this.noOfDays, alignment: 'left', fontSize: 9, bold: true }],
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




  releases: IWeighGoodRelease[] = []

  async getWeighGoodReleases(weighId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.releases = []

    await this.http.get<IWeighGoodRelease[]>(API_URL + '/weigh_good_releases/get_by_weigh?weigh_id=' + weighId, options)
      .toPromise()
      .then(
        data => {

          this.releases = data!.slice().reverse()
          console.log(data)
        }
      )
  }

  //   String id;
  // String no;
  // String qty;
  // String status;
  // String weighId;
  // String releaseDate;
  // //
  // String clientName;
  // String goodName;
  // String unitPrice;
  // String total;


  weighGoodReleaseId: any
  weighGoodReleaseNo: string = ''
  weighGoodReleaseQty: number = 0
  weighGoodReleaseStatus: string = ''
  weighGoodReleaseWeighId: string = ''
  weighGoodReleaseReleaseDate: string = ''
  weighGoodCheckedInDate: string = ''
  weighGoodReleaseClientName: string = ''
  weighGoodReleaseClientAddress: string = ''
  weighGoodReleaseClientPhoneNo: string = ''
  weighGoodReleaseGoodName: string = ''
  weighGoodReleaseUnitPrice: string = ''
  weighGoodReleaseTotal: string = ''

  billItems: IServiceBillItem[] = []
  billItem: IServiceBillItem
  async getWeighGoodRelease(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.weighGoodReleaseId = null
    this.weighGoodReleaseNo = ''
    this.weighGoodReleaseQty = 0
    this.weighGoodReleaseStatus = ''
    this.weighGoodReleaseWeighId = ''
    this.weighGoodReleaseReleaseDate = ''
    this.weighGoodCheckedInDate = ''
    this.weighGoodReleaseClientName = ''
    this.weighGoodReleaseClientAddress = ''
    this.weighGoodReleaseClientPhoneNo = ''
    this.weighGoodReleaseGoodName = ''
    this.weighGoodReleaseUnitPrice = ''
    this.weighGoodReleaseTotal = ''

    this.billItems = []

    await this.http.get<IWeighGoodRelease>(API_URL + '/weigh_good_releases/get?id=' + id, options)
      .toPromise()
      .then(
        data => {

          this.weighGoodReleaseId = data!.id
          this.weighGoodReleaseNo = data!.no
          this.weighGoodReleaseQty = data!.qty
          this.weighGoodReleaseStatus = data!.status
          this.weighGoodReleaseWeighId = data!.weighId
          this.weighGoodReleaseReleaseDate = data!.releaseDate
          this.weighGoodCheckedInDate = data!.checkedInDate
          this.weighGoodReleaseClientName = data!.clientName
          this.weighGoodReleaseClientAddress = data!.clientAddress
          this.weighGoodReleaseClientPhoneNo = data!.clientPhoneNo
          this.weighGoodReleaseGoodName = data!.goodName
          this.weighGoodReleaseUnitPrice = data!.unitPrice
          this.weighGoodReleaseTotal = data!.total

          this.billItems = []

          this.billItem = {
            sn: 1,
            item: this.weighGoodReleaseGoodName,
            qty: this.weighGoodReleaseQty,
            noOfDays: data!.noOfDays,
            amount: Number(this.weighGoodReleaseTotal)
          } as IServiceBillItem;

          this.billItems.push(this.billItem)

          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }

  async printWeighGoodReleaseNote(id: any) {
    await this.getWeighGoodRelease(id)

    this.documentHeader = await this.data.getDocumentHeader()
    const title = 'Gate Pass - Weigh Release'

    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'potrait',
      footer: (currentPage: any, pageCount: any) => ({
        text: `${currentPage} of ${pageCount}`,
        alignment: 'center',
        fontSize: 8,
      }),
      content: [
        // Document Header
        {
          columns: [this.documentHeader],
          margin: [0, 0, 0, 10]
        },

        // Title
        {
          text: title,
          fontSize: 16,
          bold: true,
          alignment: 'left',
          margin: [0, 10, 0, 20],
        },

        // No and Date
        {
          columns: [
            {
              text: 'No: ' + this.weighGoodReleaseNo,
              fontSize: 12,
              width: '50%',
            },
            {
              text: 'Date: ____________',
              alignment: 'right',
              fontSize: 12,
              width: '50%',
            },
          ],
          margin: [0, 0, 0, 10],
        },

        // Client Name
        // {
        //   text: 'Client Name: ' + this.weighGoodReleaseClientName,
        //   fontSize: 12,
        //   margin: [0, 0, 0, 15],
        // },

        {
          layout: 'noBorders',
          table: {
            headerRows: 0,
            widths: [200],
            body: [
              [{ text: title, alignment: 'center', fontSize: 9, bold: true }],
              [{ text: 'Client Name: ' + this.weighGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.weighGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.weighGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: '________________________________' }],
              [{ text: 'Goods Details', alignment: 'center', fontSize: 9, bold: true }],
              [{ text: ' ', alignment: 'center', fontSize: 9, bold: true }],
            ]
          }
        },

        // Table Header
        {
          columns: [
            { text: 'Description', bold: true, fontSize: 12, width: '30%' },
            { text: 'Qty', bold: true, fontSize: 12, width: '20%' },
            { text: 'Unit Price', bold: true, fontSize: 12, width: '25%', alignment: 'right' },
            { text: 'Total', bold: true, fontSize: 12, width: '25%', alignment: 'right' },
          ],
          margin: [0, 0, 0, 5],
        },

        // Table Row
        {
          columns: [
            { text: this.weighGoodReleaseGoodName, fontSize: 11, width: '25%' },
            { text: this.weighGoodReleaseQty, fontSize: 11, width: '25%' },
            { text: this.getTzFormatCurrency(this.weighGoodReleaseUnitPrice), fontSize: 11, alignment: 'right', width: '25%' },
            { text: this.getTzFormatCurrency(this.weighGoodReleaseTotal), fontSize: 11, alignment: 'right', width: '25%' },
          ],
          margin: [0, 0, 0, 10],
        },

        // Total Section
        {
          columns: [
            { text: '', width: '50%' },
            {
              text: 'Total (TZS): ' + this.getTzFormatCurrency(this.weighGoodReleaseTotal),
              fontSize: 12,
              bold: true,
              alignment: 'right',
              width: '50%',
            },
          ],
          margin: [0, 10, 0, 20],
        },
        { text: '' },
        { text: '' },
        {
          text: 'Served By: _________________________',
          fontSize: 12,
          margin: [0, 0, 0, 15],
        },
      ],
    };

    pdfMake.createPdf(docDefinition).print();

  }

  printGatePassNote = async (billItems: IServiceBillItem[], receiptNo: string, cash: number, id: any) => {

    //await this.get(this.parkingId)
    //await this.getWeighGoodRelease(id)
    //await this.getLastBillingDate(this.parkingId)

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title = 'Cargo Gate Pass'
    var total: number = 0
    var discount: number = 0
    var tax: number = 0

    // var address : any = await this.data.getReceiptHeader(receiptNo)
    var address: any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(receiptNo)

    var receipt = [
      [
        { text: 'SN', fontSize: 8, bold: true },
        { text: 'Good Name', fontSize: 8, bold: true },
        { text: 'Qty', fontSize: 8, bold: true },
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
      ]
      receipt.push(item)
    })

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
              [{ text: title, alignment: 'center', fontSize: 9, bold: true }],
              [{ text: 'Client Name: ' + this.weighGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.weighGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.weighGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: '________________________________' }],
              [{ text: 'Goods Details', alignment: 'center', fontSize: 9, bold: true }],
              [{ text: ' ', alignment: 'center', fontSize: 9, bold: true }],
            ]
          }
        },
        {
          layout: 'noBorders',
          table: {
            headerRows: 1,
            widths: [25, 120, 35],
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
              // [{text : 'Cashier Comments', alignment : 'left', fontSize : 9, bold : true}],
              //[{text : this.comments, alignment : 'left', fontSize : 9, bold : false}],
              [{ text: 'Check In Date: ' + this.weighGoodCheckedInDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Date: ' + this.weighGoodReleaseReleaseDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Time: ' + this.weighGoodReleaseReleaseDate.substring(11), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Number of Days: ' + this.noOfDays, alignment: 'left', fontSize: 9, bold: true }],
              //[{text : 'Checkout At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
              //[{text : 'Day Out: ' + this.lastBillingDate, alignment : 'left', fontSize : 9, bold : true}],
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

  async printReleaseNote(id: any) {
    await this.getWeighGoodRelease(id)
    await this.printGatePassNote(this.billItems, this.weighGoodReleaseNo, 0, id)
  }

  getTzFormatCurrency(value: any) {
    return new Intl.NumberFormat('en-TZ', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }




}

export interface IBillSummary {
  billReceivables: IBillReceivable[]
  billReceivableCollections: IBillReceivableCollection[]
}

export interface IBillReceivableCollection {
  amount: number
  payCode: string
  refNo: string
}

interface IModel {
  stringData: string
}


// @Data
// class WeighCustomBillDetail{
// 	double totalQty;
// 	double billedQty;
// 	double unbilledQty;
// 	double billingRate;
// }

interface IWeighCustomBillDetail {
  totalQty: number
  billedQty: number
  unbilledQty: number
  billingRate: number
  noOfDays: number
  billingType: string
}


interface IWeighGoodReleaseDetail {
  id: any
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IWeighGoodRelease {
  id: any
  no: string
  qty: number
  noOfDays: number
  status: string
  weighId: any
  checkedInDate: string
  releaseDate: string
  clientName: string
  clientAddress: string
  clientPhoneNo: string
  goodName: string
  unitPrice: string
  total: string
}

interface IDiscountRequest {
  serviceBillId: any
  billAmount: number
  discountAmount: number
  serviceBillName: string
  reason: string
  comments: string
}
