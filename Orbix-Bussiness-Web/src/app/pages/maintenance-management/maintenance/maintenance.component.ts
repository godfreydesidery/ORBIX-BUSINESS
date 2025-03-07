import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { NgxPaginationModule } from 'ngx-pagination';
import { IMaintenance } from 'src/app/domain/maintenance';
import { IVehicleEquipmentType } from 'src/app/domain/vehicle-equipment-type';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import * as pdfMake from 'pdfmake/build/pdfmake';

import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { IMaintenanceJobCard } from 'src/app/domain/maintenance-job-card';
import { IMaintenanceIssueType } from 'src/app/domain/maintenance-issue-type';
import { IServiceSpecialist } from 'src/app/domain/service-specialist';
import { IMaintenanceJobCardIssue } from 'src/app/domain/maintenance-job-card-issue';


const API_URL = environment.apiUrl;
@Component({
  selector: 'az-maintenance',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule
  ],
  templateUrl: './maintenance.component.html',
  styleUrl: './maintenance.component.scss'
})
export class MaintenanceComponent {
  documentHeader! : any
  
    page: number = 1; // Initialize the current page to 1
  
    filterRecords : string = ''
  
  
  
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
  
    // Vehicle or Equipment Information
    registrationNo: string = ''
    chasisNo: string = ''
    leftFrontLamp: string = ''
    rightFrontLamp: string = ''
    leftRearLamp: string = ''
    rightRearLamp: string = ''
    leftSideMirror: string = ''
    rightSideMirror: string = ''
    leftWiper: string = ''
    rightWiper: string = ''
    backWiper: string = ''
    fuelCap: string = ''
    spareTire: string = ''
    battery: string = ''
    starter: string = ''
    aerial: string = ''
    wheelCap: string = ''
    roundMirror: string = ''
    tireIndicator: string = ''
    hasKeys : string = ''
  
    deviceStatus : string = 'ATTACHED'
  
    vehicleEquipmentColor : string = ''
  
    comments : string = ''
  
    cardNo : string = ''
  
    //image: Byte[]
  
    status: string = "PENDING"
  
    
  
    startBillingAt : Date | null
  
    // Foreign keys
    
    branchId: any = ''
    companyId: any = ''
  
    maintenanceZoneName : string = ''
  
    
  
    /**Collections */
    maintenances : IMaintenance[] = []
  
    vehicleEquipmentTypes  : IVehicleEquipmentType[] = []
    maintenanceIssueTypes : IMaintenanceIssueType[] = []
    serviceSpecialists : IServiceSpecialist[] = []

    maintenanceJobCard : IMaintenanceJobCard

    maintenanceJobCardId : any = null
    maintenanceJobCardNo : string = ''
    maintenanceId: any = null
    maintenanceNo : string = ''
    vehicleEquipmentTypeId: any = ''
    vehicleEquipmentTypeName : string = ''
    vehicleEquipmentName : string = ''
    ownerName : string = ''
  
    constructor(
      private http :HttpClient,
      private auth : AuthService,
      private data : DataService,
      private msg : MsgBoxService
    ) {}
  
    ngOnInit(){
      this.getAllPendingOrCheckedInMaintenances()   
      this.getAllCompanyActiveVehicleAndEquipmentTypes()
      this.getAllCompanyActiveMaintenanceIssueTypes()
      this.getAllBranchServiceSpecialists()
    }
  
