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
import { IBondZone } from 'src/app/domain/bond-zone';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';
import { IBondItem } from 'src/app/domain/bond-item';
import { IBondItemType } from 'src/app/domain/bond-item-type';
import { error } from 'src/custom-packages/util';
import { NgSelectModule } from '@ng-select/ng-select';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { IServiceBillItem } from 'src/app/domain/maintenance';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-bond-checkout-list',
  standalone: true,
  imports: [
    FormsModule,
        CommonModule,
        SearchFilterPipe,
        NgxPaginationModule,
        RouterModule,
        NgSelectModule
  ],
  templateUrl: './bond-checkout-list.component.html',
  styleUrl: './bond-checkout-list.component.scss'
})
export class BondCheckoutListComponent {
documentHeader!: any

  from: Date | string | null = null
  to: Date | string | null = null

  nickname = ''

  page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  mode: string = ''
  availableBondZones: IBondZone[] = []
  selectedBondZone: IBondZone | null = null
  selectedBondZoneId: string | null = null
  branchId: string = ''
  bondZoneLoaded: boolean = false
  lpoPage: number = 1; // Initialize the current page to 1
  grnPage: number = 1; // Initialize the current page to 1
  filterLpoRecords: string = ''
  filterGrnRecords: string = ''
  selectedOption: string = '';
  /////////////////////////////////
  // For new bondItem

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
  bondItemName: string = 'None'
  bondItemDescription: string = ''
  weight: number = 0 // In kg
  length: number = 0 // In cm
  width: number = 0 // In cm
  height: number = 0 // In cm
  startBillingAt: Date | null | string = null
  status: string = ''
  billingType: string = 'MONTHLY'
  billingAmount: number | null = null
  totalPrice: number | null = null
  initialQty: number = 0
  currentQty: number = 0
  bondZoneId: any = null
  bondItemTypeId: any = null
  bondItemTypeName: string = ''
  bondZoneName: string = ''

  qtyToRemove: number = 0;
  reasonToRemove: string = '';

  bondItems: IBondItem[] = []
  bondItemTypes: IBondItemType[] = []
  // Agent Information
  agentName: string = ''
  agentAddress: string = ''
  agentPhoneNo: string = ''
  agentEmail: string = ''
  tformNumber: string = ''

  // Vehicle or Equipment Information
  registrationNo: string = ''
  transardNo: string = ''
  chasisNo: string = ''
  leftFrontLamp: string = 'YES'
  rightFrontLamp: string = 'YES'
  leftRearLamp: string = 'YES'
  rightRearLamp: string = 'YES'
  leftSideMirror: string = 'YES'
  rightSideMirror: string = 'YES'
  leftWiper: string = 'YES'
  rightWiper: string = 'YES'
  backWiper: string = 'YES'
  fuelCap: string = 'YES'
  spareTire: string = 'YES'
  battery: string = 'YES'
  starter: string = 'YES'
  aerial: string = 'YES'
  wheelCap: string = 'YES'
  roundMirror: string = 'YES'
  tireIndicator: string = 'YES'
  hasKeys: string = 'YES'

  deviceStatus: string = 'ATTACHED'

  vehicleEquipmentColor: string = ''

  cardNo: string = ''

