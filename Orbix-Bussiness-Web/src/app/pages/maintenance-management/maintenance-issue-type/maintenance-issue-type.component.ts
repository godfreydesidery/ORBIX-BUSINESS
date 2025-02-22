import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IMaintenanceIssueType } from 'src/app/domain/maintenance-issue-type';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-maintenance-issue-type',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './maintenance-issue-type.component.html',
  styleUrl: './maintenance-issue-type.component.scss'
})
export class MaintenanceIssueTypeComponent {
  /**Data */
    id : any = null
    code : string = ''
    name : string = '' 
    active : string = 'Inactive'
  
    dailyPrice : number = 0;
  
    /**Collections */
    maintenanceIssueTypes : IMaintenanceIssueType[] = []
  
    constructor(
      private http :HttpClient,
      private auth : AuthService,
      private msg : MsgBoxService
    ) {}
  
    ngOnInit(){
      this.getAllMaintenanceIssueTypes()
    }
  
    async getAllMaintenanceIssueTypes(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.maintenanceIssueTypes = []
  
      await this.http.get<IMaintenanceIssueType[]>(API_URL+'/maintenance_issue_types', options)
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
  
    async get(id : any){
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IMaintenanceIssueType>(API_URL+'/maintenance_issue_types/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showMaintenanceIssueTypeData(data!)
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
    }
  
  
  
    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var maintenanceIssueType = {
        id: this.id,
        code: this.code,
        name: this.name,
        dailyPrice : this.dailyPrice,
  
        active: this.active,
  
        company : {id : "" },
  
        branch : {id : ""},
  
        sn : 0
      }
  
      if(maintenanceIssueType.id === null){
        /**Create new maintenanceIssueType */
        await this.http.post<IMaintenanceIssueType>(API_URL+'/maintenance_issue_types/create', maintenanceIssueType, options)
        .toPromise()
        .then(
          data => {
            this.showMaintenanceIssueTypeData(data!)
  
            console.log(data)
  
            this.getAllMaintenanceIssueTypes()
  
            this.msg.showSuccessMessage('Type created successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
      }else{
        /**Update an exiisting maintenanceIssueType */
        await this.http.post<IMaintenanceIssueType>(API_URL+'/maintenance_issue_types/update', maintenanceIssueType, options)
        .toPromise()
        .then(
          data => {
            this.showMaintenanceIssueTypeData(data!)
  
            console.log(data)
  
            this.getAllMaintenanceIssueTypes()
  
            this.msg.showSuccessMessage('Type updated successifully')
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
  
      var maintenanceIssueType = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/maintenance_issue_types/activate', maintenanceIssueType, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllMaintenanceIssueTypes()
            this.msg.showSuccessMessage('Type activated successifully')
  
  
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
  
      var maintenanceIssueType = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/maintenance_issue_types/deactivate', maintenanceIssueType, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllMaintenanceIssueTypes()
            this.msg.showSuccessMessage('Type deactivated successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    showMaintenanceIssueTypeData(data : IMaintenanceIssueType){
      this.id = data?.id
      this.code = data!.code
      this.name = data!.name
    }
  
    clearMaintenanceIssueTypeData(){
      this.id = null
      this.code = ''
      this.name = ''
      this.dailyPrice = 0
    }
}
