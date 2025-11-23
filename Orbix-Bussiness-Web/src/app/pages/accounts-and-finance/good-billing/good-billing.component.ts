import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IBillReceivable, IStorageBillReceivable, IServiceBillReceivable } from 'src/app/domain/bill-receivable';
import { environment } from 'src/environments/environment';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem, IStorage } from 'src/app/domain/storage';
import { ICustomer } from 'src/app/domain/customer';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-good-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './good-billing.component.html',
  styleUrl: './good-billing.component.scss'
})
export class GoodBillingComponent {
  // Storage attributes
  storageId: any = null
  storageNo: string = ''
  storageBillReceivableId: any = null
  storageBillReceivableDescription: string = ''
  storageBillReceivableStartingDate: Date | null
  storageBillReceivableEndingDate: Date | null
  storageBillReceivablePrice: number = 0
  storageBillReceivableQty: number = 0
  storageBillReceivableNoOfDays: number = 0
  storageBillReceivableDiscount: number = 0
  storageBillReceivableAmount: number = 0
  storageBillReceivableStatus: string = ''

  // Service attributes
  serviceBillReceivableId: any = null
  serviceBillReceivableDate: Date | null
  serviceBillReceivableDescription: string = ''
  serviceBillReceivablePrice: number = 0
  serviceBillReceivableQty: number = 0
  serviceBillReceivableDiscount: number = 0
  serviceBillReceivableStatus: string = ''
  serviceBillReceivableAmount: number = 0