    async getAllPendingOrCheckedInMaintenances(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.maintenances = []
  
      await this.http.get<IMaintenance[]>(API_URL+'/maintenances/get_all_pending_or_checked_in', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.reverse().forEach(element => {
            element.sn = sn
            this.maintenances.push(element)
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
  
    async getAllCompanyActiveVehicleAndEquipmentTypes(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.vehicleEquipmentTypes = []
  
      await this.http.get<IVehicleEquipmentType[]>(API_URL+'/vehicle_equipment_types/get_all_company_active', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.vehicleEquipmentTypes.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }


    async getAllCompanyActiveMaintenanceIssueTypes(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.maintenanceIssueTypes = []
  
      await this.http.get<IMaintenanceIssueType[]>(API_URL+'/maintenance_issue_types/get_all_company_active', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.maintenanceIssueTypes.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }

    async getAllBranchServiceSpecialists(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.serviceSpecialists = []
  
      await this.http.get<IServiceSpecialist[]>(API_URL+'/service_specialists/get_all_branch_active', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            this.serviceSpecialists.push(element)
          })
          console.log(data)
        }
      )
    }
  
    selectedOption: string = '';
    options: string[] = ['Option 1', 'Option 2', 'Option 3'];
  
    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenance = {
        id: this.maintenanceId,
        no: this.maintenanceNo,
        ownerFirstName: this.ownerFirstName,
        ownerMiddleName: this.ownerMiddleName,
        ownerLastName: this.ownerLastName,
        ownerCompanyName: this.ownerCompanyName,
        ownerIdNo: this.ownerIdNo,
        ownerIdType: this.ownerIdType,
        ownerPhoneNo: this.ownerPhoneNo,
        ownerEmail: this.ownerEmail,
        ownerAddress: this.ownerAddress,
    
        comments : this.comments,
    
        // Vehicle or Equipment Information
        registrationNo: this.registrationNo,
        chasisNo: this.chasisNo,
  
        leftFrontLamp: this.leftFrontLamp === 'YES' ? 1 : 0,
        rightFrontLamp: this.rightFrontLamp === 'YES' ? 1 : 0,
        leftRearLamp: this.leftRearLamp === 'YES' ? 1 : 0,
        rightRearLamp: this.rightRearLamp === 'YES' ? 1 : 0,
        leftSideMirror: this.leftSideMirror === 'YES' ? 1 : 0,
        rightSideMirror: this.rightSideMirror === 'YES' ? 1 : 0,
        leftWiper: this.leftWiper === 'YES' ? 1 : 0,
        rightWiper: this.rightWiper === 'YES' ? 1 : 0,
        backWiper: this.backWiper === 'YES' ? 1 : 0,
        fuelCap: this.fuelCap === 'YES' ? 1 : 0,
        spareTire: this.spareTire === 'YES' ? 1 : 0,
        battery: this.battery === 'YES' ? 1 : 0,
        starter: this.starter === 'YES' ? 1 : 0,
        aerial: this.aerial === 'YES' ? 1 : 0,
        wheelCap: this.wheelCap === 'YES' ? 1 : 0,
        roundMirror: this.roundMirror === 'YES' ? 1 : 0,
        tireIndicator: this.tireIndicator === 'YES' ? 1 : 0,
        hasKeys : this.hasKeys === 'YES' ? 1 : 0,
        vehicleEquipmentTypeName : this.vehicleEquipmentTypeName,
  
        deviceStatus : this.deviceStatus === 'ATTACHED' ? 1 : 0,
    
        vehicleEquipmentColor : this.vehicleEquipmentColor,
  
        cardNo : this.cardNo,
    
        maintenanceZoneName : this.maintenanceZoneName
      }
  
      console.log(maintenance)
  
      if(maintenance.id === null){
        /**Create new maintenance */
        await this.http.post<IMaintenance>(API_URL+'/maintenances/create', maintenance, options)
        .toPromise()
        .then(
          data => {
            this.showMaintenanceData(data!)
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Maintenance created successifully')
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
      }else{
        /**Update an exiisting maintenance */
        await this.http.post<IMaintenance>(API_URL+'/maintenances/update', maintenance, options)
        .toPromise()
        .then(
          data => {
            this.showMaintenanceData(data!)
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Maintenance updated successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
      }
    }
  
    
  
    async activate(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenance = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/maintenances/activate', maintenance, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Maintenance activated successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    async checkIn(id : any){

      this.clearJobCard()

      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenance = {
        id : id,
        // cardNo : this.cardNo,
        // hasKeys : this.hasKeys === 'YES' ? 1 : 0,
      }
  
      await this.http.post<IMaintenance>(API_URL+'/maintenances/check_in', maintenance, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Checked in Successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    async checkOut(){
  
      if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to check out?', 'question', 'Yes', 'No') == false){
        return
      }
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenance = {
        id : this.maintenanceId,
        cardNo : this.cardNo,
        maintenanceZoneName : this.maintenanceZoneName,
        startBillingAt : this.startBillingAt
      }
  
      await this.http.post<IMaintenance>(API_URL+'/maintenances/check_out', maintenance, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Checked out Successifully')
            this.printGatePass()
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    async deactivate(id : any){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenance = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/maintenances/deactivate', maintenance, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllPendingOrCheckedInMaintenances()
  
            this.msg.showSuccessMessage('Maintenance deactivated successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }


  async createOrLoadMaintenanceJobCard(maintenanceId: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }


    this.clearJobCard()

    var maintenance = {
      id: maintenanceId
    }

    await this.http.post<IMaintenanceJobCard>(API_URL + '/maintenances/create_maintenance_job_card', maintenance, options)
      .toPromise()
      .then(
        data => {

          this.maintenanceJobCard = data!

          this.showJobCard(data!)

          console.log(data)

          this.msg.showSuccessMessage('Card created/fetched successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showJobCard(data: IMaintenanceJobCard) {
    this.maintenanceJobCard = data
    this.maintenanceJobCardId = data!.id
    this.maintenanceJobCardNo = data!.no
    this.maintenanceId = data!.maintenanceId
    this.maintenanceNo = data!.maintenanceNo
    this.vehicleEquipmentName = data!.vehicleEquipmentName
    this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName
    this.ownerName = data!.ownerName
    this.ownerPhoneNo = data!.ownerPhoneNo
    this.chasisNo = data!.chasisNo
    this.hasKeys = data!.hasKeys
  }

  closeJobCard(){
    this.maintenanceJobCardId = null
    this.maintenanceJobCardNo = ''
    this.maintenanceId = null
    this.maintenanceNo = ''
    this.vehicleEquipmentName = ''
    this.vehicleEquipmentTypeName = ''
    this.ownerName = ''
  }

  clearJobCard() {
    this.maintenanceJobCardId = null
    this.maintenanceJobCardNo = ''
    this.maintenanceId = null
    this.maintenanceNo = ''
    this.vehicleEquipmentName = ''
    this.vehicleEquipmentTypeName = ''
    this.ownerName = ''
  }
  
    showMaintenanceData(data : IMaintenance){
      this.maintenanceId = data?.id;
      this.maintenanceNo = data!.no;
      this.ownerFirstName = data?.ownerFirstName;
      this.ownerMiddleName = data?.ownerMiddleName;
      this.ownerLastName = data?.ownerLastName;
      this.ownerCompanyName = data?.ownerCompanyName;
      this.ownerIdNo = data?.ownerIdNo;
      this.ownerIdType = data?.ownerIdType;
      this.ownerPhoneNo = data?.ownerPhoneNo;
      this.ownerEmail = data?.ownerEmail;
      this.ownerAddress = data?.ownerAddress;
  
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
      this.vehicleEquipmentTypeName = data!.vehicleEquipmentTypeName,
      this.deviceStatus = data!.deviceStatus == true ? 'ATTACHED' : 'NOT-ATTACHED'
  
      this.comments = data!.comments,
  
      this.vehicleEquipmentColor = data!.vehicleEquipmentColor
  
       this.cardNo = data!.cardNo  
    }
  
    clearMaintenanceData(){
      this.maintenanceId = null;
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
  
      // Vehicle or Equipment Information
      this.registrationNo = ''
      this.chasisNo = ''
      this.leftFrontLamp = ''
      this.rightFrontLamp = ''
      this.leftRearLamp = ''
      this.rightRearLamp = ''
      this.leftSideMirror = ''
      this.rightSideMirror = ''
      this.leftWiper = ''
      this.rightWiper = ''
      this.backWiper = ''
      this.fuelCap = ''
      this.spareTire = ''
      this.battery = ''
      this.starter = ''
      this.aerial = ''
      this.wheelCap = ''
      this.roundMirror = ''
      this.tireIndicator = ''
      this.vehicleEquipmentTypeName = ''
  
      this.comments = ''
  
      this.deviceStatus = 'ATTACHED'
  
      this.vehicleEquipmentColor = ''
  
      this.maintenanceZoneName = ''
  
      this.hasKeys = 'YES'
    }


    maintenanceJobCardIssueName : string = ''
    maintenanceJobCardIssueDescription : string = ''
    maintenanceJobCardIssueTypeName : string = ''
    maintenanceJobCardIssueSpecialistUser : string = ''

    maintenanceJobCardIssuePrice : number | string = ''
    maintenanceJobCardIssueNoOfDays : number = 1

    async createMaintenanceJobCardIssue(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }
  
  
      var maintenanceJobCard = {
        id : this.maintenanceJobCardId,
        no : this.maintenanceJobCardNo,
        maintenanceJobCardIssueRequest : {
          id: null,
          no : null,
          name : this.maintenanceJobCardIssueName,
          description : this.maintenanceJobCardIssueDescription,
          maintenanceIssueTypeName : this.maintenanceJobCardIssueTypeName,
          serviceSpecialistUserNickname : this.maintenanceJobCardIssueSpecialistUser,
          price : this.maintenanceJobCardIssuePrice,
          noOfDays : this.maintenanceJobCardIssueNoOfDays
        }
        
      }
  
      await this.http.post<IMaintenanceJobCardIssue>(API_URL + '/maintenance_job_card_issues/create', maintenanceJobCard, options)
        .toPromise()
        .then(
          data => {
  
            
            console.log(data)
  
            this.msg.showSuccessMessage('Issue created/fetched successifully')

            this.createOrLoadMaintenanceJobCard(this.maintenanceId)
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )

        this.clearIssueData()
        
    }

    async openIssue(id : any, no : string){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var issue = {
        id : id,
        no : no
      }

      await this.http.post(API_URL + '/maintenance_job_card_issues/open', issue, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.msg.showSuccessMessage('Issue opened successifully')

            this.createOrLoadMaintenanceJobCard(this.maintenanceId)
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )

    }

    async openAllIssues(){
      for(let individualIssue of this.maintenanceJobCard.maintenanceJobCardIssues){

        if(individualIssue.status != 'PENDING'){
          continue
        }

        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
        }
  
        var issue = {
          id : individualIssue.id,
          no : individualIssue.no
        }
  
        await this.http.post(API_URL + '/maintenance_job_card_issues/open', issue, options)
          .toPromise()
          .then(
            data => {
    
              console.log(data)
    
            this.msg.showSuccessMessage('Sent successifully')
  
              // this.createOrLoadMaintenanceJobCard(this.maintenanceId)
    
            }
    
          )
          .catch(
            error => {
              console.log(error)
              // this.msg.showErrorMessage(error, 'Error')
            }
          )
      }
      this.closeJobCard()
      //await this.createOrLoadMaintenanceJobCard(this.maintenanceId)
    }

    async removeIssue(id : any, no : string){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }

      var issue = {
        id : id,
        no : no
      }

      await this.http.post(API_URL + '/maintenance_job_card_issues/remove', issue, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            //this.msg.showSuccessMessage('Issue removed successifully')

            this.createOrLoadMaintenanceJobCard(this.maintenanceId)
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )

    }

    clearIssueData(){  
      this.maintenanceJobCardIssueName = ''
      this.maintenanceJobCardIssueDescription = ''
      this.maintenanceJobCardIssueTypeName = ''
      this.maintenanceJobCardIssueSpecialistUser = ''
      this.maintenanceJobCardIssuePrice = ''
      this.maintenanceJobCardIssueNoOfDays = 1
    }
  
  
    printGatePass1() {
      const documentDefinition = {
        content: [
          { text: 'Davagan', fontSize: 18, bold: true },
          { text: 'Gate Pass', fontSize: 18, bold: true },
          { text: 'Vehicle Name', fontSize: 18, bold: true },
          { text: 'Color', fontSize: 18, bold: true },
          { text: 'Chasis No', fontSize: 18, bold: true },
          { text: 'Reg No', fontSize: 18, bold: true },
  
          { text: 'Payments: OK', fontSize: 18, bold: true },
          { text: 'Cashier Coments: OK', fontSize: 18, bold: true },
  
          { text: 'Issue Date: ', fontSize: 18, bold: true },
  
          { text: 'Gate Pass issued By:', fontSize: 18, bold: true },
  
        ]
      };
      pdfMake.createPdf(documentDefinition).open();
    }
  
    printGatePass = async () => {
      this.documentHeader = await this.data.getDocumentHeader()
      var header = ''
      var footer = ''
      var title  = 'Gate Pass'
      var logo : any = ''
      var total : number = 0
      var discount : number = 0
      var tax : number = 0
      
      /*this.report.forEach((element) => {
        total = total + element.amount
        discount = discount + element.discount
        tax = tax + element.tax
        var detail = [
          {text : formatDate(element.date, 'yyyy-MM-dd', 'en-US'), fontSize : 9, fillColor : '#ffffff'}, 
          {text : element.amount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},
          {text : element.discount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},  
          {text : element.tax.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#ffffff'},
        ]
        report.push(detail)
      })*/
      /*var detailSummary = [
        {text : 'Total', fontSize : 9, fillColor : '#CCCCCC'}, 
        {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},
        {text : discount.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},  
        {text : tax.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', fillColor : '#CCCCCC'},        
      ]
      report.push(detailSummary)*/
      const docDefinition : any = {
        header: '',
        footer: function (currentPage: { toString: () => string; }, pageCount: string) {
          return currentPage.toString() + " of " + pageCount;
        },
        //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
          content : [
            {
              columns : 
              [
                this.documentHeader
              ]
            },
            '  ',
            '  ',
            {text : title, fontSize : 14, bold : true, alignment : 'center'},
            this.data.getHorizontalLine(),
           
            // {text : title, fontSize : 12, bold : true},
            '  ',
            {
              layout : 'noBorders',
              table : {
                widths : [75, 300],
                body : [
                  [
                    {text : 'Vehicle Name', fontSize : 9}, 
                    {text : this.vehicleEquipmentTypeName, fontSize : 9} 
                  ],
                  [
                    {text : 'Maintenance Ref No', fontSize : 9}, 
                    {text : this.maintenanceNo, fontSize : 9} 
                  ],
                  [
                    {text : 'Color', fontSize : 9}, 
                    {text : this.vehicleEquipmentColor, fontSize : 9} 
                  ],
                  [
                    {text : 'Chasis No', fontSize : 9}, 
                    {text :this.chasisNo, fontSize : 9} 
                  ],
                  [
                    {text : 'Reg No', fontSize : 9}, 
                    {text : this.registrationNo, fontSize : 9} 
                  ],
  
                  [
                    {text : '', fontSize : 9}, 
                    {text : '', fontSize : 9} 
                  ],
  
                  [
                    {text : 'Cashier Comments', fontSize : 9}, 
                    {text : this.comments, fontSize : 9} 
                  ],
                  [
                    {text : '', fontSize : 9}, 
                    {text : '', fontSize : 9} 
                  ],
  
                  [
                    {text : 'Gate Pass Issued By', fontSize : 9}, 
                    {text : '...............................', fontSize : 9} 
                  ],
                ]
              },
            },
            '  ',
            //{
              //layout : 'noBorders',
              //table : {
                  //headerRows : 1,
                  //widths : [100, 100, 100, 100, 100],
                  //body : report
              //}
          //},                   
        ]     
      };
      pdfMake.createPdf(docDefinition).print()
    }
}
