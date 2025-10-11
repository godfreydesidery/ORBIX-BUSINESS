import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { environment } from 'src/environments/environment';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { ReceiptItem } from 'src/app/domain/receipt-item';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { IMaintenanceJobCardIssueBillReceivable } from 'src/app/domain/maintenance-job-card-issue-bill-receivable';
import { IBillReceivable } from 'src/app/domain/bill-receivable';
import { IMaintenance, IServiceBillItem } from 'src/app/domain/maintenance';
import { ICustomer } from 'src/app/domain/customer';

var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-maintenance-vehicle-equipment-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './maintenance-vehicle-equipment-billing.component.html',
  styleUrl: './maintenance-vehicle-equipment-billing.component.scss'
})
export class MaintenanceVehicleEquipmentBillingComponent {
// Maintenance attributes
  maintenanceId : any = null
  maintenanceNo : string = ''
  maintenanceJobCardIssueBillReceivableId : any = null
  maintenanceJobCardIssueBillReceivableDescription : string = ''
  maintenanceJobCardIssueBillReceivableStartingDate : Date | null
  maintenanceJobCardIssueBillReceivableEndingDate : Date | null
  maintenanceJobCardIssueBillReceivablePrice : number = 0
  maintenanceJobCardIssueBillReceivableQty : number = 0
  maintenanceJobCardIssueBillReceivableDiscount : number = 0
  maintenanceJobCardIssueBillReceivableAmount : number = 0
  maintenanceJobCardIssueBillReceivableStatus : string = ''

  // Service attributes
  serviceBillReceivableId : any = null
  serviceBillReceivableDate : Date | null
  serviceBillReceivableDescription : string = ''
  serviceBillReceivablePrice : number = 0
  serviceBillReceivableQty : number = 0
  serviceBillReceivableDiscount : number = 0
  serviceBillReceivableStatus : string = ''
  serviceBillReceivableAmount : number = 0

  //Maintenance and bills Collections attributes
  maintenanceJobCardIssueBillReceivables : IMaintenanceJobCardIssueBillReceivable[] = []
  billReceivables : IBillReceivable[] = []
  documentHeader: any;
  invoice: any;

  cash : number = 0

  mpesa : number = 0
  mpesaRefNo : string = ''



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

  validUntilDate : Date | null = new Date()

  comments : string = ''

  cardNo : string = ''

  billingType : string = ''
  billingAmount : number = 0
  //image: Byte[]

  status: string = "PENDING"

  startBillingAt : Date | null

  vehicleEquipmentTypeId: any = ''
  vehicleEquipmentTypeName : string = ''
  branchId: any = ''
  companyId: any = ''

  warehouseId: any = null
  warehouseName : string = ''

  vehicleEquipmentName : string = ''


