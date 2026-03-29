import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
//import { IBondItem } from 'src/app/domain/bondItem';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { BrowserModule } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { IBondItemBillReceivable } from 'src/app/domain/bill-receivable';
import { IBillView } from 'src/app/domain/bill-view';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem } from 'src/app/domain/maintenance';
import * as pdfMake from 'pdfmake/build/pdfmake';
import { IBondItem } from 'src/app/domain/bond-item';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-bond-billing',
  standalone: true,
  imports: [
    FormsModule,
        CommonModule,
        SearchFilterPipe,
        NgxPaginationModule,
        RouterModule
  ],
  templateUrl: './bond-billing.component.html',
  styleUrl: './bond-billing.component.scss'
})
export class BondBillingComponent {
page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  startedAt: Date | null
  endedAt: Date | null
  billingType: string
  qty: number
  price: number
  discount: number

  bondItemId: any

  autoBilling: any = 1

  bondItemAmount: number

  billingStartAt: string = ''
  ////////////////////////////////////////////

  vehicleEquipmentTypeName: string = ''


  // BondItem attributes
  // bondItemId : any = null
  bondItemNo: string = ''
  bondItemBillReceivableId: any = null
  bondItemBillReceivableDescription: string = ''
  bondItemBillReceivableStartingDate: Date | null
  bondItemBillReceivableEndingDate: Date | null
  bondItemBillReceivablePrice: number = 0
  bondItemBillReceivableQty: number = 0
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

  bondItemCategory: string = ''

  billingAmount: number = 0
  //image: Byte[]

  status: string = "PENDING"



  startBillingAt: Date | null


  bondItemName: string = ''
  bondItemTypeId: any = ''
  bondItemTypeName: string = ''
  branchId: any = ''
  companyId: any = ''

  bondZoneName: string = ''

  initialQty: number = 0
  currentQty: number = 0





  ////////////////////////////////////////




  /**Collections */
  bondItems: IBondItem[] = []
  // bondItemBillReceivables : IBondItemBillReceivable[] = []

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private msg: MsgBoxService,
    private data: DataService
  ) { }


  ngOnInit() {
    this.getAllCheckedInBondItems()
  }

  async getAllCheckedInBondItems() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.bondItems = []

    await this.http.get<IBondItem[]>(API_URL + '/bond_items/get_all_checked_in', options)
      .toPromise()
      .then(
        data => {
          data?.reverse()
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.bondItems.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  async getBondItemBillReceivables(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.bondItemBillReceivables = []

    await this.http.get<IBondItemBillReceivable[]>(API_URL + '/bond_items/get_bond_item_bill_receivables?bond_tem_id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.bondItemBillReceivables.push(element)
            sn = sn + 1
          })
          this.qty = 0
          this.price = 0
          this.discount = 0
          this.bondItemAmount = 0
          this.getBondItem(bondItemId)
          console.log(data)
        }
      )
  }

  async getBondItem(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IBondItem>(API_URL + '/bond_items/get?id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {
          this.bondItemId = data!.id
          this.billingType = data!.billingType
          this.price = data!.billingAmount
        }
      )
  }

  async createBondItemBillReceivable() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var bondItemBillReceivable = {
      startedAt: this.startedAt,
      endedAt: this.endedAt,
      billingType: this.billingType,
      qty: this.qty,
      price: this.price,
      discount: this.discount,
      autoBilling: this.autoBilling,
      bondItemId: this.bondItemId
    }

    await this.http.post<IBondItemBillReceivable>(API_URL + '/bond_items/create_bond_item_bill_receivable', bondItemBillReceivable, options)
      .toPromise()
      .then(
        data => {
          this.getBondItemBillReceivables(this.bondItemId)
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

  clearBondItemBill() {
    this.startedAt = null
    this.endedAt = null
    this.qty = 0
    this.price = 0
    this.discount = 0
    this.bondItemId = null
    this.autoBilling = 1
    this.bondItemAmount = 0
  }

  doAutoBilling() {
    this.clearBondItemBill()
  }

  doCustomBilling() {
    this.autoBilling = 0
  }

  refreshBondItemAmounts() {
    this.bondItemAmount = (this.price * this.qty) - this.discount
  }


  async bondItemBilling(bondItemId: any) {

    localStorage.setItem('bondItem-id', '');
    localStorage.setItem('bondItem-id', bondItemId);

    await this.router.navigate(['app/accounts-and-finance/bond-item-billing'], {
      queryParams: { bond_item_id: bondItemId }
    });

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

  showBondItemData(data: IBondItem) {

    this.bondItemId = data?.id
    this.bondItemNo = data?.no

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

    this.bondItemName = data!.bondItemName

    this.bondItemTypeName = data!.bondItemTypeName
    this.bondZoneName = data!.bondZoneName
    this.billingType = data!.billingType
    this.status = data!.status
    this.validUntilDate = null
    this.comments = data!.comments// check this

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
    this.billingStartAt = ''
    this.bondItemTypeName = ''

    this.initialQty = 0
    this.currentQty = 0

    this.bondItemName = ''

    this.bondItemCategory = ''

    this.bondZoneName = ''
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

    await this.http.get<IBondItemGoodReleaseDetail>(API_URL + '/bond_item_releases/get_bond_item_release_detail?bond_item_id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {

          this.currentBondItemId = bondItemId

          this.originalQty = data!.initialQty
          this.availableQty = data!.currentQty
          this.releasedQty = data!.releasedQty
          this.availableForRelease = data!.availableForRelease

          this.qtyToRelease = 0

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

    await this.http.get<IBillView>(API_URL + '/bond_item_bill_receivables/get_bill_view?bond_item_id=' + id, options)
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

    var bondItem = {
      id: id
    }

    await this.http.post<IBondItem>(API_URL + '/bond_items/check_out', bondItem, options)
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
      this.getAllCheckedInBondItems()
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


interface IBondItemGoodReleaseDetail {
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IBondItemGoodRelease {
  id: any
  no: string
  qty: number
  status: string
  bondItemId: any
  releaseDate: string
  clientName: string
  clientAddress: string
  clientPhoneNo: string
  bondItemName: string
  unitPrice: string
  total: string
}

interface IModel {
  stringData: string
}


