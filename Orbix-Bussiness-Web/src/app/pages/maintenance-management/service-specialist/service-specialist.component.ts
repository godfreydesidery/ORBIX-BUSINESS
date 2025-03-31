import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IServiceSpecialist } from 'src/app/domain/service-specialist';
import { IUser } from 'src/app/domain/user';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-service-specialist',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './service-specialist.component.html',
  styleUrl: './service-specialist.component.scss'
})
export class ServiceSpecialistComponent {
/**Data */
    id : any = null
    nickname : string = '' 
    active : string = 'Inactive'
  
  
    /**Collections */
    serviceSpecialists : IServiceSpecialist[] = []
  
    constructor(
      private http :HttpClient,
      private auth : AuthService,
      private msg : MsgBoxService
    ) {}
  
    ngOnInit(){
      this.getAllServiceSpecialists()
      this.getAllBranchUsers()
    }
  
    async getAllServiceSpecialists(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.serviceSpecialists = []
  
      await this.http.get<IServiceSpecialist[]>(API_URL+'/service_specialists', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.serviceSpecialists.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
    }

    users : IUser[] = []

    async getAllBranchUsers(){
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      this.users = []
  
      await this.http.get<IUser[]>(API_URL+'/users', options)
      .toPromise()
      .then(
        data => {
          data?.forEach(element => {
            this.users.push(element)
          })
          console.log(data)
        }
      )
    }
  
    async get(id : any){
  
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }
      await this.http.get<IServiceSpecialist>(API_URL+'/service_specialists/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showServiceSpecialistData(data!)
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
  
      var serviceSpecialist = {
        id: this.id,
        nickname: this.nickname,
        active: this.active,
        company : {id : "" },
        branch : {id : ""},
        sn : 0
      }
  
      if(serviceSpecialist.id === null){
        /**Create new serviceSpecialist */
        await this.http.post<IServiceSpecialist>(API_URL+'/service_specialists/create', serviceSpecialist, options)
        .toPromise()
        .then(
          data => {
            this.showServiceSpecialistData(data!)
  
            console.log(data)
  
            this.getAllServiceSpecialists()
  
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
        /**Update an exiisting serviceSpecialist */
        await this.http.post<IServiceSpecialist>(API_URL+'/service_specialists/update', serviceSpecialist, options)
        .toPromise()
        .then(
          data => {
            this.showServiceSpecialistData(data!)
  
            console.log(data)
  
            this.getAllServiceSpecialists()
  
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
  
      var serviceSpecialist = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/service_specialists/activate', serviceSpecialist, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllServiceSpecialists()
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
  
      var serviceSpecialist = {
        id : id
      }
  
      await this.http.post<String>(API_URL+'/service_specialists/deactivate', serviceSpecialist, options)
        .toPromise()
        .then(
          data => {
  
            console.log(data)
  
            this.getAllServiceSpecialists()
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
  
    showServiceSpecialistData(data : IServiceSpecialist){
      this.id = data?.id
      this.nickname = data!.nickname
    }
  
    clearServiceSpecialistData(){
      this.id = null
      this.nickname = ''
    }
}