  ///////////////////////

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
  ) { } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  async ngOnInit() {
    this.selectedBondZoneId = localStorage.getItem('selected-bond-zone-id')
    if (this.selectedBondZoneId == '' || this.selectedBondZoneId == null) {
      this.bondZoneLoaded = false
      await this.loadAvailableBondZones()
    } else {
      this.selectedBondZoneId = localStorage.getItem('selected-bond-zone-id')
      await this.loadSelectedBondZone()
    }
    await this.getAllRecentCheckedOutBondItems()
  }

  loadAvailableBondZones = async () => {
    this.availableBondZones = []
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IBondZone[]>(API_URL + '/bond_zones/get_branch_available_bond_zones_by_user', options)
      .toPromise()
      .then(
        data => {
          this.availableBondZones = data!
          console.log(data)
        }
      )
  }

  loadSelectedBondZone = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IBondZone>(API_URL + '/bond_zones/get_selected_bond_zone?bond_zone_id=' + this.selectedBondZoneId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedBondZone = data!
          localStorage.setItem('selected-bond-zone-id', this.selectedBondZone.id.toString());
          this.bondZoneId = this.selectedBondZone.id
          this.bondZoneLoaded = true
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-bond-zone-id', '');
        this.bondZoneLoaded = false
      }
      )
  }



  // onBondZoneChange(event: any): void {
  //   this.selectedBondZoneId = event.target.value;
  //   console.log('Selected BondZone ID:', this.selectedBondZoneId);
  //   //alert('Selected BondZone ID: ' + this.selectedBondZoneId);
  // }

  // selectBondZone() {
  //   //localStorage.setItem('selected-bond-zone-id', this.selectedBondZoneId!);
  //   if (this.selectedBondZoneId! === '' || this.selectedBondZoneId === null) {
  //     this.msg.showErrorMessage3('Please select a bondZone first')
  //     return
  //   }
  //   this.loadSelectedBondZone()
  // }

  // clearSelectedBondZone() {
  //   localStorage.setItem('selected-bond-zone-id', '');
  //   this.selectedBondZoneId = ''

  // }

  // calculateBillingAmount() {
  //   if (this.initialQty == null || this.initialQty <= 0) {
  //     this.billingAmount = 0
  //   }
  //   this.billingAmount = parseFloat((this.totalPrice! / this.initialQty).toFixed(2));
  // }

  // Alerts array to manage the notifications
  alerts: {
    type: string;
    message: string;
    link?: string
  }[] = [
      { type: 'success', message: 'BondZone selected successfully!', link: 'bondZone-sales-order' },

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
    this.clearBondItemData()
    this.mode = 'new'
  }

  setExisting() {
    this.mode = 'existing'
  }

  setReleased() {
    this.mode = 'released'
  }

  // async getAllCompanyActiveBondItemTypes() {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }
  //   this.bondItemTypes = []

  //   await this.http.get<IBondItemType[]>(API_URL + '/bond_item_types/get_all_company_active', options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         var sn = 1
  //         data?.forEach(element => {
  //           element.sn = sn
  //           this.bondItemTypes.push(element)
  //           sn = sn + 1
  //         })
  //         console.log(data)
  //       }
  //     )
  // }

  // public async saveBondItem() {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   if (this.bondZoneId === null || this.bondZoneId === undefined) {
  //     this.msg.showErrorMessage3('BondZone not defined, please select a bondZone first')
  //   }

  //   // if (this.billingAmount === null || this.billingAmount === undefined) {
  //   //   this.msg.showErrorMessage3('Billing amount not defined, please provide billing amount first')
  //   // }

  //   var bondItem = {
  //     id: this.id,
  //     no: this.no,
  //     ownerFirstName: this.ownerFirstName,
  //     ownerMiddleName: this.ownerMiddleName,
  //     ownerLastName: this.ownerLastName,
  //     ownerCompanyName: this.ownerCompanyName,
  //     ownerIdNo: this.ownerIdNo,
  //     ownerIdType: this.ownerIdType,
  //     ownerPhoneNo: this.ownerPhoneNo,
  //     ownerEmail: this.ownerEmail,
  //     ownerAddress: this.ownerAddress,
  //     bondZoneId: this.bondZoneId,
  //     billingType: this.billingType,
  //     billingAmount: this.billingAmount,
  //     comments: this.comments,
  //     bondItemName: this.bondItemTypeName,
  //     bondItemDescription: this.bondItemDescription,
  //     bondItemTypeName: this.bondItemTypeName,
  //     length: this.length,
  //     width: this.width,
  //     height: this.height,
  //     weight: this.weight,
  //     initialQty: this.initialQty,
  //     startBillingAt: this.startBillingAt,



  //     ////////////////////////////



  //     // Agent Information
  //     agentName: this.agentName,
  //     agentAddress: this.agentAddress,
  //     agentPhoneNo: this.agentPhoneNo,
  //     agentEmail: this.agentEmail,
  //     tformNumber: this.tformNumber,



  //     // Vehicle or Equipment Information
  //     registrationNo: this.registrationNo,
  //     transardNo: this.transardNo,
  //     chasisNo: this.chasisNo,

  //     leftFrontLamp: this.leftFrontLamp === 'YES' ? 1 : 0,
  //     rightFrontLamp: this.rightFrontLamp === 'YES' ? 1 : 0,
  //     leftRearLamp: this.leftRearLamp === 'YES' ? 1 : 0,
  //     rightRearLamp: this.rightRearLamp === 'YES' ? 1 : 0,
  //     leftSideMirror: this.leftSideMirror === 'YES' ? 1 : 0,
  //     rightSideMirror: this.rightSideMirror === 'YES' ? 1 : 0,
  //     leftWiper: this.leftWiper === 'YES' ? 1 : 0,
  //     rightWiper: this.rightWiper === 'YES' ? 1 : 0,
  //     backWiper: this.backWiper === 'YES' ? 1 : 0,
  //     fuelCap: this.fuelCap === 'YES' ? 1 : 0,
  //     spareTire: this.spareTire === 'YES' ? 1 : 0,
  //     battery: this.battery === 'YES' ? 1 : 0,
  //     starter: this.starter === 'YES' ? 1 : 0,
  //     aerial: this.aerial === 'YES' ? 1 : 0,
  //     wheelCap: this.wheelCap === 'YES' ? 1 : 0,
  //     roundMirror: this.roundMirror === 'YES' ? 1 : 0,
  //     tireIndicator: this.tireIndicator === 'YES' ? 1 : 0,
  //     hasKeys: this.hasKeys === 'YES' ? 1 : 0,

  //     deviceStatus: this.deviceStatus === 'ATTACHED' ? 1 : 0,


  //     bondItemColor: this.vehicleEquipmentColor,

  //     cardNo: this.cardNo,

  //     billintType: this.billingType,



  //     //////////////////////////////
  //   }


  //   if (this.id === null || this.id === undefined || this.id === '') {
  //     /**Create new parking */
  //     await this.http.post<IBondItem>(API_URL + '/bond_items/create', bondItem, options)
  //       .toPromise()
  //       .then(
  //         data => {
  //           this.showBondItemData(data!)
  //           console.log(data)
  //           // this.getAllPendingOrCheckedInParkings()
  //           this.msg.showSuccessMessage('BondItem created successifully, good available for check in')
  //         }
  //       )
  //       .catch(
  //         error => {
  //           console.log(error)
  //           this.msg.showErrorMessage(error, 'Error')
  //         }
  //       )
  //   } else {
  //     /**Update an exiisting parking */
  //     await this.http.post<IBondItem>(API_URL + '/bond_items/update', bondItem, options)
  //       .toPromise()
  //       .then(
  //         data => {
  //           this.showBondItemData(data!)
  //           console.log(data)
  //           // this.getAllPendingOrCheckedInParkings()
  //           if (this.mode === 'existing') { // If in existing mode, reload bondItems to reflect the changes
  //             this.getAllCheckedInAndPendingBondItems()
  //           }
  //           this.msg.showSuccessMessage('BondItem updated successifully, good available for check in')
  //         }
  //       )
  //       .catch(
  //         error => {
  //           console.log(error)
  //           this.msg.showErrorMessage3(error)
  //         }
  //       )
  //   }
  // }

  // clearRemove() {
  //   this.reasonToRemove = ''
  //   this.qtyToRemove = 0
  // }

  // public async remove() {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   if (this.bondZoneId === null || this.bondZoneId === undefined) {
  //     this.msg.showErrorMessage3('BondZone not defined, please select a bondZone first')
  //   }

  //   if (this.billingAmount === null || this.billingAmount === undefined) {
  //     this.msg.showErrorMessage3('Billing amount not defined, please provide billing amount first')
  //   }

  //   await this.http.post<null>(API_URL + '/bond_items/remove?bond_item_id=' + this.id + '&qty=' + this.qtyToRemove + '&reason=' + this.reasonToRemove, null, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         // this.showBondItemData(data!)
  //         // console.log(data)
  //         // // this.getAllPendingOrCheckedInParkings()
  //         if (this.mode === 'existing') { // If in existing mode, reload bondItems to reflect the changes
  //           this.getAllCheckedInAndPendingBondItems()
  //         }
  //         this.msg.showSuccessMessage('Archived Successifully')
  //       }
  //     )
  //     .catch(
  //       error => {
  //         console.log(error)
  //         this.msg.showErrorMessage(error, '')
  //       }
  //     )

  // }

  showBondItemData(data: IBondItem) {
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
    this.bondItemName = data!.bondItemName
    this.bondItemDescription = data!.bondItemDescription
    this.bondItemTypeName = data!.bondItemTypeName
    this.length = data!.length
    this.width = data!.width
    this.height = data!.height
    this.weight = data!.weight
    this.startBillingAt = data!.billingStartAt
    this.bondZoneName = data!.bondZoneName
    this.status = data!.status
    this.currentQty = data!.currentQty

    ////////////////////////


    this.ownerFirstName = data?.ownerFirstName;
    this.ownerMiddleName = data?.ownerMiddleName;
    this.ownerLastName = data?.ownerLastName;
    this.ownerCompanyName = data?.ownerCompanyName;
    this.ownerIdNo = data?.ownerIdNo;
    this.ownerIdType = data?.ownerIdType;
    this.ownerPhoneNo = data?.ownerPhoneNo;
    this.ownerEmail = data?.ownerEmail;
    this.ownerAddress = data?.ownerAddress;

    // Agent Information
    this.agentName = data?.agentName;
    this.agentAddress = data?.agentAddress;
    this.agentPhoneNo = data?.agentPhoneNo;
    this.agentEmail = data?.agentEmail;
    this.tformNumber = data?.tformNumber;

    this.billingType = data?.billingType
    this.billingAmount = data?.billingAmount

    // Vehicle or Equipment Information
    this.registrationNo = data?.registrationNo;
    this.chasisNo = data?.chasisNo;
    this.leftFrontLamp = data?.leftFrontLamp == true ? 'YES' : 'NO'
    this.rightFrontLamp = data?.rightFrontLamp == true ? 'YES' : 'NO'
    this.leftRearLamp = data?.leftRearLamp == true ? 'YES' : 'NO'
    this.rightRearLamp = data?.rightRearLamp == true ? 'YES' : 'NO'
    this.leftSideMirror = data?.leftSideMirror == true ? 'YES' : 'NO'
    this.rightSideMirror = data?.rightSideMirror == true ? 'YES' : 'NO'
    this.leftWiper = data?.leftWiper == true ? 'YES' : 'NO'
    this.rightWiper = data?.rightWiper == true ? 'YES' : 'NO'
    this.backWiper = data?.backWiper == true ? 'YES' : 'NO'
    this.fuelCap = data?.fuelCap == true ? 'YES' : 'NO'
    this.spareTire = data?.spareTire == true ? 'YES' : 'NO'
    this.battery = data?.battery == true ? 'YES' : 'NO'
    this.starter = data?.starter == true ? 'YES' : 'NO'
    this.aerial = data?.aerial == true ? 'YES' : 'NO'
    this.wheelCap = data?.wheelCap == true ? 'YES' : 'NO'
    this.roundMirror = data?.roundMirror == true ? 'YES' : 'NO'
    this.tireIndicator = data?.tireIndicator == true ? 'YES' : 'NO'
    this.hasKeys = data?.hasKeys == true ? 'YES' : 'NO'
    this.deviceStatus = data!.deviceStatus == true ? 'ATTACHED' : 'NOT-ATTACHED'

    this.comments = data!.comments



    this.vehicleEquipmentColor = data!.vehicleEquipmentColor

    this.cardNo = data!.cardNo

    this.billingType = data!.billingType
    this.startBillingAt = data!.billingStartAt

    /////////////////////////
  }

  clearBondItemData() {
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
    this.billingType = 'MONTHLY'
    this.billingAmount = null
    this.totalPrice = null
    this.bondItemTypeId = null
    this.bondItemTypeName = ''
    this.bondItemName = ''
    this.bondItemDescription = ''
    this.comments = ''
    this.length = 0
    this.width = 0
    this.height = 0
    this.weight = 0
    this.startBillingAt = null
    this.bondZoneName = ''
    this.status = ''

    /////////////////////////

    this.ownerFirstName = ''
    this.ownerMiddleName = ''
    this.ownerLastName = ''
    this.ownerCompanyName = ''
    this.ownerIdNo = ''
    this.ownerIdType = ''
    this.ownerPhoneNo = ''
    this.ownerEmail = ''
    this.ownerAddress = ''

    // Agent Information
    this.agentName = ''
    this.agentAddress = ''
    this.agentPhoneNo = ''
    this.agentEmail = ''
    this.tformNumber = ''

    this.billingType = 'MONTHLY'
    this.billingAmount = 0

    // Vehicle or Equipment Information
    this.registrationNo = ''
    this.chasisNo = ''
    this.transardNo = ''
    this.leftFrontLamp = 'YES'
    this.rightFrontLamp = 'YES'
    this.leftRearLamp = 'YES'
    this.rightRearLamp = 'YES'
    this.leftSideMirror = 'YES'
    this.rightSideMirror = 'YES'
    this.leftWiper = 'YES'
    this.rightWiper = 'YES'
    this.backWiper = 'YES'
    this.fuelCap = 'YES'
    this.spareTire = 'YES'
    this.battery = 'YES'
    this.starter = 'YES'
    this.aerial = 'YES'
    this.wheelCap = 'YES'
    this.roundMirror = 'YES'
    this.tireIndicator = 'YES'

    this.comments = ''

    this.deviceStatus = 'ATTACHED'

    this.vehicleEquipmentColor = ''



    this.hasKeys = 'YES'

    this.billingType = 'MONTHLY'

    /////////////////////////
  }



  // async getAllCheckedInAndPendingBondItems() {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }
  //   this.bondItems = []

  //   await this.http.get<IBondItem[]>(API_URL + '/bond_items/get_all_pending_or_checked_in_by_bond_zone?bond_zone_id=' + this.bondZoneId, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         data?.reverse()
  //         var sn = 1
  //         data?.forEach(element => {
  //           element.sn = sn
  //           this.bondItems.push(element)
  //           sn = sn + 1
  //         })
  //         console.log(data)
  //       }
  //     )
  //     .catch(error => {
  //       console.log(error)
  //     })
  // }

  // async getAllCheckedInBondItems() {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }
  //   this.bondItems = []

  //   await this.http.get<IBondItem[]>(API_URL + '/bond_items/get_all_checked_in_by_bond_zone?bond_zone_id=' + this.bondZoneId, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         data?.reverse()
  //         var sn = 1
  //         data?.forEach(element => {
  //           element.sn = sn
  //           this.bondItems.push(element)
  //           sn = sn + 1
  //         })
  //         console.log(data)
  //       }
  //     )
  //     .catch(error => {
  //       console.log(error)
  //     })
  // }

  async getAllRecentCheckedOutBondItems() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.bondItems = []

    await this.http.get<IBondItem[]>(API_URL + '/bond_items/get_all_recent_checked_out_by_bond_zone?bond_zone_id=' + this.bondZoneId, options)
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
      .catch(error => {
        console.log(error)
      })
  }

  async getBondItem(bondItemId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IBondItem>(API_URL + '/bond_items/get?id=' + bondItemId, options)
      .toPromise()
      .then(
        data => {
          this.showBondItemData(data!)
          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      })
  }

  async get(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IBondItem>(API_URL + '/bond_items/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.clearBondItemData()
          this.startBillingAt = null
          this.showBondItemData(data!)
          this.billItems = data!.serviceBillItems
          console.log(data)
        }
      )
  }

  

  // async checkIn(id: any, no: string, descr: string) {
  //   if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check in bondItem no: ' + no + ' - ' + descr + '?', 'question', 'Yes', 'No') == false) {
  //     return
  //   }

  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   var bondItem = {
  //     id: id
  //   }

  //   await this.http.post<IBondItem>(API_URL + '/bond_items/check_in', bondItem, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         console.log(data)
  //         this.getAllCheckedInAndPendingBondItems()
  //         this.msg.showSuccessMessage('Checked in Successifully')
  //         this.mode = ''
  //       }
  //     )
  //     .catch(
  //       error => {
  //         console.log(error)
  //         this.msg.showErrorMessage(error, 'Error')
  //         this.getAllCheckedInAndPendingBondItems()
  //       }
  //     )
  // }

  // async checkOut(id: any, no: string, descr: string): Promise<void> {


  //   if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out bondItem no: ' + no + ' - ' + descr + '?', 'question', 'Yes', 'No') == false) {
  //     return
  //   }

  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   var bondItem = {
  //     id: id
  //     // cardNo : this.cardNo,
  //     // bondItemZoneName : this.bondItemZoneName,
  //     // startBillingAt : this.startBillingAt
  //   }

  //   await this.http.post<IBondItem>(API_URL + '/bond_items/check_out', bondItem, options)
  //     .toPromise()
  //     .then(
  //       data => {

  //         console.log(data)

  //         this.getAllCheckedInAndPendingBondItems()
  //         this.msg.showSuccessMessage('Checked out Successifully')

  //         //this.printGatePassRcpt(data!.serviceBillItems, '', 0);
  //       }
  //     )
  //     .catch(
  //       error => {
  //         console.log(error)
  //         this.msg.showErrorMessage(error, 'Error')
  //         this.getAllCheckedInAndPendingBondItems()
  //       }
  //     )
  // }

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


  // originalQty: number = 0
  // availableQty: number = 0
  // releasedQty: number = 0
  // availableForRelease: number = 0

  // qtyToRelease: number = 0

  currentBondItemId: any = null

  // async getBondItemGoodReleaseDetail(bondItemId: any) {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   this.originalQty = 0
  //   this.availableQty = 0
  //   this.releasedQty = 0
  //   this.availableForRelease = 0

  //   this.currentBondItemId = null

  //   this.qtyToRelease = 0

  //   await this.http.get<IBondItemGoodReleaseDetail>(API_URL + '/bond_item_good_releases/get_bond_item_good_release_detail?bond_item_id=' + bondItemId, options)
  //     .toPromise()
  //     .then(
  //       data => {

  //         this.currentBondItemId = bondItemId

  //         this.originalQty = data!.initialQty
  //         this.availableQty = data!.currentQty
  //         this.releasedQty = data!.releasedQty
  //         this.availableForRelease = data!.availableForRelease

  //         this.qtyToRelease = 0

  //         console.log(data)
  //       }
  //     )
  // }


  // async createBondItemGoodRelease() {

  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   var bondItemGoodRelease = {
  //     bondItemId: this.currentBondItemId,
  //     qty: this.qtyToRelease,

  //   }

  //   await this.http.post<IBondItemGoodReleaseDetail>(API_URL + '/bond_item_good_releases/create_bond_item_good_release', bondItemGoodRelease, options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         //this.getBondItemBillReceivables(this.bondItemId)
  //         this.msg.showSuccessMessage('Success')
  //         console.log(data)
  //       }
  //     )
  //     .catch(
  //       error => {
  //         this.msg.showErrorMessage(error, 'Error')
  //         console.log(error)
  //       }
  //     )


  // }

  releases: IBondItemGoodRelease[] = []

  // async getBondItemGoodReleases(bondItemId: any) {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }
  //   this.releases = []

  //   await this.http.get<IBondItemGoodRelease[]>(API_URL + '/bond_item_good_releases/get_by_bond_item?bond_item_id=' + bondItemId, options)
  //     .toPromise()
  //     .then(
  //       data => {

  //         this.releases = data!.slice().reverse()
  //         console.log(data)
  //       }
  //     )
  // }

  //   String id;
  // String no;
  // String qty;
  // String status;
  // String bondItemId;
  // String releaseDate;
  // //
  // String clientName;
  // String goodName;
  // String unitPrice;
  // String total;


  bondItemGoodReleaseId: any
  bondItemGoodReleaseNo: string = ''
  bondItemGoodReleaseQty: number = 0
  bondItemGoodReleaseStatus: string = ''
  bondItemGoodReleaseBondItemId: string = ''
  bondItemGoodReleaseReleaseDate: string = ''
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
    this.bondItemGoodReleaseClientName = ''
    this.bondItemGoodReleaseClientAddress = ''
    this.bondItemGoodReleaseClientPhoneNo = ''
    this.bondItemGoodReleaseGoodName = ''
    this.bondItemGoodReleaseUnitPrice = ''
    this.bondItemGoodReleaseTotal = ''

    this.billItems = []

    await this.http.get<IBondItemGoodRelease>(API_URL + '/bond_item_good_releases/get?id=' + id, options)
      .toPromise()
      .then(
        data => {

          this.bondItemGoodReleaseId = data!.id
          this.bondItemGoodReleaseNo = data!.no
          this.bondItemGoodReleaseQty = data!.qty
          this.bondItemGoodReleaseStatus = data!.status
          this.bondItemGoodReleaseBondItemId = data!.bondItemId
          this.bondItemGoodReleaseReleaseDate = data!.releaseDate
          this.bondItemGoodReleaseClientName = data!.clientName
          this.bondItemGoodReleaseClientAddress = data!.clientAddress
          this.bondItemGoodReleaseClientPhoneNo = data!.clientPhoneNo
          this.bondItemGoodReleaseGoodName = data!.goodName
          this.bondItemGoodReleaseUnitPrice = data!.unitPrice
          this.bondItemGoodReleaseTotal = data!.total

          this.billItems = []

          this.billItem = {
            sn: 1,
            item: this.bondItemGoodReleaseGoodName,
            qty: this.bondItemGoodReleaseQty,
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
        {
          text: 'Client Name: ' + this.bondItemGoodReleaseClientName,
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

  async printReleaseNote(id: any) {
    await this.get(id)
    await this.printGatePassRcpt(this.billItems, '', id)
  }

  printGatePassRcpt = async (billItems: IServiceBillItem[], receiptNo: string, cash: number) => {
  
      //await this.get(this.bondItemId)
      //await this.getLastBillingDate(this.bondItemId)
  
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
                [{ text: 'Vehicle Name: ' + this.bondItemDescription, alignment: 'left', fontSize: 9, bold: false }],
                [{ text: 'Chasis No: ' + this.chasisNo, alignment: 'left', fontSize: 9, bold: false }],
                [{ text: 'Color: ' + ''!, alignment: 'left', fontSize: 9, bold: false }],
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
                [{ text: '---Reprinted---', fontSize: 9, bold: true, alignment: 'center' }],
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


  printGatePassRcpt1 = async (billItems: IServiceBillItem[], receiptNo: string, cash: number) => {
  
      //await this.get(this.id)
      //await this.getLastBillingDate(this.parkingId)
  
      var companyName = localStorage.getItem('company-name')!
  
      var header = ''
      var footer = ''
      var title = 'Gate Pass(Reprinted)'
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
                [{ text: title, alignment: 'center', fontSize: 9, bold: true }],
                [{ text: 'Vehicle Name: ' + this.bondItemTypeName, alignment: 'left', fontSize: 9, bold: false }],
                [{ text: 'Vehicle Color: ' + '', alignment: 'left', fontSize: 9, bold: false }],
                [{ text: 'Chassis No: ' + this.chasisNo, alignment: 'left', fontSize: 9, bold: false }],
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
                //[{ text: 'Valid Until: ' + this.lastBillingDate, alignment: 'left', fontSize: 9, bold: true }],
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

  getTzFormatCurrency(value: any) {
    return new Intl.NumberFormat('en-TZ', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }



  grant(privileges: string[]): boolean {
    return this.auth.grant(privileges); // Adjust return value based on logic
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
  goodName: string
  unitPrice: string
  total: string
}
