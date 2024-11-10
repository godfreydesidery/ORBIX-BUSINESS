import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IBillReceivable, IParkingBillReceivable, IServiceBillReceivable } from 'src/app/domain/bill-receivable';
import { IParking } from 'src/app/domain/parking';
import { environment } from 'src/environments/environment';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';
import { MsgBoxService } from '@services/custom/msg-box.service';

var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;


@Component({
  selector: 'az-vehicle-equipment-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './vehicle-equipment-billing.component.html',
  styleUrl: './vehicle-equipment-billing.component.scss'
})
export class VehicleEquipmentBillingComponent {

  // Parking attributes
  parkingId : any = null
  parkingBillReceivableId : any = null
  parkingBillReceivableDescription : string = ''
  parkingBillReceivableStartingDate : Date | null
  parkingBillReceivableEndingDate : Date | null
  parkingBillReceivablePrice : number = 0
  parkingBillReceivableQty : number = 0
  parkingBillReceivableDiscount : number = 0
  parkingBillReceivableAmount : number = 0
  parkingBillReceivableStatus : string = ''

  // Service attributes
  serviceBillReceivableId : any = null
  serviceBillReceivableDate : Date | null
  serviceBillReceivableDescription : string = ''
  serviceBillReceivablePrice : number = 0
  serviceBillReceivableQty : number = 0
  serviceBillReceivableDiscount : number = 0
  serviceBillReceivableStatus : string = ''
  serviceBillReceivableAmount : number = 0