  ////////////////////////////////////////



  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private data : DataService,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.maintenanceId = params['maintenance_id'] 
    })
    this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
    //this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  }


  async getMaintenanceBill(id : any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IMaintenanceJobCardIssueBillReceivable>(API_URL+'/maintenance_bill_receivables/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.maintenanceJobCardIssueBillReceivableId = data!.id
        this.maintenanceJobCardIssueBillReceivablePrice = data!.price
        this.maintenanceJobCardIssueBillReceivableQty = data!.qty
        this.maintenanceJobCardIssueBillReceivableDiscount = data!.discount
        this.maintenanceJobCardIssueBillReceivableAmount = data!.price
        this.maintenanceJobCardIssueBillReceivableStatus = data!.payStatus
        
        console.log(data)
      }
    )

  }



  async saveMaintenanceBill() { 
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to save this maintenance bill?', 'question', 'Yes', 'No') == false){
        return
      }
     
    // If maintenance bill receivable id is null, create new maintenance bill receivable
    // If maintenance bill receivable id is not null, update maintenance bill receivable
    if(this.maintenanceId != null) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      var maintenanceBill = {
        id: this.maintenanceJobCardIssueBillReceivableId,
        description: this.maintenanceJobCardIssueBillReceivableDescription,
        startedAt: this.maintenanceJobCardIssueBillReceivableStartingDate,
        endedAt: this.maintenanceJobCardIssueBillReceivableEndingDate,
        price: this.maintenanceJobCardIssueBillReceivablePrice,
        qty: this.maintenanceJobCardIssueBillReceivableQty,
        discount: this.maintenanceJobCardIssueBillReceivableDiscount,
        maintenanceId : this.maintenanceId
      }

      if(this.maintenanceJobCardIssueBillReceivableId == null) {
      // Create new maintenance bill receivable
      await this.http.post<IMaintenanceJobCardIssueBillReceivable>(API_URL+'/maintenance_bill_receivables/create_maintenance_bill_receivable?maintenance_id=' + this.maintenanceId, maintenanceBill, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Maintenance bill created successfully')
            this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
            console.log(error)
          }
        )
      }else{
        await this.http.post<IMaintenanceJobCardIssueBillReceivable>(API_URL+'/maintenance_bill_receivables/update_maintenance_bill_receivable?maintenance_id=' + this.maintenanceId, maintenanceBill, options)
        .toPromise()
        .then(
          data => {        
            this.msg.showSuccessMessage('Maintenance bill updated successfully')
            this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
            console.log(data)
          }
        )
        .catch(
          error => {
            this.msg.showErrorMessage(error, 'Error')
            this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
            console.log(error)
          }
        )
      }  
    }else{
      this.msg.showErrorMessage3('No maintenance available')
    }
  }

  // async saveServiceBill() {
  //   // If service bill receivable id is null, create new service bill receivable
  //   // If service bill receivable id is not null, update service bill receivable
  //   if(this.maintenanceId != null) {
  //     let options = {
  //       headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //     }

  //     var serviceBill = {
  //       id: this.serviceBillReceivableId,
  //       serviceDate: this.serviceBillReceivableDate,
  //       description: this.serviceBillReceivableDescription,
  //       price: this.serviceBillReceivablePrice,
  //       qty: this.serviceBillReceivableQty,
  //       discount: this.serviceBillReceivableDiscount,
  //       maintenanceId : this.maintenanceId
  //     }

  //     if(this.serviceBillReceivableId == null) {
  //       // Create new service bill receivable
  //       await this.http.post<IServiceBillReceivable>(API_URL+'/maintenance_bill_receivables/create_service_bill_receivable?maintenance_id=' + this.maintenanceId, serviceBill, options)
  //       .toPromise()
  //       .then(
  //         data => {
  //           this.msg.showSuccessMessage('Service bill created successfully')
  //           this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //           console.log(data)
  //         }
  //       )
  //       .catch(
  //         error => {
  //           this.msg.showErrorMessage(error, 'Error')
  //           this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //           console.log(error)
  //         }
  //       )
  //     }else{
  //       // Update service bill receivable
  //       await this.http.post<IServiceBillReceivable>(API_URL+'/maintenance_bill_receivables/update_service_bill_receivable?maintenance_id=' + this.maintenanceId, serviceBill, options)
  //       .toPromise()
  //       .then(
  //         data => {        
  //           this.msg.showSuccessMessage('Service bill updated successfully')
  //           this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //           console.log(data)
  //         }
  //       )
  //       .catch(
  //         error => {
  //           this.msg.showErrorMessage(error, 'Error')
  //           this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //           console.log(error)
  //         }
  //       )
  //     }
      
  //   }else{
  //     this.msg.showErrorMessage3('No maintenance available')
  //   }
  // }


  // async deleteServiceBill(id : any) {
  //     if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to delete this service bill?', 'question', 'Yes', 'No') == false){
  //       return
  //     }

  //   if(this.maintenanceId != null) {
  //     let options = {
  //       headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //     }

  //     var serviceBill = {
  //       id: id,
  //       maintenanceId : this.maintenanceId
  //     }

  //     await this.http.post<IServiceBillReceivable>(API_URL+'/maintenance_bill_receivables/delete_service_bill_receivable', serviceBill, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         this.msg.showSuccessMessage('Service bill deleted successfully')
  //         this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //         console.log(data)
  //       }
  //     )
  //     .catch(
  //       error => {
  //         this.msg.showErrorMessage(error, 'Error')
  //         this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  //         console.log(error)
  //       }
  //     )
  //   }else{
  //     this.msg.showErrorMessage3('No maintenance available')
  //   }
  // }

  // async getServiceBill(id : any) {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }

  //   await this.http.get<IServiceBillReceivable>(API_URL+'/maintenance_service_bill_receivables/get?id=' + id, options)
  //   .toPromise()
  //   .then(
  //     data => {
  //       this.serviceBillReceivableId = data!.id
  //       this.serviceBillReceivableDate = data!.serviceDate
  //       this.serviceBillReceivableDescription = data!.description
  //       this.serviceBillReceivablePrice = data!.price
  //       this.serviceBillReceivableQty = data!.qty
  //       this.serviceBillReceivableDiscount = data!.discount
  //       console.log(data)
  //     }
  //   )

  // }

  async getMaintenanceJobCardIssueBillReceivables(maintenanceId : any) {
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.maintenanceJobCardIssueBillReceivables = []

    await this.http.get<IMaintenanceJobCardIssueBillReceivable[]>(API_URL+'/maintenance_bill_receivables/get_all_by_maintenance?maintenance_id=' + maintenanceId, options)
    .toPromise()
    .then(
      data => {
        
        data?.forEach(element => {
          this.maintenanceJobCardIssueBillReceivables.push(element) 
        })
        var sn = 1
        this.maintenanceJobCardIssueBillReceivables.reverse().forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        console.log(data)
      }
    )
  }

  // async getMaintenanceServiceBillReceivables(maintenanceId : any) {
    
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }
  //   this.serviceBillReceivables = []

  //   await this.http.get<IServiceBillReceivable[]>(API_URL+'/service_bill_receivables/get_all_by_maintenance?maintenance_id=' + maintenanceId, options)
  //   .toPromise()
  //   .then(
  //     data => {
        
  //       data?.forEach(element => {
  //         this.serviceBillReceivables.push(element)
  //       })
  //       var sn = 1
  //       this.serviceBillReceivables.reverse().forEach(element => {
  //         element.sn = sn
  //         sn = sn + 1
  //       })
  //       console.log(data)
  //     }
  //   )
  // }

  totalBillReceivable : number = 0
  balance : number = 0
  tender : number = 0
  async getBillReceivables(maintenanceId : any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.billReceivables = [] 
    this.totalBillReceivable = 0

    await this.http.get<IBillReceivable[]>(API_URL+'/bill_receivables/get_all_by_maintenance?maintenance_id=' + maintenanceId, options)
    .toPromise()
    .then(
      data => {
        data?.forEach(element => {
          if(element.payStatus == 'UNPAID'){ 
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
    this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
   // this.getMaintenanceServiceBillReceivables(this.maintenanceId)
  }

  

  getUnpaidBills() { }  

  confirmBillPayment() { }

  async refreshBillReceivables() {
    this.billReceivables = []
    await this.getBillReceivables(this.maintenanceId)
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

  billReceivableCollections : IBillReceivableCollection[] = []

  payCode = 'CASH'
  payRefNo = ''
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
    await this.http.post<IBillReceivable>(API_URL+'/bill_receivables/confirm_bills_payment?total_amount='+this.totalBillReceivable + '&pay_code=' + this.payCode + '&pay_ref_no=' + this.payRefNo, this.billReceivables, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {
        console.log(data)
        //this.msgBox.showSuccessMessage('Payment successiful')
        this.msg.showSuccessMessage('Payment successiful')
        this.printReceipt()

        this.getMaintenanceJobCardIssueBillReceivables(this.maintenanceId)
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

  clearMaintenanceBill(){
    this.maintenanceJobCardIssueBillReceivableId = null
    this.maintenanceJobCardIssueBillReceivableDescription = ''
    this.maintenanceJobCardIssueBillReceivableAmount = 0
    this.maintenanceJobCardIssueBillReceivableQty = 0
    this.maintenanceJobCardIssueBillReceivableDiscount = 0
    this.maintenanceJobCardIssueBillReceivableStartingDate = null
    this.maintenanceJobCardIssueBillReceivableEndingDate = null
  }

  clearServiceBill(){
    this.serviceBillReceivableId = null
    this.serviceBillReceivableDate = null
    this.serviceBillReceivableDescription = ''
    this.serviceBillReceivableAmount = 0
    this.serviceBillReceivablePrice = 0
    this.serviceBillReceivableQty = 0
    this.serviceBillReceivableDiscount = 0
  }


  async checkOut(): Promise<void>{


    if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false){
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var maintenance = {
      id : this.maintenanceId
      // cardNo : this.cardNo,
      // maintenanceZoneName : this.maintenanceZoneName,
      // startBillingAt : this.startBillingAt
    }

    await this.http.post<IMaintenance>(API_URL+'/maintenances/check_out', maintenance, options)
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

    var customer: ICustomer = {
          name: this.ownerFirstName + ' ' + this.ownerLastName,
          address: this.ownerAddress,
          phone: this.ownerPhoneNo
        }

    this.printer.print(items, 'NA', 0, customer)
    this.toPrintReceipt = false
  }

  refreshMaintenanceAmounts(){
    /////////////////////////////////////////
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

    //var address : any = await this.data.getReceiptHeader(receiptNo)
    var address : any = await this.data.getBranchReceiptHeader(receiptNo)
   
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




   lastBillingDate : string = ''

  async getLastBillingDate(maintenanceId : any){
    // this.lastBillingDate = ''
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IModel>(API_URL+'/maintenances/get_last_maintenance_bill_date?id=' + maintenanceId, options)
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

  async get(id : any){

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<IMaintenance>(API_URL+'/maintenances/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.startBillingAt = null
        this.showMaintenanceData(data!)
        console.log(data)
      }
    )
  }


  ///////////////////////////////////////
  showMaintenanceData(data : IMaintenance){

    this.maintenanceId = data?.id
    this.maintenanceNo = data?.no

    this.ownerFirstName = data?.ownerFirstName;
    this.ownerMiddleName = data?.ownerMiddleName;
    this.ownerLastName = data?.ownerLastName;
    this.ownerCompanyName = data?.ownerCompanyName;
    this.ownerIdNo = data?.ownerIdNo;
    this.ownerIdType = data?.ownerIdType;
    this.ownerPhoneNo = data?.ownerPhoneNo;
    this.ownerEmail = data?.ownerEmail;
    this.ownerAddress = data?.ownerAddress;

    

    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName,

    this.vehicleEquipmentName = data!.vehicleEquipmentName

     this.status = data!.status

     this.validUntilDate = null

     this.comments = '' // check this

  }

  clearMaintenanceData(){
    this.maintenanceId = null
    this.maintenanceNo = ''
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
   
    this.vehicleEquipmentTypeId = null
    this.vehicleEquipmentTypeName = ''

    this.vehicleEquipmentName = ''

    this.warehouseId = null
    this.warehouseName = ''

    this.billingType = ''
  }



  ///////////////////////////////////


  printGatePassRcpt = async (billItems : IServiceBillItem[], receiptNo :string, cash : number) => {

    await this.get(this.maintenanceId)
    await this.getLastBillingDate(this.maintenanceId)

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title  = 'Gate Pass'
    var total : number = 0
    var discount : number = 0
    var tax : number = 0

    // var address : any = await this.data.getReceiptHeader(receiptNo)
    var address : any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(receiptNo)

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
        {text : 'SN', fontSize : 8, bold : true}, 
        {text : 'Item', fontSize : 8, bold : true},
        {text : 'Qty', fontSize : 8, bold : true},
        {text : 'Amount', fontSize : 8, bold : true},
      ]
    ] 
    
    var sn = 0

    billItems.forEach((element) => {
      total = total + (+element.amount)
      sn = sn + 1
      var item = [
        {text : sn.toString(), fontSize : 8, bold : false}, 
        {text : element.item, fontSize : 8, bold : false},
        {text : element.qty.toString(), fontSize : 8, bold : false},
        {text : (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 8, alignment : 'right', bold : false},
      ]
      receipt.push(item)
    })
    var detailSummary = [
      {text : ' ', fontSize : 8, bold : false},
      {text : 'Total', fontSize : 9, bold : true},
      {text : ' ', fontSize : 8, bold : false},
      {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', bold : true},
    ]
    receipt.push(detailSummary)
    

    const docDefinition = {
      header: '',
      
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
        content : [
          {
            layout : 'noBorders',
            table : address
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
                [{text : 'Gate Pass', alignment : 'center', fontSize : 9, bold : true}],
                [{text : 'Vehicle Name: ' + this.vehicleEquipmentTypeName, alignment : 'left', fontSize : 9, bold : false}],
                [{text : '________________________________'}],
                [{text : 'Payment Details', alignment : 'center', fontSize : 9, bold : true}],
                [{text : ' ', alignment : 'center', fontSize : 9, bold : true}],
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
              widths : [200],
              body : [
                [{text : ' '}],
                [{text : 'Cashier Comments', alignment : 'left', fontSize : 9, bold : true}],
                [{text : this.comments, alignment : 'left', fontSize : 9, bold : false}],
                [{text : ' '}],
                [{text : ' '}],
                [{text : 'Issued At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                [{text : 'Checkout At: ' + new Date().toString(), alignment : 'left', fontSize : 9, bold : true}],
                [{text : 'Day Out: ' + this.lastBillingDate, alignment : 'left', fontSize : 9, bold : true}],
                [{text : ' '}],
                [{text : 'Gate Pass issued By: ' + localStorage.getItem('user-name'), alignment : 'left', fontSize : 9, bold : true}],
                [{text : ' '}],
                [{text : 'Signature: ......................'}],
              ]
            }
          },   
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
                [{text : 'Developed By @Davaghana', fontSize : 10, bold : true, alignment : 'center'}],
                [{text : '***End of Document***', fontSize : 9, alignment : 'center'}]
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

export interface IBillSummary {
  billReceivables : IBillReceivable[]
  billReceivableCollections : IBillReceivableCollection[]
}

export interface IBillReceivableCollection {
  amount : number
  payCode : string 
  refNo : string 
}

interface IModel{
  stringData : string
}