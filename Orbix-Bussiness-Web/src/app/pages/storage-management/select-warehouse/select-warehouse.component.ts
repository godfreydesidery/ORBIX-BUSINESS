import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IWarehouse } from 'src/app/domain/warehouse';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';
import { IStorage } from 'src/app/domain/storage';
import { IGoodType } from 'src/app/domain/good-type';
import { error } from 'src/custom-packages/util';

import * as pdfMake from 'pdfmake/build/pdfmake';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-select-warehouse',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-warehouse.component.html',
  styleUrl: './select-warehouse.component.scss'
})
export class SelectWarehouseComponent {

  documentHeader! : any

  from : Date | string | null = null
  to : Date | string | null = null

  nickname = ''

  page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  mode: string = ''
  availableWarehouses: IWarehouse[] = []
  selectedWarehouse: IWarehouse | null = null
  selectedWarehouseId: string | null = null
  branchId: string = ''
  warehouseLoaded: boolean = false
  lpoPage: number = 1; // Initialize the current page to 1
  grnPage: number = 1; // Initialize the current page to 1
  filterLpoRecords: string = ''
  filterGrnRecords: string = ''
  selectedOption: string = '';
  /////////////////////////////////
  // For new storage

  id: any = null
  no: string = ''
  ownerFirstName: string = ''
  ownerMiddleName: string = ''
  ownerLastName: string = ''
  ownerCompanyName: string = ''
  ownerIdNo: string = ''
  ownerIdType: string = ''
  ownerPhoneNo: string = ''
  ownerEmail: string = ''
  ownerAddress: string = ''
  comments: string = ''
  goodName: string = ''
  goodDescription: string = ''
  weight: number = 0 // In kg
  length: number = 0 // In cm
  width: number = 0 // In cm
  height: number = 0 // In cm
  startBillingAt: Date | null = null
  status: string = ''
  billingType: string = 'FLAT-RATE'
  billingAmount: number | null = null
  initialQty: number = 0
  currentQty: number = 0
  warehouseId: any = null
  goodTypeId: any = null
  goodTypeName: string = ''
  warehouseName : string = ''