  //Parking and bills Collections attributes
  parkingBillReceivables : IParkingBillReceivable[] = []
  serviceBillReceivables : IServiceBillReceivable[] = []
  billReceivables : IBillReceivable[] = []
  documentHeader: any;
  data: any;
  invoice: any;


  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.parkingId = params['parking_id'] 
    })
    this.getParkingBillReceivables(this.parkingId)
    this.getParkingServiceBillReceivables(this.parkingId)
  }


  async getParkingBill(id : any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IParkingBillReceivable>(API_URL+'/parking_bill_receivables/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.parkingBillReceivableId = data!.id
        this.parkingBillReceivableDescription = data!.description
        this.parkingBillReceivableStartingDate = data!.startedAt
        this.parkingBillReceivableEndingDate = data!.endedAt
        this.parkingBillReceivablePrice = data!.price
        this.parkingBillReceivableQty = data!.qty
        this.parkingBillReceivableDiscount = data!.discount
        this.parkingBillReceivableAmount = data!.amount
        this.parkingBillReceivableStatus = data!.status
        
        console.log(data)
      }
    )

  }



  async saveParkingBill() { 
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to save this parking bill?', 'question', 'Yes', 'No') == false){
        return
      }
     
    // If parking bill receivable id is null, create new parking bill receivable
    // If parking bill receivable id is not null, update parking bill receivable
    if(this.parkingId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var parkingBill = {
        id: this.parkingBillReceivableId,
        description: this.parkingBillReceivableDescription,
        startedAt: this.parkingBillReceivableStartingDate,
        endedAt: this.parkingBillReceivableEndingDate,
        price: this.parkingBillReceivablePrice,
        qty: this.parkingBillReceivableQty,
        discount: this.parkingBillReceivableDiscount,
        parkingId : this.parkingId
      }

      if(this.parkingBillReceivableId == null) {
      // Create new parking bill receivable
      await this.http.post<IParkingBillReceivable>(API_URL+'/parking_bill_receivables/create_parking_bill_receivable?parking_id=' + this.parkingId, parkingBill, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Parking bill created successfully')
            this.getParkingBillReceivables(this.parkingId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getParkingBillReceivables(this.parkingId)
            console.log(error)
          }
        )
      }else{
        await this.http.post<IParkingBillReceivable>(API_URL+'/parking_bill_receivables/update_parking_bill_receivable?parking_id=' + this.parkingId, parkingBill, options)
        .toPromise()
        .then(
          data => {        
            this.msg.showSuccessMessage('Parking bill updated successfully')
            this.getParkingBillReceivables(this.parkingId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getParkingBillReceivables(this.parkingId)
            console.log(error)
          }
        )
      }  
    }else{
      this.msg.showErrorMessage3('No parking available')
    }
  }

  async saveServiceBill() {
    // If service bill receivable id is null, create new service bill receivable
    // If service bill receivable id is not null, update service bill receivable
    if(this.parkingId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var serviceBill = {
        id: this.serviceBillReceivableId,
        serviceDate: this.serviceBillReceivableDate,
        description: this.serviceBillReceivableDescription,
        price: this.serviceBillReceivablePrice,
        qty: this.serviceBillReceivableQty,
        discount: this.serviceBillReceivableDiscount,
        parkingId : this.parkingId
      }

      if(this.serviceBillReceivableId == null) {
        // Create new service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL+'/parking_bill_receivables/create_service_bill_receivable?parking_id=' + this.parkingId, serviceBill, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Service bill created successfully')
            this.getParkingServiceBillReceivables(this.parkingId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getParkingServiceBillReceivables(this.parkingId)
            console.log(error)
          }
        )
      }else{
        // Update service bill receivable
        await this.http.post<IServiceBillReceivable>(API_URL+'/parking_bill_receivables/update_service_bill_receivable?parking_id=' + this.parkingId, serviceBill, options)
        .toPromise()
        .then(
          data => {        
            this.msg.showSuccessMessage('Service bill updated successfully')
            this.getParkingServiceBillReceivables(this.parkingId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getParkingServiceBillReceivables(this.parkingId)
            console.log(error)
          }
        )
      }
      
    }else{
      this.msg.showErrorMessage3('No parking available')
    }
  }


  async deleteServiceBill(id : any) {
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to delete this service bill?', 'question', 'Yes', 'No') == false){
        return
      }

    if(this.parkingId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var serviceBill = {
        id: id,
        parkingId : this.parkingId
      }

      await this.http.post<IServiceBillReceivable>(API_URL+'/parking_bill_receivables/delete_service_bill_receivable', serviceBill, options)
      .toPromise()
      .then(
        data => {
          this.msg.showSuccessMessage('Service bill deleted successfully')
          this.getParkingServiceBillReceivables(this.parkingId)
          console.log(data)
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Error')
          this.getParkingServiceBillReceivables(this.parkingId)
          console.log(error)
        }
      )
    }else{
      this.msg.showErrorMessage3('No parking available')
    }
  }

  async getServiceBill(id : any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IServiceBillReceivable>(API_URL+'/parking_service_bill_receivables/get?id=' + id, options)
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

  async getParkingBillReceivables(parkingId : any) {
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.parkingBillReceivables = []

    await this.http.get<IParkingBillReceivable[]>(API_URL+'/parking_bill_receivables/get_all_by_parking?parking_id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        
        data?.forEach(element => {
          this.parkingBillReceivables.push(element) 
        })
        var sn = 1
        this.parkingBillReceivables.reverse().forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  async getParkingServiceBillReceivables(parkingId : any) {
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.serviceBillReceivables = []

    await this.http.get<IServiceBillReceivable[]>(API_URL+'/service_bill_receivables/get_all_by_parking?parking_id=' + parkingId, options)
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

  totalBillReceivable : number = 0
  balance : number = 0
  tender : number = 0
  async getBillReceivables(parkingId : any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.billReceivables = [] 
    this.totalBillReceivable = 0

    await this.http.get<IBillReceivable[]>(API_URL+'/bill_receivables/get_all_by_parking?parking_id=' + parkingId, options)
    .toPromise()
    .then(
      data => {
        
        data?.forEach(element => {
          if(element.status == 'UNPAID'){ 
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

  refresh(){
    this.getParkingBillReceivables(this.parkingId)
    this.getParkingServiceBillReceivables(this.parkingId)
  }

  

  getUnpaidBills() { }  

  confirmBillPayment() { }

  refreshBillReceivables() {
    this.getBillReceivables(this.parkingId)
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

  toPrintReceipt : boolean = false

  receiptData : IBillReceivable [] = []

  async confirmBillsPayment(){

    this.toPrintReceipt = false

   
    if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to confirm this payment?', 'question', 'Yes', 'No') == false){
      return
    }
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.receiptData = this.billReceivables

    //this.spinner.show()
    await this.http.post<IBillReceivable>(API_URL+'/bill_receivables/confirm_bills_payment?total_amount='+this.totalBillReceivable, this.billReceivables, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {
        console.log(data)
        //this.msgBox.showSuccessMessage('Payment successiful')
        this.msg.showSuccessMessage('Payment successiful')
        //this.printReceipt()

        this.getParkingBillReceivables(this.parkingId)
        this.getParkingServiceBillReceivables(this.parkingId)
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

  clearParkingBill(){
    this.parkingBillReceivableId = null
    this.parkingBillReceivableDescription = ''
    this.parkingBillReceivableAmount = 0
    this.parkingBillReceivableQty = 0
    this.parkingBillReceivableDiscount = 0
    this.parkingBillReceivableStartingDate = null
    this.parkingBillReceivableEndingDate = null
  }



  printReceipt(){

    if(this.toPrintReceipt == false){
      return
    }

    if(this.receiptData.length == 0){
      this.msg.showErrorMessage3('No data to print')
      return
    }

    var items : ReceiptItem[] = []
    var item : ReceiptItem

    this.receiptData.forEach(element => {
      item = new ReceiptItem()
      item.code = element.id
      item.name = element.summary
      item.amount = element.amount
      item.qty = parseFloat(element.qty)
      items.push(item)
    })

    this.printer.print(items, 'NA', 0)
    this.toPrintReceipt = false
  }

  clearReceipt(){
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
    print = async ( receiptNo :string, cash : number) => {

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title  = 'Receipt'
    var total : number = 0
    var discount : number = 0
    var tax : number = 0

    var address : any = await this.data.getReceiptHeader(receiptNo)
   
    var receipt = [
      [
        {text : 'SN', fontSize : 8, bold : true}, 
        {text : 'Item', fontSize : 8, bold : true},
        {text : 'Qty', fontSize : 8, bold : true},
        {text : 'Amount', fontSize : 8, bold : true},
      ]
    ] 
    
    var sn = 0

    this.billReceivables.forEach((element) => {
      total = total + (+element.amount)
      sn = sn + 1
      var item = [
        {text : sn.toString(), fontSize : 8, bold : false}, 
        {text : element.summary, fontSize : 8, bold : false},
        {text : element.qty.toString(), fontSize : 8, bold : false},
        {text : (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 8, alignment : 'right', bold : false},
      ]
      receipt.push(item)
    })
    var detailSummary = [
      {text : ' ', fontSize : 8, bold : false},
      {text : 'Total', fontSize : 9, bold : true},
      {text : ' ', fontSize : 8, bold : false},
      {text : (+total).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', bold : true},
    ]
    receipt.push(detailSummary)
    

    const docDefinition = {
      header: '',
      
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
        content : [
          {
            layout : 'noBorders',
            table :  address

          }, 
          
          
          
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
              ]
            }
          }, 
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                // [{text : patient?.firstName + ' '+ patient?.middleName + ' '+ patient?.lastName , fontSize : 8}],
                // [{text : patient?.no, fontSize : 8}],
                // [{text : patient?.address, fontSize : 8}],
                // [{text : patient?.phoneNo, fontSize : 8}],
                // [{text : '________________________________',alignment : 'center',}],
                [{text : '' , fontSize : 8}],
                [{text : '', fontSize : 8}],
                [{text : '', fontSize : 8}],
                [{text : '', fontSize : 8}],
                [{text : '________________________________',alignment : 'center',}],
              ]
            }
          },  
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                [{text : 'Receipt', alignment : 'center', fontSize : 9, bold : true}],
              ]
            }
          },      
          {
            layout : 'noBorders',
            table : {
                headerRows : 1,
                widths : [15, 100, 15, 50],
                body : receipt
            }
          },
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
                [{text : 'Served By : '+ localStorage.getItem('user-name'), fontSize : 9, alignment : 'left'}],
                [{text : 'Developed By @Orbix Systems', fontSize : 10, bold : true, alignment : 'center'}],
                [{text : '***End of Receipt***', fontSize : 9, alignment : 'center'}]
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
