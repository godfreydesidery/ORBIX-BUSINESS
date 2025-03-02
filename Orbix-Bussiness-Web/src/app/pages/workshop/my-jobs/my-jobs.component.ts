import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';
import { IGoodType } from 'src/app/domain/good-type';
import { IMaintenanceJobCardIssue } from 'src/app/domain/maintenance-job-card-issue';

import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl; @Component({
  selector: 'az-my-jobs',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './my-jobs.component.html',
  styleUrl: './my-jobs.component.scss'
})
export class MyJobsComponent {

  maintenanceJobCardIssues: IMaintenanceJobCardIssue[] = []

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.loadMyJobs()
  }

  async loadMyJobs() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.maintenanceJobCardIssues = []


    await this.http.get<IMaintenanceJobCardIssue[]>(API_URL + '/maintenance_job_card_issues/get_my_jobs', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          this.maintenanceJobCardIssues = data!
          this.maintenanceJobCardIssues.forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        })
  }

  maintenanceJobCardIssue: IMaintenanceJobCardIssue | undefined | null
  issueId: any = null
  comments: string = ''

  async loadIssue(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.maintenanceJobCardIssue = null
    this.issueId = null
    this.comments = ''


    await this.http.get<IMaintenanceJobCardIssue>(API_URL + '/maintenance_job_card_issues/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.maintenanceJobCardIssue = data!
          this.issueId = data!.id
          this.comments = data!.comments
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        })
  }

  async saveComments() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var issue = {
      id: this.issueId,
      comments: this.comments
    }


    await this.http.post(API_URL + '/maintenance_job_card_issues/save_comments', issue, options)
      .toPromise()
      .then(
        data => {
          //this.msg.showSuccess('Comments saved successfully')
          this.loadMyJobs()
          console.log(data)
        }
      )
      .catch(
        error => {
          console.log(error)
        })
  }

}
