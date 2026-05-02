import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IBillReceivable, IBondItemBillReceivable, IServiceBillReceivable } from 'src/app/domain/bill-receivable';
import { environment } from 'src/environments/environment';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem, IBondItem } from 'src/app/domain/bond-item';
import { ICustomer } from 'src/app/domain/customer';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-bond-item-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './bond-item-billing.component.html',
  styleUrl: './bond-item-billing.component.scss'
})
export class BondItemBillingComponent {
  // BondItem attributes
  bondItemId: any = null
  bondItemNo: string = ''
  bondItemBillReceivableId: any = null
  bondItemBillReceivableDescription: string = ''
  bondItemBillReceivableStartingDate: Date | null
  bondItemBillReceivableEndingDate: Date | null
  bondItemBillReceivablePrice: number = 0
  bondItemBillReceivableQty: number = 0
  bondItemBillReceivableNoOfDays: number = 0
  bondItemBillReceivableDiscount: number = 0
  bondItemBillReceivableAmount: number = 0
  bondItemBillReceivableStatus: string = ''

  // Service attributes
  serviceBillReceivableId: any = null
  serviceBillReceivableDate: Date | null
  serviceBillReceivableDescription: string = ''
  serviceBillReceivablePrice: number = 0
  serviceBillReceivableQty: number = 0
  serviceBillReceivableDiscount: number = 0
  serviceBillReceivableStatus: string = ''
  serviceBillReceivableAmount: number = 0

  //BondItem and bills Collections attributes
  bondItemBillReceivables: IBondItemBillReceivable[] = []
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

  vehicleName: string = ''
  chasisNo: string = ''
  color: string = ''

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

  bondZoneId: any = null
  bondZoneName: string = ''

  bondItemTypeName: string = ''
  bondItemTypeId: any = null


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

