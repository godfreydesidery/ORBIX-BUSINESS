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
import { IWorkshop } from 'src/app/domain/workshop';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';
import { IStorage } from 'src/app/domain/storage';
import { IGoodType } from 'src/app/domain/good-type';
import { error } from 'src/custom-packages/util';

import * as pdfMake from 'pdfmake/build/pdfmake';
import { IServiceBillItem } from 'src/app/domain/maintenance';
import { IMachine } from 'src/app/domain/machine';
import { IMachineService } from 'src/app/domain/machine-service';
import { IService } from 'src/app/domain/service';

var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-select-workshop',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-workshop.component.html',
  styleUrl: './select-workshop.component.scss'
})
export class SelectWorkshopComponent {

  machineId: any = null
  machineNo: string = ''
  machineName: string = ''
  machineRegNo: string = ''
  ownerName: string = ''
  ownerPhoneNo: string = ''

  recordMode: string = ''
  viewOnly: boolean = false;


  machineServiceId: any = null
  serviceId: any = null
  qty: number = 1


  public async saveMachine() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    if (this.workshopId === null || this.workshopId === undefined) {
      this.msg.showErrorMessage3('Workshop not defined, please select a workshop first')
    }

    var machine = {
      id: this.machineId,
      no: this.machineNo,
      regNo: this.machineRegNo,
      name: this.machineName,
      ownerName: this.ownerName,
      ownerPhoneNo: this.ownerPhoneNo,
      workshopId: this.workshopId
    }

    if (this.machineId === null || this.machineId === undefined || this.machineId === '') {
      /**Create new parking */
      await this.http.post<IMachine>(API_URL + '/machines/create', machine, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Machine created successifully')
            this.machineId = data!.id
            this.machineNo = data!.no
            this.machineName = data!.name
            this.ownerName = data!.ownerName
            this.ownerPhoneNo = data!.ownerPhoneNo
            this.viewOnly = true
            console.log(data)
            this.viewMachine(this.machineId)
            // this.getAllPendingOrCheckedInParkings()
            
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
      await this.http.post<IMachine>(API_URL + '/machines/update', machine, options)
        .toPromise()
        .then(
          data => {
            this.msg.showSuccessMessage('Machine updated successifully')
            this.machineId = data!.id
            this.machineNo = data!.no
            this.machineName = data!.name
            this.ownerName = data!.ownerName
            this.ownerPhoneNo = data!.ownerPhoneNo

            this.viewOnly = true
            console.log(data)
            this.viewMachine(this.machineId)
            
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

  setNew() {
    this.machineId = null
    this.clearMachineData()
    this.viewOnly = false
    this.recordMode = 'single'
  }

  setList() {
    this.clearMachineData()
    this.viewOnly = true
    this.recordMode = 'list'
  }

  clearScreen(){
    this.clearMachineData()
    this.viewOnly = false
    this.recordMode = ''
  }

  setEdit() {
    if (this.machineId === null) {
      alert("Can not edit")
      return;
    }
    this.viewOnly = false
    this.recordMode = 'single'
  }

  clearMachineData() {
    this.machineId = null
    this.machineNo = ''
    this.machineName = ''
    this.ownerName = ''
    this.ownerPhoneNo = ''
    this.machineRegNo = ''
  }


  public async addMachineService() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    if (this.machineServiceId === null || this.machineServiceId === undefined) {
      this.msg.showErrorMessage3('Workshop not defined, please select a workshop first')
    }

    var machineService = {
      machineId: this.machineId,
      serviceId: this.serviceId
    }

    await this.http.post<IMachineService>(API_URL + '/machine-services/create', machineService, options)
      .toPromise()
      .then(
        data => {
          this.machineId = data!.machineId
          this.serviceId = data!.serviceId
          this.qty = data!.qty

          console.log(data)
          this.msg.showSuccessMessage('Service created successifully')
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  machineServices: IMachineService[] = []

  async getMachine(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IMachine>(API_URL + '/machines/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.clearMachineData()

          this.machineId = data!.id
          this.machineNo = data!.no
          this.machineName = data!.name
          this.ownerName = data!.ownerName
          this.ownerPhoneNo = data!.ownerPhoneNo
          this.machineRegNo = data!.machineRegNo

          this.machineServices = data!.machineServices




          this.viewOnly = true

          this.setEdit()

          console.log(data)
        }
      )
  }

  async viewMachine(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IMachine>(API_URL + '/machines/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.clearMachineData()

          this.machineId = data!.id
          this.machineNo = data!.no
          this.machineName = data!.name
          this.ownerName = data!.ownerName
          this.ownerPhoneNo = data!.ownerPhoneNo
          this.machineRegNo = data!.machineRegNo

          this.machineServices = data!.machineServices

          this.viewOnly = true

          console.log(data)
        }
      )
  }

  triggerAddService(){
    this.recordMode = 'single'
    this.viewOnly = true
  }


  searchKey: string = ''

  serviceName: string = ''
  serviceDescription: string = ''
  serviceCode: string = ''
  servicePrice: number = 0
  serviceQty: number | string = 1
  discount: number = 0

  searchTerm: string = ''


  restaurantName: string = ''

  searchedServices: IService[] = []



  isUserTyping = false

  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getServiceLike(searchText);
    }
  }

  onServiceSelected(): void {
    const selectedService = this.searchedServices.find(
      (service) => service.name === this.searchKey
    );

    if (selectedService) {
      this.searchService(selectedService.id)
      this.searchedServices = []; // Clear suggestions after selection
    } else {
      console.warn('Selected service is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);

  }

  filteredServices: any[] = [];
  selectedService: any | null = null;
  isDropdownOpen: boolean = false;
  searchServices(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)

    }
    this.filteredServices = [];
    if (this.searchTerm.trim().length >= 2) {
      this.http
        .get<IService[]>(API_URL + '/services/get_dineables_containing?service_name_like=' + this.searchTerm, options)
        .subscribe(
          (data) => (this.filteredServices = data),
          (error) => console.error('Error fetching dineables:', error)
        )

    } else {
      this.filteredServices = [];
    }
  }

  searchService = async (id: any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.clearRestaurantService()

    await this.http.get<IService>(API_URL + '/services/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.serviceId = data!.id
          this.serviceCode = data!.code
          this.serviceName = data!.name
          this.serviceDescription = data!.description
          this.searchTerm = this.serviceName
          this.servicePrice = data!.price
          this.serviceQty = 1
        }
      )
      .catch(error => {
        console.log(error)
      }
      )
  }


  machines : IMachine[] = []


  async getAllMachinesByWorkshop() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.machines = []

    await this.http.get<IMachine[]>(API_URL + '/machines/by_workshop?workshop_id=' + this.workshopId, options)
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
      .catch(error => {
        console.log(error)
      })
  }