  //Storage and bills Collections attributes
  storageBillReceivables: IStorageBillReceivable[] = []
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
      this.storageId = params['storage_id']
    })
    this.getStorageBillReceivables(this.storageId)
    this.getStorageServiceBillReceivables(this.storageId)
  }


  async getStorageBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IStorageBillReceivable>(API_URL + '/storage_bill_receivables/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.storageBillReceivableId = data!.id
          this.storageBillReceivableDescription = data!.description
          this.storageBillReceivableStartingDate = data!.startedAt
          this.storageBillReceivableEndingDate = data!.endedAt
          this.storageBillReceivablePrice = data!.price
          this.storageBillReceivableQty = data!.qty
          this.storageBillReceivableNoOfDays = data!.noOfDays
          this.storageBillReceivableDiscount = data!.discount
          this.storageBillReceivableAmount = data!.amount
          this.storageBillReceivableStatus = data!.payStatus
          this.billingType = data!.billingType

          console.log(data)

          this.getDiscount()
        }
      )

  }


  qty: number = 0
  async createStorageCustomBill() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var customBill = {
      storageId: this.storageId,
      qty: this.qty,

    }

    await this.http.post<IStorageBillReceivable>(API_URL + '/storages/create_storage_custom_bill_receivable', customBill, options)
      .toPromise()
      .then(
        data => {
          this.getStorageBillReceivables(this.storageId)
          this.msg.showSuccessMessage('Storage bill created successfully')
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


  async saveStorageBill() {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to save this storage bill?', 'question', 'Yes', 'No') == false) {
      return
    }

    // If storage bill receivable id is null, create new storage bill receivable
    // If storage bill receivable id is not null, update storage bill receivable
    if (this.storageId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var storageBill = {
        id: this.storageBillReceivableId,
        description: this.storageBillReceivableDescription,
        startedAt: this.storageBillReceivableStartingDate,
        endedAt: this.storageBillReceivableEndingDate,
        price: this.storageBillReceivablePrice,
        qty: this.storageBillReceivableQty,
        discount: this.storageBillReceivableDiscount,
        storageId: this.storageId
      }

      if (this.storageBillReceivableId == null) {
        // Create new storage bill receivable
        await this.http.post<IStorageBillReceivable>(API_URL + '/storage_bill_receivables/create_storage_bill_receivable?storage_id=' + this.storageId, storageBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Storage bill created successfully')
              this.getStorageBillReceivables(this.storageId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getStorageBillReceivables(this.storageId)
              console.log(error)
            }
          )
      } else {
        await this.http.post<IStorageBillReceivable>(API_URL + '/storage_bill_receivables/update_storage_bill_receivable?storage_id=' + this.storageId, storageBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Storage bill updated successfully')
              this.getStorageBillReceivables(this.storageId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getStorageBillReceivables(this.storageId)
              console.log(error)
            }
          )
      }
    } else {
      this.msg.showErrorMessage3('No storage available')
    }
  }

  async requestDiscount() {
    // if (await this.msg.showConfirmMessageDialog('Confirm', 'Confirm Requesting Discount?', 'question', 'Yes', 'No') == false) {
    //   return
    // }

    if (this.storageId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var discountRequest = {
        serviceBillId: this.storageBillReceivableId,
        billAmount: (this.storageBillReceivablePrice * this.storageBillReceivableQty * this.storageBillReceivableNoOfDays),
        discountAmount: this.storageBillReceivableDiscount,
        serviceBillName: 'Storage',
        reason: this.discountReason

      }

      await this.http.post<IStorageBillReceivable>(API_URL + '/discount_requests/create?service_bill_id=' + this.storageBillReceivableId + '&bill_amount=' + (this.storageBillReceivablePrice * this.storageBillReceivableQty * this.storageBillReceivableNoOfDays) + '&discount_amount=' + this.storageBillReceivableDiscount + '&service_bill_name=Storage', discountRequest, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Discount request sent successfully')
            this.getStorageBillReceivables(this.storageId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getStorageBillReceivables(this.storageId)
            console.log(error)
          }
        )

    }
  }


  discountReason: string = ''
  discountComments: string = ''

  async getDiscount() {

    if (this.storageId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      await this.http.get<IDiscountRequest>(API_URL + '/discount_requests/get_discount?service_bill_id=' + this.storageBillReceivableId + '&bill_amount=' + (this.storageBillReceivablePrice * this.storageBillReceivableQty) + '&discount_amount=' + this.storageBillReceivableDiscount + '&service_bill_name=Storage', options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.storageBillReceivableDiscount = data!.discountAmount
            this.discountReason = data!.reason
            this.discountComments = data!.comments

          }
        )
      // .catch(
      //   error => {
      //     this.msg.showErrorMessage(error, 'Error')
      //     this.getParkingBillReceivables(this.parkingId)
      //     console.log(error)
      //   }
      // )

    }
  }

  async saveServiceBill() {
    // If service bill receivable id is null, create new service bill receivable
    // If service bill receivable id is not null, update service bill receivable
    if (this.storageId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var serviceBill = {
        id: this.serviceBillReceivableId,
        serviceDate: this.serviceBillReceivableDate,
        description: this.serviceBillReceivableDescription,
        price: this.serviceBillReceivablePrice,
        qty: this.serviceBillReceivableQty,
        discount: this.serviceBillReceivableDiscount,
        storageId: this.storageId
      }

      if (this.serviceBillReceivableId == null) {
        // Create new service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL + '/storage_bill_receivables/create_service_bill_receivable?storage_id=' + this.storageId, serviceBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Service bill created successfully')
              this.getStorageServiceBillReceivables(this.storageId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getStorageServiceBillReceivables(this.storageId)
              console.log(error)
            }
          )
      } else {
        // Update service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL + '/storage_bill_receivables/update_service_bill_receivable?storage_id=' + this.storageId, serviceBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Service bill updated successfully')
              this.getStorageServiceBillReceivables(this.storageId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getStorageServiceBillReceivables(this.storageId)
              console.log(error)
            }
          )
      }

    } else {
      this.msg.showErrorMessage3('No storage available')
    }
  }


  async deleteServiceBill(id: any) {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to delete this service bill?', 'question', 'Yes', 'No') == false) {
      return
    }

    if (this.storageId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var serviceBill = {
        id: id,
        storageId: this.storageId
      }

      await this.http.post<IServiceBillReceivable>(API_URL + '/storage_bill_receivables/delete_service_bill_receivable', serviceBill, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Service bill deleted successfully')
            this.getStorageServiceBillReceivables(this.storageId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getStorageServiceBillReceivables(this.storageId)
            console.log(error)
          }
        )
    } else {
      this.msg.showErrorMessage3('No storage available')
    }
  }

  async getServiceBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IServiceBillReceivable>(API_URL + '/storage_service_bill_receivables/get?id=' + id, options)
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

  async getStorageBillReceivables(storageId: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.storageBillReceivables = []

    await this.http.get<IStorageBillReceivable[]>(API_URL + '/storage_bill_receivables/get_all_by_storage?storage_id=' + storageId, options)
      .toPromise()
      .then(
        data => {

          data?.forEach(element => {
            this.storageBillReceivables.push(element)
          })
          var sn = 1
          this.storageBillReceivables.reverse().forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  async getStorageServiceBillReceivables(storageId: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.serviceBillReceivables = []

    await this.http.get<IServiceBillReceivable[]>(API_URL + '/service_bill_receivables/get_all_by_storage?storage_id=' + storageId, options)
      .toPromise()
      .then(
        data => {

          data?.forEach(element => {
            this.serviceBillReceivables.push(element)
          })
          var sn = 1
          this.serviceBillReceivables.reverse().forEach(element => {
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
  async getBillReceivables(storageId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.billReceivables = []
    this.totalBillReceivable = 0

    await this.http.get<IBillReceivable[]>(API_URL + '/bill_receivables/get_all_by_storage?storage_id=' + storageId, options)
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
    this.getStorageBillReceivables(this.storageId)
    this.getStorageServiceBillReceivables(this.storageId)
  }



  getUnpaidBills() { }

  confirmBillPayment() { }

  async refreshBillReceivables() {
    this.billReceivables = []
    await this.getBillReceivables(this.storageId)
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

          this.getStorageBillReceivables(this.storageId)
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

  clearStorageBill() {
    this.storageBillReceivableId = null
    this.storageBillReceivableDescription = ''
    this.storageBillReceivableAmount = 0
    this.storageBillReceivableQty = 0
    this.storageBillReceivableNoOfDays = 0
    this.storageBillReceivableDiscount = 0
    this.storageBillReceivableStartingDate = null
    this.storageBillReceivableEndingDate = null
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


  async checkOut(): Promise<void> {


    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var storage = {
      id: this.storageId
      // cardNo : this.cardNo,
      // storageZoneName : this.storageZoneName,
      // startBillingAt : this.startBillingAt
    }

    await this.http.post<IStorage>(API_URL + '/storages/check_out', storage, options)
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
  }



  printReceipt() {

    if (this.toPrintReceipt == false) {
      return
    }

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
      name: this.storageGoodReleaseClientName,
      address: this.ownerAddress,
      phone: this.ownerPhoneNo
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

  async getLastBillingDate(storageId: any) {
    // this.lastBillingDate = ''
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IModel>(API_URL + '/storages/get_last_storage_bill_date?id=' + storageId, options)
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
    await this.http.get<IStorage>(API_URL + '/storages/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.startBillingAt = null
          this.showStorageData(data!)
          console.log(data)
        }
      )
  }


  ///////////////////////////////////////
  showStorageData(data: IStorage) {

    this.storageId = data?.id
    this.storageNo = data?.no

    this.ownerFirstName = data?.ownerFirstName;
    this.ownerMiddleName = data?.ownerMiddleName;
    this.ownerLastName = data?.ownerLastName;
    this.ownerCompanyName = data?.ownerCompanyName;
    this.ownerIdNo = data?.ownerIdNo;
    this.ownerIdType = data?.ownerIdType;
    this.ownerPhoneNo = data?.ownerPhoneNo;
    this.ownerEmail = data?.ownerEmail;
    this.ownerAddress = data?.ownerAddress;

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount

    this.goodTypeName = data!.goodTypeName,

      this.warehouseId = data!.warehouseId
    this.warehouseName = data!.warehouseName,

      this.billingType = data!.billingType

    this.status = data!.status

    this.validUntilDate = null

    this.comments = '' // check this

  }

  clearStorageData() {
    this.storageId = null
    this.storageNo = ''
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

  async getStorageCustomBillingDetails() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.totalQty = 0
    this.billedQty = 0
    this.unbilledQty = 0
    this.billingRate = 0
    this.noOfDays = 0
    this.qty = 0

    await this.http.get<IStorageCustomBillDetail>(API_URL + '/storages/get_custom_bill_item?storage_id=' + this.storageId, options)
      .toPromise()
      .then(
        data => {

          this.totalQty = data!.totalQty
          this.billedQty = data!.billedQty
          this.unbilledQty = data!.unbilledQty
          this.billingRate = data!.billingRate
          this.noOfDays = data!.noOfDays
          this.qty = data!.unbilledQty // set defauult to unbilled qty
          this.billingType = data!.billingType

          this.calcBillAmount()

          console.log(data)
        }
      )
  }

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

  currentStorageId: any = null

  async getStorageGoodReleaseDetail(storageId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.originalQty = 0
    this.availableQty = 0
    this.releasedQty = 0
    this.availableForRelease = 0

    this.currentStorageId = null

    this.qtyToRelease = 0

    await this.http.get<IStorageGoodReleaseDetail>(API_URL + '/storage_good_releases/get_storage_good_release_detail?storage_id=' + storageId, options)
      .toPromise()
      .then(
        data => {

          this.currentStorageId = storageId

          this.originalQty = data!.initialQty
          this.availableQty = data!.currentQty
          this.releasedQty = data!.releasedQty
          this.availableForRelease = data!.availableForRelease

          this.qtyToRelease = this.availableForRelease

          console.log(data)
        }
      )
  }


  async createStorageGoodRelease() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var storageGoodRelease = {
      storageId: this.currentStorageId,
      qty: this.qtyToRelease,

    }

    await this.http.post<IStorageGoodReleaseDetail>(API_URL + '/storage_good_releases/create_storage_good_release', storageGoodRelease, options)
      .toPromise()
      .then(
        data => {
          //this.getStorageBillReceivables(this.storageId)
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

    await this.get(this.storageId)
    await this.getLastBillingDate(this.storageId)

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title = 'Gate Pass'
    var total: number = 0
    var discount: number = 0
    var tax: number = 0

    // var address : any = await this.data.getReceiptHeader(receiptNo)
    var address: any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(receiptNo)

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
              [{ text: 'Client Name: ' + this.storageGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
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




  releases: IStorageGoodRelease[] = []

  async getStorageGoodReleases(storageId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.releases = []

    await this.http.get<IStorageGoodRelease[]>(API_URL + '/storage_good_releases/get_by_storage?storage_id=' + storageId, options)
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
  // String storageId;
  // String releaseDate;
  // //
  // String clientName;
  // String goodName;
  // String unitPrice;
  // String total;


  storageGoodReleaseId: any
  storageGoodReleaseNo: string = ''
  storageGoodReleaseQty: number = 0
  storageGoodReleaseStatus: string = ''
  storageGoodReleaseStorageId: string = ''
  storageGoodReleaseReleaseDate: string = ''
  storageGoodCheckedInDate: string = ''
  storageGoodReleaseClientName: string = ''
  storageGoodReleaseClientAddress: string = ''
  storageGoodReleaseClientPhoneNo: string = ''
  storageGoodReleaseGoodName: string = ''
  storageGoodReleaseUnitPrice: string = ''
  storageGoodReleaseTotal: string = ''

  billItems: IServiceBillItem[] = []
  billItem: IServiceBillItem
  async getStorageGoodRelease(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.storageGoodReleaseId = null
    this.storageGoodReleaseNo = ''
    this.storageGoodReleaseQty = 0
    this.storageGoodReleaseStatus = ''
    this.storageGoodReleaseStorageId = ''
    this.storageGoodReleaseReleaseDate = ''
    this.storageGoodCheckedInDate = ''
    this.storageGoodReleaseClientName = ''
    this.storageGoodReleaseClientAddress = ''
    this.storageGoodReleaseClientPhoneNo = ''
    this.storageGoodReleaseGoodName = ''
    this.storageGoodReleaseUnitPrice = ''
    this.storageGoodReleaseTotal = ''

    this.billItems = []

    await this.http.get<IStorageGoodRelease>(API_URL + '/storage_good_releases/get?id=' + id, options)
      .toPromise()
      .then(
        data => {

          this.storageGoodReleaseId = data!.id
          this.storageGoodReleaseNo = data!.no
          this.storageGoodReleaseQty = data!.qty
          this.storageGoodReleaseStatus = data!.status
          this.storageGoodReleaseStorageId = data!.storageId
          this.storageGoodReleaseReleaseDate = data!.releaseDate
          this.storageGoodCheckedInDate = data!.checkedInDate
          this.storageGoodReleaseClientName = data!.clientName
          this.storageGoodReleaseClientAddress = data!.clientAddress
          this.storageGoodReleaseClientPhoneNo = data!.clientPhoneNo
          this.storageGoodReleaseGoodName = data!.goodName
          this.storageGoodReleaseUnitPrice = data!.unitPrice
          this.storageGoodReleaseTotal = data!.total

          this.billItems = []

          this.billItem = {
            sn: 1,
            item: this.storageGoodReleaseGoodName,
            qty: this.storageGoodReleaseQty,
            noOfDays: data!.noOfDays,
            amount: Number(this.storageGoodReleaseTotal)
          } as IServiceBillItem;

          this.billItems.push(this.billItem)

          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }

  async printStorageGoodReleaseNote(id: any) {
    await this.getStorageGoodRelease(id)

    this.documentHeader = await this.data.getDocumentHeader()
    const title = 'Gate Pass - Storage Release'

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
              text: 'No: ' + this.storageGoodReleaseNo,
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
        //   text: 'Client Name: ' + this.storageGoodReleaseClientName,
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
              [{ text: 'Client Name: ' + this.storageGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.storageGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.storageGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
            { text: this.storageGoodReleaseGoodName, fontSize: 11, width: '25%' },
            { text: this.storageGoodReleaseQty, fontSize: 11, width: '25%' },
            { text: this.getTzFormatCurrency(this.storageGoodReleaseUnitPrice), fontSize: 11, alignment: 'right', width: '25%' },
            { text: this.getTzFormatCurrency(this.storageGoodReleaseTotal), fontSize: 11, alignment: 'right', width: '25%' },
          ],
          margin: [0, 0, 0, 10],
        },

        // Total Section
        {
          columns: [
            { text: '', width: '50%' },
            {
              text: 'Total (TZS): ' + this.getTzFormatCurrency(this.storageGoodReleaseTotal),
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
    //await this.getStorageGoodRelease(id)
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
              [{ text: 'Client Name: ' + this.storageGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.storageGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.storageGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
              [{ text: 'Check In Date: ' + this.storageGoodCheckedInDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Date: ' + this.storageGoodReleaseReleaseDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Time: ' + this.storageGoodReleaseReleaseDate.substring(11), alignment: 'left', fontSize: 9, bold: true }],
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
    await this.getStorageGoodRelease(id)
    await this.printGatePassNote(this.billItems, this.storageGoodReleaseNo, 0, id)
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
// class StorageCustomBillDetail{
// 	double totalQty;
// 	double billedQty;
// 	double unbilledQty;
// 	double billingRate;
// }

interface IStorageCustomBillDetail {
  totalQty: number
  billedQty: number
  unbilledQty: number
  billingRate: number
  noOfDays: number
  billingType: string
}


interface IStorageGoodReleaseDetail {
  id: any
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IStorageGoodRelease {
  id: any
  no: string
  qty: number
  noOfDays: number
  status: string
  storageId: any
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
