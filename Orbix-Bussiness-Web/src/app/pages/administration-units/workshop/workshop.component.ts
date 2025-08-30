import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IWorkshop } from 'src/app/domain/workshop';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-workshop',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './workshop.component.html',
  styleUrl: './workshop.component.scss'
})
export class WorkshopComponent {
  /**Data */
    id : any = null
    code : string = ''
    name : string = ''
    locationName : string = ''
    workshopCategory : string = 'NORMAL'
    active : string = 'Inactive'
  
    workshop : IWorkshop
  
    /**Collections */
  
    workshops : IWorkshop[] = []
  
    /**Identifiers */
    workshopId : string = ''
  
    constructor(
      private http :HttpClient,
      private auth : AuthService,
      private msg : MsgBoxService
    ) {}
  
    ngOnInit(){
      this.getAllWorkshops()
    }
  
    async getAllWorkshops(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.workshops = []
  
      await this.http.get<IWorkshop[]>(API_URL+'/workshops', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.workshops.push(element)
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
      await this.http.get<IWorkshop>(API_URL+'/workshops/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showWorkshopData(data!)
          console.log(data)
        }
      )
    }
  
  
  
    public async save(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
  
      var workshop = {
        id : this.id,
        code : this.code,
        name : this.name,
        locationName : this.locationName,
        workshopCategory : this.workshopCategory
      }
  
      if(workshop.id === null){
        /**Create new workshop */
        await this.http.post<IWorkshop>(API_URL+'/workshops/create', workshop, options)
        .toPromise()
        .then(
          data => {
            this.showWorkshopData(data!)
  
            console.log(data)
  
            this.getAllWorkshops()
  
            this.msg.showSuccessMessage('Workshop created successifully')
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
      }else{
        /**Update an existing workshop */
        await this.http.post<IWorkshop>(API_URL+'/workshops/update', workshop, options)
        .toPromise()
        .then(
          data => {
            this.showWorkshopData(data!)
  
            console.log(data)
  
            this.getAllWorkshops()
  
            this.msg.showSuccessMessage('Workshop updated successifully')
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
  
      var company = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/workshops/activate', company, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllWorkshops()
  
            this.msg.showSuccessMessage('Workshop activated successifully')
  
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
  
      var company = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/workshops/deactivate', company, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllWorkshops()
            this.msg.showSuccessMessage('Workshop deactivated successifully')
  
  
          }
  
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    showWorkshopData(data : IWorkshop){
      this.id = data?.id
      this.code = data!.code
      this.name = data!.name
      this.locationName = data!.locationName
      this.workshopCategory = data!.workshopCategory
      
    }
  
    clearCompanyData(){
      this.id = null
      this.code = ''
      this.name = ''
      this.locationName = ''
    
    }
}