  salesOrderDetailId: any = null
  restaurantServiceId: any = null
  vat: number = 0
  costPriceVatIncl: number = 0
  sellingPriceVatIncl: number = 0
  costPriceVatExcl: number = 0
  sellingPriceVatExcl: number = 0
  minStock: number = 0
  maxStock: number = 0
  defaultReorderQty: number = 0
  defaultReorderLevel: number = 0
  baseUom: string = ''


  clearRestaurantService() {
    this.serviceId = null

    this.searchTerm = ''

    this.salesOrderDetailId = null
    this.searchKey = ''
    this.serviceName = ''
    this.serviceCode = ''
    this.serviceDescription = ''
    this.vat = 0
    this.costPriceVatIncl = 0
    this.sellingPriceVatIncl = 0
    this.costPriceVatExcl = 0
    this.sellingPriceVatExcl = 0
    this.minStock = 0
    this.maxStock = 0
    this.defaultReorderQty = 0
    this.defaultReorderLevel = 0
    this.baseUom = ''
    this.qty = 0
    this.discount = 0
  }

  
  getServiceLike = async (searchKey: string) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    if (searchKey.length > 2) {
      await this.http.get<IService[]>(API_URL + '/services/get_services_by_company_containing?service_name_like=' + searchKey, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.filteredServices = data!
          }
        )
        .catch(error => {
          console.log(error)
        }
        )
    } else {
      this.filteredServices = []
    }
  }

  async removeService(id: any) {
    if (!(await this.msg.showConfirmMessageDialog(
      'Confirm',
      'Are you sure you want to remove?',
      'question',
      'Yes',
      'No'
    ))) {
      return;
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get(API_URL + '/machine-services/remove?id=' + id, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.viewMachine(this.machineId)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  async confirmServices(machineId : any){
    if (!(await this.msg.showConfirmMessageDialog(
      'Confirm',
      'Are you sure you want to confirm?',
      'question',
      'Yes',
      'No'
    ))) {
      return;
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.post(API_URL + '/machine-services/confirm?machine_id=' + machineId, null ,options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          //this.getMachine(this.machineId)
          this.clearScreen()
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  selectService(service: any): void {
    this.selectedService = service
    this.searchTerm = service.name
    this.isDropdownOpen = false
    this.searchService(service.id)
    this.filteredServices = []
  }

  clearMachineService() {
    this.serviceId = null
    this.machineId = null
    this.machineServiceId = null
    this.serviceQty = ''
    this.servicePrice = 0
    this.searchTerm = ''
  }




  async addService() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var addSer = {
      serviceId: this.serviceId,
      machineId: this.machineId,
      qty: this.serviceQty,
      price: this.servicePrice
    }

    await this.http.post<IMachineService>(API_URL + '/machine-services/create', addSer, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.viewMachine(this.machineId)
          this.msg.showSuccessMessage('Added Successifully')
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
    this.clearMachineService()
  }







































  documentHeader!: any

  from: Date | string | null = null
  to: Date | string | null = null

  nickname = ''

  page: number = 1; // Initialize the current page to 1

  filterRecords: string = ''

  mode: string = ''
  availableWorkshops: IWorkshop[] = []
  selectedWorkshop: IWorkshop | null = null
  selectedWorkshopId: string | null = null
  branchId: string = ''
  workshopLoaded: boolean = false
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
  // ownerPhoneNo: string = ''
  ownerEmail: string = ''
  ownerAddress: string = ''
  comments: string = ''
  goodName: string = ''
  goodDescription: string = ''
  weight: number = 0 // In kg
  length: number = 0 // In cm
  width: number = 0 // In cm
  height: number = 0 // In cm
  startBillingAt: Date | null | string = null
  status: string = ''
  billingType: string = 'DAILY'
  billingAmount: number | null = null
  totalPrice: number | null = null
  initialQty: number = 0
  currentQty: number = 0
  workshopId: any = null
  goodTypeId: any = null
  goodTypeName: string = ''
  workshopName: string = ''



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
    this.selectedWorkshopId = localStorage.getItem('selected-workshop-id')
    if (this.selectedWorkshopId == '' || this.selectedWorkshopId == null) {
      this.workshopLoaded = false
      this.loadAvailableWorkshops()
    } else {
      this.selectedWorkshopId = localStorage.getItem('selected-workshop-id')
      this.loadSelectedWorkshop()
    }
    // this.getAllCompanyActiveGoodTypes()
  }

  loadAvailableWorkshops = async () => {
    this.availableWorkshops = []
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWorkshop[]>(API_URL + '/workshops/get_branch_available_workshops_by_user', options)
      .toPromise()
      .then(
        data => {
          this.availableWorkshops = data!
          console.log(data)
        }
      )
  }

  loadSelectedWorkshop = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWorkshop>(API_URL + '/workshops/get_selected_workshop?workshop_id=' + this.selectedWorkshopId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedWorkshop = data!
          localStorage.setItem('selected-workshop-id', this.selectedWorkshop.id.toString());
          this.workshopId = this.selectedWorkshop.id
          this.workshopLoaded = true
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-workshop-id', '');
        this.workshopLoaded = false
      }
      )
  }



  onWorkshopChange(event: any): void {
    this.selectedWorkshopId = event.target.value;
    console.log('Selected Workshop ID:', this.selectedWorkshopId);
    //alert('Selected Workshop ID: ' + this.selectedWorkshopId);
  }

  selectWorkshop() {
    //localStorage.setItem('selected-workshop-id', this.selectedWorkshopId!);
    if (this.selectedWorkshopId! === '' || this.selectedWorkshopId === null) {
      this.msg.showErrorMessage3('Please select a workshop first')
      return
    }
    this.loadSelectedWorkshop()
  }

  clearSelectedWorkshop() {
    localStorage.setItem('selected-workshop-id', '');
    this.selectedWorkshopId = ''

  }

  calculateBillingAmount() {
    if (this.initialQty == null || this.initialQty <= 0) {
      this.billingAmount = 0
    }
    this.billingAmount = parseFloat((this.totalPrice! / this.initialQty).toFixed(2));
  }

  // Alerts array to manage the notifications
  alerts: {
    type: string;
    message: string;
    link?: string
  }[] = [
      { type: 'success', message: 'Workshop selected successfully!', link: 'workshop-sales-order' },

    ];

  // Close alert method
  closeAlert(index: number) {
    this.alerts.splice(index, 1); // Remove the alert at the given index
  }

  // Add an alert for testing purposes
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }



  setExisting() {
    this.mode = 'existing'
  }

  setReleased() {
    this.mode = 'released'
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

    if (this.workshopId === null || this.workshopId === undefined) {
      this.msg.showErrorMessage3('Workshop not defined, please select a workshop first')
    }

    if (this.billingAmount === null || this.billingAmount === undefined) {
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
      workshopId: this.workshopId,
      billingType: this.billingType,
      billingAmount: this.billingAmount,
      comments: this.comments,
      goodName: this.goodName,
      goodDescription: this.goodDescription,
      goodTypeName: this.goodTypeName,
      length: this.length,
      width: this.width,
      height: this.height,
      weight: this.weight,
      initialQty: this.initialQty,
      startBillingAt: this.startBillingAt
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
    this.startBillingAt = data!.billingStartAt
    // this.workshopName = data!.workshopName
    this.status = data!.status
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
    this.billingType = 'DAILY'
    this.billingAmount = null
    this.totalPrice = null
    this.goodTypeId = null
    this.goodTypeName = ''
    this.goodName = ''
    this.goodDescription = ''
    this.comments = ''
    this.length = 0
    this.width = 0
    this.height = 0
    this.weight = 0
    this.startBillingAt = null
    this.workshopName = ''
    this.status = ''
  }



  async getAllCheckedInAndPendingStorages() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.storages = []

    await this.http.get<IStorage[]>(API_URL + '/storages/get_all_pending_or_checked_in_by_workshop?workshop_id=' + this.workshopId, options)
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

  async getAllCheckedInStorages() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.storages = []

    await this.http.get<IStorage[]>(API_URL + '/storages/get_all_checked_in_by_workshop?workshop_id=' + this.workshopId, options)
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

    await this.http.get<IStorage[]>(API_URL + '/storages/get_all_recent_checked_out_by_workshop?workshop_id=' + this.workshopId, options)
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

  async get(id: any) {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IStorage>(API_URL + '/storages/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.clearStorageData()
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

  async checkOut(id: any, no: string, descr: string): Promise<void> {


    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out storage no: ' + no + ' - ' + descr + '?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var storage = {
      id: id
      // cardNo : this.cardNo,
      // storageZoneName : this.storageZoneName,
      // startBillingAt : this.startBillingAt
    }

    await this.http.post<IStorage>(API_URL + '/storages/check_out', storage, options)
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

          this.qtyToRelease = 0

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
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Error')
          console.log(error)
        }
      )


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

  async printReleaseNote(id: any) {
    await this.getStorageGoodRelease(id)
    await this.printGatePassRcpt(this.billItems, this.storageGoodReleaseNo, 0, id)
  }


  printGatePassRcpt = async (billItems: IServiceBillItem[], receiptNo: string, cash: number, id: any) => {

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
              [{ text: 'Client Name: ' + this.storageGoodReleaseClientName, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Client Address: ' + this.storageGoodReleaseClientAddress, alignment: 'left', fontSize: 9, bold: false }],
              [{ text: 'Phone No: ' + this.storageGoodReleaseClientPhoneNo, alignment: 'left', fontSize: 9, bold: false }],
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
              // [{text : 'Cashier Comments', alignment : 'left', fontSize : 9, bold : true}],
              //[{text : this.comments, alignment : 'left', fontSize : 9, bold : false}],
              [{ text: 'Issued At: ' + this.storageGoodReleaseReleaseDate, alignment: 'left', fontSize: 9, bold: true }],
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

interface IStorageGoodReleaseDetail {
  initialQty: number
  currentQty: number
  releasedQty: number
  availableForRelease: number
}

interface IStorageGoodRelease {
  id: any
  no: string
  qty: number
  status: string
  storageId: any
  releaseDate: string
  clientName: string
  clientAddress: string
  clientPhoneNo: string
  goodName: string
  unitPrice: string
  total: string
}