  storages: IStorage[] = []
  goodTypes: IGoodType[] = []

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
  ) { } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    this.selectedWarehouseId = localStorage.getItem('selected-warehouse-id')
    if (this.selectedWarehouseId == '' || this.selectedWarehouseId == null) {
      this.warehouseLoaded = false
      this.loadAvailableWarehouses()
    } else {
      this.selectedWarehouseId = localStorage.getItem('selected-warehouse-id')
      this.loadSelectedWarehouse()
    }
    this.getAllCompanyActiveGoodTypes()
  }

  loadAvailableWarehouses = async () => {
    this.availableWarehouses = []
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWarehouse[]>(API_URL + '/warehouses/get_branch_available_warehouses_by_user', options)
      .toPromise()
      .then(
        data => {
          this.availableWarehouses = data!
          console.log(data)
        }
      )
  }

  loadSelectedWarehouse = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWarehouse>(API_URL + '/warehouses/get_selected_warehouse?warehouse_id=' + this.selectedWarehouseId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedWarehouse = data!
          localStorage.setItem('selected-warehouse-id', this.selectedWarehouse.id.toString());
          this.warehouseId = this.selectedWarehouse.id
          this.warehouseLoaded = true
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-warehouse-id', '');
        this.warehouseLoaded = false
      }
      )
  }



  onWarehouseChange(event: any): void {
    this.selectedWarehouseId = event.target.value;
    console.log('Selected Warehouse ID:', this.selectedWarehouseId);
    //alert('Selected Warehouse ID: ' + this.selectedWarehouseId);
  }

  selectWarehouse() {
    //localStorage.setItem('selected-warehouse-id', this.selectedWarehouseId!);
    if (this.selectedWarehouseId! === '' || this.selectedWarehouseId === null) {
      this.msg.showErrorMessage3('Please select a warehouse first')
      return
    }
    this.loadSelectedWarehouse()
  }

  clearSelectedWarehouse() {
    localStorage.setItem('selected-warehouse-id', '');
    this.selectedWarehouseId = ''

  }

  // Alerts array to manage the notifications
  alerts: {
    type: string;
    message: string;
    link?: string
  }[] = [
      { type: 'success', message: 'Warehouse selected successfully!', link: 'warehouse-sales-order' },

    ];

  // Close alert method
  closeAlert(index: number) {
    this.alerts.splice(index, 1); // Remove the alert at the given index
  }

  // Add an alert for testing purposes
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }

  setNew() {
    this.clearStorageData()
    this.mode = 'new'
  }

  setExisting() {
    this.mode = 'existing'
  }

  async getAllCompanyActiveGoodTypes() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.goodTypes = []

    await this.http.get<IGoodType[]>(API_URL + '/good_types/get_all_company_active', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.goodTypes.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  public async saveStorage() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    if (this.warehouseId === null || this.warehouseId === undefined) {
      this.msg.showErrorMessage3('Warehouse not defined, please select a warehouse first')
    }

    if(this.billingAmount === null || this.billingAmount === undefined) {
      this.msg.showErrorMessage3('Billing amount not defined, please provide billing amount first')
    }  

    var storage = {
      id: this.id,
      no: this.no,
      ownerFirstName: this.ownerFirstName,
      ownerMiddleName: this.ownerMiddleName,
      ownerLastName: this.ownerLastName,
      ownerCompanyName: this.ownerCompanyName,
      ownerIdNo: this.ownerIdNo,
      ownerIdType: this.ownerIdType,
      ownerPhoneNo: this.ownerPhoneNo,
      ownerEmail: this.ownerEmail,
      ownerAddress: this.ownerAddress,
      warehouseId: this.warehouseId,
      billingType: this.billingType,
      billingAmount: this.billingAmount,
      comments: this.comments,
      goodName: this.goodName,
      goodDescription: this.goodDescription,
      goodTypeName: this.goodTypeName,
      length : this.length,
      width : this.width,
      height : this.height,
      weight : this.weight,
      initialQty : this.initialQty
    }


    if (this.id === null || this.id === undefined || this.id === '') {
      /**Create new parking */
      await this.http.post<IStorage>(API_URL + '/storages/create', storage, options)
        .toPromise()
        .then(
          data => {
            this.showStorageData(data!)
            console.log(data)
            // this.getAllPendingOrCheckedInParkings()
            this.msg.showSuccessMessage('Storage created successifully, good available for check in')
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an exiisting parking */
      await this.http.post<IStorage>(API_URL + '/storages/update', storage, options)
        .toPromise()
        .then(
          data => {
            this.showStorageData(data!)
            console.log(data)
            // this.getAllPendingOrCheckedInParkings()
            if (this.mode === 'existing') { // If in existing mode, reload storages to reflect the changes
              this.getAllCheckedInAndPendingStorages()
            }
            this.msg.showSuccessMessage('Storage updated successifully, good available for check in')
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage3(error)
          }
        )
    }
  }

  showStorageData(data: IStorage) {
    this.id = data?.id
    this.no = data!.no
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
    this.comments = data!.comments
    this.billingType = data!.billingType
    this.goodName = data!.goodName
    this.goodDescription = data!.goodDescription
    this.goodTypeName = data!.goodTypeName
    this.length = data!.length
    this.width = data!.width
    this.height = data!.height
    this.weight = data!.weight
  }

  clearStorageData() {
    //this.showParking = false
    this.id = null;
    this.no = ''
    this.ownerFirstName = ''
    this.ownerMiddleName = ''
    this.ownerLastName = ''
    this.ownerCompanyName = ''
    this.ownerIdNo = ''
    this.ownerIdType = ''
    this.ownerPhoneNo = ''
    this.ownerEmail = ''
    this.ownerAddress = ''
    this.billingType = 'FLAT-RATE'
    this.billingAmount = null
    this.goodTypeId = null
    this.goodTypeName = ''
    this.goodName = ''
    this.goodDescription = ''
    this.comments = ''
    this.length = 0
    this.width = 0
    this.height = 0
    this.weight = 0
  }



  async getAllCheckedInAndPendingStorages() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.storages = []

    await this.http.get<IStorage[]>(API_URL + '/storages/get_all_pending_or_checked_in_by_warehouse?warehouse_id=' + this.warehouseId, options)
      .toPromise()
      .then(
        data => {
          data?.reverse()
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.storages.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }

  async getAllRecentCheckedOutStorages() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.storages = []

    await this.http.get<IStorage[]>(API_URL + '/storages/get_all_recent_checked_out_by_warehouse?warehouse_id=' + this.warehouseId, options)
      .toPromise()
      .then(
        data => {
          data?.reverse()
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.storages.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }

  async getStorage(storageId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IStorage>(API_URL + '/storages/get?id=' + storageId, options)
      .toPromise()
      .then(
        data => {
          this.showStorageData(data!)
          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      })
  }

  async get(id : any){
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IStorage>(API_URL+'/storages/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.startBillingAt = null
          this.showStorageData(data!)
          console.log(data)
        }
      )
    }

  async checkIn(id: any, no: string, descr: string) {
    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check in storage no: ' + no + ' - ' + descr + '?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var storage = {
      id: id
    }

    await this.http.post<IStorage>(API_URL + '/storages/check_in', storage, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllCheckedInAndPendingStorages()
          this.msg.showSuccessMessage('Checked in Successifully')
          this.mode = ''
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
          this.getAllCheckedInAndPendingStorages()
        }
      )
  }

  async checkOut(id: any, no: string, descr: string): Promise<void>{
  
  
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out storage no: ' + no + ' - ' + descr + '?', 'question', 'Yes', 'No') == false){
        return
      }
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var storage = {
        id : id
        // cardNo : this.cardNo,
        // storageZoneName : this.storageZoneName,
        // startBillingAt : this.startBillingAt
      }
  
      await this.http.post<IStorage>(API_URL+'/storages/check_out', storage, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)

            this.getAllCheckedInAndPendingStorages()
            this.msg.showSuccessMessage('Checked out Successifully')
            
            //this.printGatePassRcpt(data!.serviceBillItems, '', 0);
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
            this.getAllCheckedInAndPendingStorages()
          }
        )
    }

  // async checkIn(): Promise<void>{


  //     if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false){
  //       return
  //     }

  //     let options = {
  //       headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //     }

  //     var parking = {
  //       id : this.id,
  //       cardNo : this.cardNo,
  //       parkingZoneName : this.parkingZoneName,
  //       startBillingAt : this.startBillingAt
  //     }

  //     await this.http.post<IParking>(API_URL+'/parkings/check_out', parking, options)
  //       .toPromise()
  //       .then(
  //         data => {

  //           console.log(data)

  //           this.getAllClearedParkings()
  //           this.msg.showSuccessMessage('Checked out Successifully')

  //           this.printGatePassRcpt(data!.serviceBillItems, '', 0);
  //         }
  //       )
  //       .catch(
  //         error => {
  //           console.log(error)
  //           this.msg.showErrorMessage(error, 'Error')
  //         }
  //       )
  //   }


  originalQty : number = 0
  availableQty : number = 0
  releasedQty : number = 0
  availableForRelease : number = 0

  qtyToRelease : number = 0

  currentStorageId : any = null

  async getStorageGoodReleaseDetail(storageId : any){
      let options = { 
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      this.originalQty = 0
      this.availableQty = 0
      this.releasedQty = 0
      this.availableForRelease = 0

      this.currentStorageId = null

      this.qtyToRelease = 0
  
      await this.http.get<IStorageGoodReleaseDetail>(API_URL+'/storage_good_releases/get_storage_good_release_detail?storage_id=' + storageId, options)
      .toPromise()
      .then(
        data => {

          this.currentStorageId = storageId
  
          this.originalQty = data!.initialQty
          this.availableQty = data!.currentQty
          this.releasedQty = data!.releasedQty
          this.availableForRelease = data!.availableForRelease

          this.qtyToRelease = 0
          
          console.log(data)
        }
      )
    }


    async createStorageGoodRelease(){
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var storageGoodRelease = {
        storageId : this.currentStorageId,
        qty : this.qtyToRelease,
        
      }
  
      await this.http.post<IStorageGoodReleaseDetail>(API_URL+'/storage_good_releases/create_storage_good_release', storageGoodRelease, options)  
      .toPromise()
      .then(
        data => {
          //this.getStorageBillReceivables(this.storageId)
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

    releases : IStorageGoodRelease[] = []

    async getStorageGoodReleases(storageId : any){
      let options = { 
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.releases = []
  
      await this.http.get<IStorageGoodRelease[]>(API_URL+'/storage_good_releases/get_by_storage?storage_id=' + storageId, options)
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


  storageGoodReleaseId : any
  storageGoodReleaseNo : string = ''
  storageGoodReleaseQty : number = 0
  storageGoodReleaseStatus : string = ''
  storageGoodReleaseStorageId : string = ''
  storageGoodReleaseReleaseDate : string = ''
  storageGoodReleaseClientName : string = ''
  storageGoodReleaseGoodName : string = ''
  storageGoodReleaseUnitPrice : string = ''
  storageGoodReleaseTotal : string = ''

    async getStorageGoodRelease(id : any){
      let options = { 
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      this.storageGoodReleaseId = null
      this.storageGoodReleaseNo = ''
      this.storageGoodReleaseQty = 0
      this.storageGoodReleaseStatus = ''
      this.storageGoodReleaseStorageId = ''
      this.storageGoodReleaseReleaseDate = ''
      this.storageGoodReleaseClientName = ''
      this.storageGoodReleaseGoodName = ''
      this.storageGoodReleaseUnitPrice = ''
      this.storageGoodReleaseTotal = ''
  
      await this.http.get<IStorageGoodRelease>(API_URL+'/storage_good_releases/get?id=' + id, options)
      .toPromise()
      .then(
        data => {

          this.storageGoodReleaseId = data!.id
          this.storageGoodReleaseNo = data!.no
          this.storageGoodReleaseQty = data!.qty
          this.storageGoodReleaseStatus = data!.status
          this.storageGoodReleaseStorageId = data!.storageId
          this.storageGoodReleaseReleaseDate = data!.releaseDate
          this.storageGoodReleaseClientName = data!.clientName
          this.storageGoodReleaseGoodName = data!.goodName
          this.storageGoodReleaseUnitPrice = data!.unitPrice
          this.storageGoodReleaseTotal = data!.total

          console.log(data)
        }
      )
    }

    async printStorageGoodReleaseNote(id : any){
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
            {
              text: 'Client Name: ' + this.storageGoodReleaseClientName,
              fontSize: 12,
              margin: [0, 0, 0, 15],
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
            {text : ''},
            {text : ''},
            {
              text: 'Served By: _________________________',
              fontSize: 12,
              margin: [0, 0, 0, 15],
            },
          ],
        };
      
        pdfMake.createPdf(docDefinition).print();

    }

    getTzFormatCurrency(value: any) {
      return new Intl.NumberFormat('en-TZ', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }).format(value);
    }

    






}

interface IStorageGoodReleaseDetail{
  initialQty : number
  currentQty : number
  releasedQty : number
  availableForRelease : number
}

interface IStorageGoodRelease{
  id : any
  no : string
  qty : number
  status : string
  storageId : any
  releaseDate : string
  clientName : string
  goodName : string
  unitPrice : string
  total : string
}