  async ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.bondItemId = params['bond_item_id']
    })
    await this.getBondItemBillReceivables(this.bondItemId)
    await this.getBondItemServiceBillReceivables(this.bondItemId)
  }


  async getBondItemBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IBondItemBillReceivable>(API_URL + '/bond_item_bill_receivables/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.bondItemBillReceivableId = data!.id
          this.bondItemBillReceivableDescription = data!.description
          this.bondItemBillReceivableStartingDate = data!.startedAt
          this.bondItemBillReceivableEndingDate = data!.endedAt
          this.bondItemBillReceivablePrice = data!.price
          this.bondItemBillReceivableQty = data!.qty
          this.bondItemBillReceivableNoOfDays = data!.noOfDays
          this.bondItemBillReceivableDiscount = data!.discount
          this.bondItemBillReceivableAmount = data!.amount
          this.bondItemBillReceivableStatus = data!.payStatus
          this.billingType = data!.billingType

          console.log(data)

          this.getDiscount()
        }
      )

  }


  qty: number = 0
  async createBondItemCustomBill() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var customBill = {
      bondItemId: this.bondItemId,
      qty: this.qty,

    }

    await this.http.post<IBondItemBillReceivable>(API_URL + '/bond_items/create_bond_item_custom_bill_receivable', customBill, options)
      .toPromise()
      .then(
        data => {
          this.getBondItemBillReceivables(this.bondItemId)
          this.msg.showSuccessMessage('BondItem bill created successfully')
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


  async saveBondItemBill() {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to save this bondItem bill?', 'question', 'Yes', 'No') == false) {
      return
    }

    // If bondItem bill receivable id is null, create new bondItem bill receivable
    // If bondItem bill receivable id is not null, update bondItem bill receivable
    if (this.bondItemId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var bondItemBill = {
        id: this.bondItemBillReceivableId,
        description: this.bondItemBillReceivableDescription,
        startedAt: this.bondItemBillReceivableStartingDate,
        endedAt: this.bondItemBillReceivableEndingDate,
        price: this.bondItemBillReceivablePrice,
        qty: this.bondItemBillReceivableQty,
        discount: this.bondItemBillReceivableDiscount,
        bondItemId: this.bondItemId
      }

      if (this.bondItemBillReceivableId == null) {
        // Create new bondItem bill receivable
        await this.http.post<IBondItemBillReceivable>(API_URL + '/bond_item_bill_receivables/create_bond_item_bill_receivable?bond_item_id=' + this.bondItemId, bondItemBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('BondItem bill created successfully')
              this.getBondItemBillReceivables(this.bondItemId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getBondItemBillReceivables(this.bondItemId)
              console.log(error)
            }
          )
      } else {
        await this.http.post<IBondItemBillReceivable>(API_URL + '/bond_item_bill_receivables/update_bond_item_bill_receivable?bond_item_id=' + this.bondItemId, bondItemBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('BondItem bill updated successfully')
              this.getBondItemBillReceivables(this.bondItemId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getBondItemBillReceivables(this.bondItemId)
              console.log(error)
            }
          )
      }
    } else {
      this.msg.showErrorMessage3('No bondItem available')
    }
  }

  async requestDiscount() {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Confirm Requesting Discount?', 'question', 'Yes', 'No') == false) {
      return
    }

    if (this.bondItemId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var discountRequest = {
        serviceBillId: this.bondItemBillReceivableId,
        billAmount: this.bondItemBillReceivableAmount, // (this.bondItemBillReceivablePrice * this.bondItemBillReceivableQty * this.bondItemBillReceivableNoOfDays),
        discountAmount: this.bondItemBillReceivableDiscount,
        serviceBillName: 'Bond',
        reason: this.discountReason
      }

      await this.http.post<IBondItemBillReceivable>(API_URL + '/discount_requests/create?service_bill_id=' + this.bondItemBillReceivableId + '&bill_amount=' + (this.bondItemBillReceivablePrice * this.bondItemBillReceivableQty * this.bondItemBillReceivableNoOfDays) + '&discount_amount=' + this.bondItemBillReceivableDiscount + '&service_bill_name=BondItem', discountRequest, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Discount request sent successfully')
            this.getBondItemBillReceivables(this.bondItemId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getBondItemBillReceivables(this.bondItemId)
            console.log(error)
          }
        )

    }
  }


  discountReason: string = ''
  discountComments: string = ''

  async getDiscount() {

    if (this.bondItemId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      await this.http.get<IDiscountRequest>(API_URL + '/discount_requests/get_discount?service_bill_id=' + this.bondItemBillReceivableId + '&bill_amount=' + (this.bondItemBillReceivablePrice * this.bondItemBillReceivableQty) + '&discount_amount=' + this.bondItemBillReceivableDiscount + '&service_bill_name=Bond', options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.bondItemBillReceivableDiscount = data!.discountAmount
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
    if (this.bondItemId != null) {
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
        bondItemId: this.bondItemId
      }

      if (this.serviceBillReceivableId == null) {
        // Create new service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL + '/bond_item_bill_receivables/create_service_bill_receivable?bond_item_id=' + this.bondItemId, serviceBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Service bill created successfully')
              this.getBondItemServiceBillReceivables(this.bondItemId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getBondItemServiceBillReceivables(this.bondItemId)
              console.log(error)
            }
          )
      } else {
        // Update service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL + '/bond_item_bill_receivables/update_service_bill_receivable?bond_item_id=' + this.bondItemId, serviceBill, options)
          .toPromise()
          .then(
            data => {
              this.msg.showSuccessMessage('Service bill updated successfully')
              this.getBondItemServiceBillReceivables(this.bondItemId)
              console.log(data)
            }
          )
          .catch(
            error => {
              this.msg.showErrorMessage(error, 'Error')
              this.getBondItemServiceBillReceivables(this.bondItemId)
              console.log(error)
            }
          )
      }

    } else {
      this.msg.showErrorMessage3('No bondItem available')
    }
  }


  async deleteServiceBill(id: any) {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to delete this service bill?', 'question', 'Yes', 'No') == false) {
      return
    }

    if (this.bondItemId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var serviceBill = {
        id: id,
        bondItemId: this.bondItemId
      }

      await this.http.post<IServiceBillReceivable>(API_URL + '/bond_item_bill_receivables/delete_service_bill_receivable', serviceBill, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Service bill deleted successfully')
            this.getBondItemServiceBillReceivables(this.bondItemId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getBondItemServiceBillReceivables(this.bondItemId)
            console.log(error)
          }
        )
    } else {
      this.msg.showErrorMessage3('No bondItem available')
    }
  }

  async getServiceBill(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IServiceBillReceivable>(API_URL + '/bond_item_service_bill_receivables/get?id=' + id, options)
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

  async getBondItemBillReceivables(bondItemId: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.bondItemBillReceivables = []

    await this.http.get<IBondItemBillReceivable[]>(API_URL + '/bond_item_bill_receivables/get_all_by_bond_item?bond_item_id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {

          data?.forEach(element => {
            this.bondItemBillReceivables.push(element)
          })
          var sn = 1
          this.bondItemBillReceivables.reverse().forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  async getBondItemServiceBillReceivables(bondItemId: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.serviceBillReceivables = []

    await this.http.get<IServiceBillReceivable[]>(API_URL + '/service_bill_receivables/get_all_by_bond_item?bond_item_id=' + bondItemId, options)
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
  async getBillReceivables(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.billReceivables = []
    this.totalBillReceivable = 0

    await this.http.get<IBillReceivable[]>(API_URL + '/bill_receivables/get_all_by_bond_item?bond_item_id=' + bondItemId, options)
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
    this.getBondItemBillReceivables(this.bondItemId)
    this.getBondItemServiceBillReceivables(this.bondItemId)
  }



  getUnpaidBills() { }

  confirmBillPayment() { }

  async refreshBillReceivables() {
    this.billReceivables = []
    await this.getBillReceivables(this.bondItemId)
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

          this.getBondItemBillReceivables(this.bondItemId)
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

  clearBondItemBill() {
    this.bondItemBillReceivableId = null
    this.bondItemBillReceivableDescription = ''
    this.bondItemBillReceivableAmount = 0
    this.bondItemBillReceivableQty = 0
    this.bondItemBillReceivableNoOfDays = 0
    this.bondItemBillReceivableDiscount = 0
    this.bondItemBillReceivableStartingDate = null
    this.bondItemBillReceivableEndingDate = null
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

    var bondItem = {
      id: this.bondItemId
      // cardNo : this.cardNo,
      // bondItemZoneName : this.bondItemZoneName,
      // startBillingAt : this.startBillingAt
    }

    await this.http.post<IBondItem>(API_URL + '/bond_items/check_out', bondItem, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.msg.showSuccessMessage('Checked out Successifully')

          this.printGatePassRcpt(data!.serviceBillItems, '', 0);

          this.router.navigate(['/app/accounts-and-finance/bond-billing']);
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
      //return
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
      name: this.ownerFirstName + ' ' + this.ownerLastName!,
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

  async getLastBillingDate(bondItemId: any) {
    // this.lastBillingDate = ''
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IModel>(API_URL + '/bond_items/get_last_bond_item_bill_date?id=' + bondItemId, options)
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
    await this.http.get<IBondItem>(API_URL + '/bond_items/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.startBillingAt = null
          this.showBondItemData(data!)
          console.log(data)
        }
      )
  }


  ///////////////////////////////////////
  showBondItemData(data: IBondItem) {

    this.bondItemId = data?.id
    this.bondItemNo = data?.no

    this.ownerFirstName = data?.ownerFirstName;
    this.ownerMiddleName = data?.ownerMiddleName;
    this.ownerLastName = data?.ownerLastName;
    this.ownerCompanyName = data?.ownerCompanyName;
    this.ownerIdNo = data?.ownerIdNo;
    this.ownerIdType = data?.ownerIdType;
    this.ownerPhoneNo = data?.ownerPhoneNo;
    this.ownerEmail = data?.ownerEmail;
    this.ownerAddress = data?.ownerAddress;

    this.vehicleName = data?.bondItemName
    this.chasisNo = data?.chasisNo
    this.color = data?.bondItemColor

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount

    this.bondItemTypeName = data!.bondItemTypeName,

      this.bondZoneId = data!.bondZoneId
    this.bondZoneName = data!.bondZoneName,

      this.billingType = data!.billingType

    this.status = data!.status

    this.validUntilDate = null

    this.comments = '' // check this

  }

  clearBondItemData() {
    this.bondItemId = null
    this.bondItemNo = ''
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

    this.bondItemTypeId = null
    this.bondItemTypeName = ''

    this.bondZoneId = null
    this.bondZoneName = ''

    this.billingType = ''

    this.vehicleName = ''
    this.chasisNo = ''
    this.color = ''

  }

  totalQty: number = 0
  billedQty: number = 0
  unbilledQty: number = 0
  billingRate: number = 0
  noOfDays: number = 0

  totalBillingAmount: number = 0

  async getBondItemCustomBillingDetails() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.totalQty = 0
    this.billedQty = 0
    this.unbilledQty = 0
    this.billingRate = 0
    this.noOfDays = 0
    this.qty = 0

    await this.http.get<IBondItemCustomBillDetail>(API_URL + '/bond_items/get_custom_bill_item?bond_item_id=' + this.bondItemId, options)
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

  currentBondItemId: any = null

  async getBondItemGoodReleaseDetail(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.originalQty = 0
    this.availableQty = 0
    this.releasedQty = 0
    this.availableForRelease = 0

    this.currentBondItemId = null

    this.qtyToRelease = 0

    await this.http.get<IBondItemGoodReleaseDetail>(API_URL + '/bond_item_releases/get_bond_item__release_detail?bond_item_id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {

          this.currentBondItemId = bondItemId

          this.originalQty = data!.initialQty
          this.availableQty = data!.currentQty
          this.releasedQty = data!.releasedQty
          this.availableForRelease = data!.availableForRelease

          this.qtyToRelease = this.availableForRelease

          console.log(data)
        }
      )
  }


  async createBondItemGoodRelease() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var bondItemGoodRelease = {
      bondItemId: this.currentBondItemId,
      qty: this.qtyToRelease,

    }

    await this.http.post<IBondItemGoodReleaseDetail>(API_URL + '/bond_item_releases/create_bond_item_release', bondItemGoodRelease, options)
      .toPromise()
      .then(
        data => {
          //this.getBondItemBillReceivables(this.bondItemId)
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

  now = new Date();

  formatted = this.now.toLocaleString('en-GB', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });


  printGatePassRcpt = async (billItems: IServiceBillItem[], receiptNo: string, cash: number) => {

    await this.get(this.bondItemId)
    await this.getLastBillingDate(this.bondItemId)

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
              [{ text: 'Client Name: ' + this.ownerFirstName + ' ' + this.ownerLastName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.ownerAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Phone: ' + this.ownerPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Vehicle Information', alignment: 'center', fontSize: 9, bold: true }],
              [{ text: 'Vehicle Name: ' + this.vehicleName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Chasis No: ' + this.chasisNo, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Color: ' + this.color!, alignment: 'left', fontSize: 9, bold: false }],
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
              [{ text: 'Issued At: ' + this.formatted, alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Checkout At: ' + this.formatted, alignment: 'left', fontSize: 9, bold: true }],
              //[{ text: 'Valid Until: ' + this.lastBillingDate, alignment: 'left', fontSize: 9, bold: true }],
              //[{ text: 'Number of Days: ' + this.noOfDays, alignment: 'left', fontSize: 9, bold: true }],
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




  releases: IBondItemGoodRelease[] = []

  async getBondItemGoodReleases(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.releases = []

    await this.http.get<IBondItemGoodRelease[]>(API_URL + '/bond_item_releases/get_by_bond_item?bond_item_id=' + bondItemId, options)
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
  // String bondItemId;
  // String releaseDate;
  // //
  // String clientName;
  // String bondItemName;
  // String unitPrice;
  // String total;


  bondItemGoodReleaseId: any
  bondItemGoodReleaseNo: string = ''
  bondItemGoodReleaseQty: number = 0
  bondItemGoodReleaseStatus: string = ''
  bondItemGoodReleaseBondItemId: string = ''
  bondItemGoodReleaseReleaseDate: string = ''
  bondItemGoodCheckedInDate: string = ''
  bondItemGoodReleaseClientName: string = ''
  bondItemGoodReleaseClientAddress: string = ''
  bondItemGoodReleaseClientPhoneNo: string = ''
  bondItemGoodReleaseGoodName: string = ''
  bondItemGoodReleaseUnitPrice: string = ''
  bondItemGoodReleaseTotal: string = ''

  billItems: IServiceBillItem[] = []
  billItem: IServiceBillItem
  async getBondItemGoodRelease(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.bondItemGoodReleaseId = null
    this.bondItemGoodReleaseNo = ''
    this.bondItemGoodReleaseQty = 0
    this.bondItemGoodReleaseStatus = ''
    this.bondItemGoodReleaseBondItemId = ''
    this.bondItemGoodReleaseReleaseDate = ''
    this.bondItemGoodCheckedInDate = ''
    this.bondItemGoodReleaseClientName = ''
    this.bondItemGoodReleaseClientAddress = ''
    this.bondItemGoodReleaseClientPhoneNo = ''
    this.bondItemGoodReleaseGoodName = ''
    this.bondItemGoodReleaseUnitPrice = ''
    this.bondItemGoodReleaseTotal = ''

    this.billItems = []

    await this.http.get<IBondItemGoodRelease>(API_URL + '/bond_item_bond_item_releases/get?id=' + id, options)
      .toPromise()
      .then(
        data => {

          this.bondItemGoodReleaseId = data!.id
          this.bondItemGoodReleaseNo = data!.no
          this.bondItemGoodReleaseQty = data!.qty
          this.bondItemGoodReleaseStatus = data!.status
          this.bondItemGoodReleaseBondItemId = data!.bondItemId
          this.bondItemGoodReleaseReleaseDate = data!.releaseDate
          this.bondItemGoodCheckedInDate = data!.checkedInDate
          this.bondItemGoodReleaseClientName = data!.clientName
          this.bondItemGoodReleaseClientAddress = data!.clientAddress
          this.bondItemGoodReleaseClientPhoneNo = data!.clientPhoneNo
          this.bondItemGoodReleaseGoodName = data!.bondItemName
          this.bondItemGoodReleaseUnitPrice = data!.unitPrice
          this.bondItemGoodReleaseTotal = data!.total

          this.billItems = []

          this.billItem = {
            sn: 1,
            item: this.bondItemGoodReleaseGoodName,
            qty: this.bondItemGoodReleaseQty,
            noOfDays: data!.noOfDays,
            amount: Number(this.bondItemGoodReleaseTotal)
          } as IServiceBillItem;

          this.billItems.push(this.billItem)

          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }

  async printBondItemGoodReleaseNote(id: any) {
    await this.getBondItemGoodRelease(id)

    this.documentHeader = await this.data.getDocumentHeader()
    const title = 'Gate Pass - BondItem Release'

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
              text: 'No: ' + this.bondItemGoodReleaseNo,
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
        //   text: 'Client Name: ' + this.bondItemGoodReleaseClientName,
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
              [{ text: 'Client Name: ' + this.bondItemGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.bondItemGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.bondItemGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
            { text: this.bondItemGoodReleaseGoodName, fontSize: 11, width: '25%' },
            { text: this.bondItemGoodReleaseQty, fontSize: 11, width: '25%' },
            { text: this.getTzFormatCurrency(this.bondItemGoodReleaseUnitPrice), fontSize: 11, alignment: 'right', width: '25%' },
            { text: this.getTzFormatCurrency(this.bondItemGoodReleaseTotal), fontSize: 11, alignment: 'right', width: '25%' },
          ],
          margin: [0, 0, 0, 10],
        },

        // Total Section
        {
          columns: [
            { text: '', width: '50%' },
            {
              text: 'Total (TZS): ' + this.getTzFormatCurrency(this.bondItemGoodReleaseTotal),
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
    //await this.getBondItemGoodRelease(id)
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
              [{ text: 'Client Name: ' + this.bondItemGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.bondItemGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.bondItemGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
              [{ text: 'Check In Date: ' + this.bondItemGoodCheckedInDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Date: ' + this.bondItemGoodReleaseReleaseDate.substring(0, 10), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Released Time: ' + this.bondItemGoodReleaseReleaseDate.substring(11), alignment: 'left', fontSize: 9, bold: true }],
              [{ text: 'Number of Days: ' + this.noOfDays, alignment: 'left', fontSize: 9, bold: true }],
              //[{text : 'Checkout At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
              [{ text: 'Day Out: ' + Date(), alignment: 'left', fontSize: 9, bold: true }],
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
    await this.getBondItemGoodRelease(id)
    await this.printGatePassNote(this.billItems, this.bondItemGoodReleaseNo, 0, id)
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
// class BondItemCustomBillDetail{
// 	double totalQty;
// 	double billedQty;
// 	double unbilledQty;
// 	double billingRate;
// }

interface IBondItemCustomBillDetail {
  totalQty: number
  billedQty: number
  unbilledQty: number
  billingRate: number
  noOfDays: number
  billingType: string
}


interface IBondItemGoodReleaseDetail {
  id: any
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IBondItemGoodRelease {
  id: any
  no: string
  qty: number
  noOfDays: number
  status: string
  bondItemId: any
  checkedInDate: string
  releaseDate: string
  clientName: string
  clientAddress: string
  clientPhoneNo: string
  bondItemName: string
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
