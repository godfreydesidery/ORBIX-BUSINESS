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
import { IMachineServiceBillReceivable } from 'src/app/domain/bill-receivable';
import { IBillView } from 'src/app/domain/bill-view';
import { DataService } from '@services/custom/data.service';
import { IServiceBillItem } from 'src/app/domain/maintenance';
import * as pdfMake from 'pdfmake/build/pdfmake';
import { IMachine } from 'src/app/domain/machine';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-service-bay-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './service-bay-billing.component.html',
  styleUrl: './service-bay-billing.component.scss'
})
export class ServiceBayBillingComponent {
page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  startedAt: Date | null
  endedAt: Date | null
  billingType: string
  qty: number
  price: number
  discount: number

  machineId: any

  autoBilling: any = 1

  weighAmount: number

  billingStartAt: string = ''
  ////////////////////////////////////////////

  vehicleEquipmentTypeName: string = ''


  // MachineService attributes
  // machineId : any = null
  weighNo: string = ''
  machineServiceBillReceivableId: any = null
  machineServiceBillReceivableDescription: string = ''
  machineServiceBillReceivableStartingDate: Date | null
  machineServiceBillReceivableEndingDate: Date | null
  machineServiceBillReceivablePrice: number = 0
  machineServiceBillReceivableQty: number = 0
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
  machines: IMachine[] = []
  // machineServiceBillReceivables : IMachineServiceBillReceivable[] = []

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private msg: MsgBoxService,
    private data: DataService
  ) { }


  ngOnInit() {
    this.getAllCheckedInMachines()
  }

  async getAllCheckedInMachines() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.machines = []

    await this.http.get<IMachine[]>(API_URL + '/machines/by_branch', options)
      .toPromise()
      .then(
        data => {
          data?.reverse()
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.machines.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  async getMachineServiceBillReceivables(machineId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.machineServiceBillReceivables = []

    await this.http.get<IMachineServiceBillReceivable[]>(API_URL + '/machines/get_machine_service_bill_receivables?machine_id=' + machineId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.machineServiceBillReceivables.push(element)
            sn = sn + 1
          })
          this.qty = 0
          this.price = 0
          this.discount = 0
          this.weighAmount = 0
          this.getMachine(machineId)
          console.log(data)
        }
      )
  }

  async getMachine(machineId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IMachine>(API_URL + '/machines/get?id=' + machineId, options)
      .toPromise()
      .then(
        data => {
          this.machineId = data!.id
        }
      )
  }

  async createMachineServiceBillReceivable() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var machineServiceBillReceivable = {
      startedAt: this.startedAt,
      endedAt: this.endedAt,
      billingType: this.billingType,
      qty: this.qty,
      price: this.price,
      discount: this.discount,
      autoBilling: this.autoBilling,
      machineId: this.machineId
    }

    await this.http.post<IMachineServiceBillReceivable>(API_URL + '/machines/create_weigh_bill_receivable', machineServiceBillReceivable, options)
      .toPromise()
      .then(
        data => {
          this.getMachineServiceBillReceivables(this.machineId)
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
    this.machineId = null
    this.autoBilling = 1
    this.weighAmount = 0
  }

  doAutoBilling() {
    this.clearWeighBill()
  }



  refreshWeighAmounts() {
    this.weighAmount = (this.price * this.qty) - this.discount
  }


  async machineBilling(machineId: any) {

    localStorage.setItem('machine-id', '');
    localStorage.setItem('machine-id', machineId);

    await this.router.navigate(['app/accounts-and-finance/machine-service-billing'], {
      queryParams: { machine_id: machineId }
    });

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
          this.showMachineData(data!)
          console.log(data)
        }
      )
  }

  showMachineData(data: IMachine) {

    

    this.machineId = data?.id
    this.weighNo = data?.no

  }

  clearWeighData() {
    this.machineId = null
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
  machineId: any
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


