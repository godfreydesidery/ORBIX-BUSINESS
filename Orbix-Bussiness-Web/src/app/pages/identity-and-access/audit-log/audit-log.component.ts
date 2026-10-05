import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { IAuditLog } from 'src/app/domain/audit-log';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { trackById } from 'src/app/common/utils/track-by-id';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-audit-log',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.scss'
})
export class AuditLogComponent implements OnInit {
  trackById = trackById

  /**Filters */
  loginHistory : boolean = false // the login history view shows sign-in events only
  fromDate : string = ''
  toDate : string = ''
  category : string = ''
  outcome : string = ''
  filterRecords : string = ''

  categories : string[] = ['AUTH', 'SECURITY', 'FINANCE', 'OPERATIONS', 'INVENTORY', 'PROCUREMENT', 'SALES', 'SETTINGS']

  /**Collections */
  auditLogs : IAuditLog[] = []
  totalAuditLogs : number = 0
  auditLogsRequest : number = 0 // number of the latest list request; answers to older ones are ignored

  page : number = 1
  pageSize : number = 20
  listSearchTimer : any = null

  /**The entry opened to show its details */
  selectedAuditLog : IAuditLog | null = null

  constructor(
    private http : HttpClient,
    private auth : AuthService,
    private msg : MsgBoxService,
    private route : ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loginHistory = this.route.snapshot.data['loginHistory'] === true
    // The last seven days by default
    var today = new Date()
    var weekAgo = new Date()
    weekAgo.setDate(today.getDate() - 7)
    this.fromDate = this.dateValue(weekAgo)
    this.toDate = this.dateValue(today)
    this.getAuditLogs()
  }

  async getAuditLogs(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, filtered and searched on the server
    var page = this.page
    var request = ++this.auditLogsRequest

    // The dates are local days; the server works in UTC, so send the exact start and end instants
    var from = this.fromDate == '' ? '' : new Date(this.fromDate + 'T00:00:00').toISOString()
    var to = ''
    if(this.toDate != ''){
      var end = new Date(this.toDate + 'T00:00:00')
      end.setDate(end.getDate() + 1)
      to = end.toISOString()
    }
    var category = this.loginHistory ? 'AUTH' : this.category

    await this.http.get<IPage<IAuditLog>>(API_URL+'/audit_logs/get_page?from=' + encodeURIComponent(from) + '&to=' + encodeURIComponent(to)
      + '&category=' + category + '&outcome=' + this.outcome + '&' + pageParams(page, this.pageSize, this.filterRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page, filter or search) is ignored
        if(request != this.auditLogsRequest){
          return
        }
        // Past the last page: show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSize))
        if(page > lastPage){
          this.page = lastPage
          this.getAuditLogs()
          return
        }
        this.auditLogs = data!.content
        this.totalAuditLogs = data!.totalElements
      }
    )
    .catch(error => {
      this.msg.showErrorMessage(error, 'Could not load the audit log')
    })
  }

  pageChanged(page : number){
    this.page = page
    this.getAuditLogs()
  }

  searchList(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimer)
    this.listSearchTimer = setTimeout(() => {
      this.page = 1
      this.getAuditLogs()
    }, 300)
  }

  filterChanged(){
    this.page = 1
    this.getAuditLogs()
  }

  showDetails(auditLog : IAuditLog){
    this.selectedAuditLog = (this.selectedAuditLog?.id === auditLog.id) ? null : auditLog
  }

  /**The details as indented JSON for reading */
  formattedDetails(details : string) : string {
    if(details == null || details == ''){
      return ''
    }
    try {
      return JSON.stringify(JSON.parse(details), null, 2)
    } catch (e) {
      return details
    }
  }

  /**Download the entries matching the filters (up to 1000 rows) as a CSV file */
  async exportCsv(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    var from = this.fromDate == '' ? '' : new Date(this.fromDate + 'T00:00:00').toISOString()
    var to = ''
    if(this.toDate != ''){
      var end = new Date(this.toDate + 'T00:00:00')
      end.setDate(end.getDate() + 1)
      to = end.toISOString()
    }
    var category = this.loginHistory ? 'AUTH' : this.category
    var rows : IAuditLog[] = []
    // The server returns at most 100 rows per page
    for(var page = 1; page <= 10; page++){
      var data = await this.http.get<IPage<IAuditLog>>(API_URL+'/audit_logs/get_page?from=' + encodeURIComponent(from) + '&to=' + encodeURIComponent(to)
        + '&category=' + category + '&outcome=' + this.outcome + '&' + pageParams(page, 100, this.filterRecords), options)
        .toPromise()
        .catch(error => {
          this.msg.showErrorMessage(error, 'Could not export the audit log')
          return undefined
        })
      if(data == undefined){
        return
      }
      rows.push(...data.content)
      if(rows.length >= data.totalElements){
        break
      }
    }
    var header = ['Time', 'User', 'Category', 'Action', 'Outcome', 'Summary', 'Record', 'Reference', 'IP address', 'Details']
    var lines = [header.map(value => this.csvValue(value)).join(',')]
    rows.forEach(row => {
      lines.push([
        this.localTime(row.occurredAt), row.username, row.category, row.action, row.outcome, row.summary,
        row.entityType, row.entityRef, row.ipAddress, row.details
      ].map(value => this.csvValue(value)).join(','))
    })
    // The byte order mark tells spreadsheet programs the file is UTF-8
    var blob = new Blob(['﻿' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' })
    var link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = (this.loginHistory ? 'login-history-' : 'audit-log-') + this.fromDate + '-to-' + this.toDate + '.csv'
    link.click()
    setTimeout(() => URL.revokeObjectURL(link.href), 1000)
  }

  /**The entry's UTC time shown in the local time of this computer */
  localTime(occurredAt : string) : string {
    if(occurredAt == null){
      return ''
    }
    var date = new Date(occurredAt)
    return isNaN(date.getTime()) ? occurredAt : date.toLocaleString()
  }

  private csvValue(value : any) : string {
    var text = value == null ? '' : String(value)
    // A value that starts like a formula (e.g. a username typed at a failed sign-in) is kept as text in spreadsheets
    if(/^[=+\-@\t\r]/.test(text)){
      text = "'" + text
    }
    return '"' + text.replace(/"/g, '""') + '"'
  }

  private dateValue(date : Date) : string {
    var month = String(date.getMonth() + 1).padStart(2, '0')
    var day = String(date.getDate()).padStart(2, '0')
    return date.getFullYear() + '-' + month + '-' + day
  }
}
