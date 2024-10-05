import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { IBranch } from 'src/app/domain/branch';
import { ICompany } from 'src/app/domain/company';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-branch',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './branch.component.html',
  styleUrl: './branch.component.scss'
})
export class BranchComponent {
  /**Data */
  id : any = null
  code : string = ''
  name : string = ''
  level : string = ''
  type : string = ''
  active : string = 'Inactive'

  physicalAddress : string = ''
  postalCode : string = ''
  postalAddress : string = ''
  telephone : string = ''
  mobile : string = ''
  email : string = ''
  website : string = ''
  fax : string = ''
  city : string = ''
  state : string = ''
  country : string = ''
  managerName : string = ''
  openingHours : string = ''
  numberOfStaff : number = 0
  dateEstablished : Date
  notes : string = ''

 
  company : ICompany
  parentBranch : IBranch

  /**Collections */

  branches : IBranch[] = []

  /**Identifiers */
  companyId : string = ''
  parentBranchId : string = ''
  companyName : string

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllBranches()
  }

  async getAllBranches(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.branches = []

    await this.http.get<IBranch[]>(API_URL+'/branches', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.branches.push(element)
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
    await this.http.get<IBranch>(API_URL+'/branches/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showBranchData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var branch = {
      id : this.id,
      code : this.code,
      name : this.name,
      level : this.level,
      type : this.type,

      city : this.city,
      state : this.state,
      country : this.country,
      managerName : this.managerName,
      openingHours : this.openingHours,
      numberOfStaff : this.numberOfStaff,
      dateEstablished : this.dateEstablished,
      physicalAddress : this.physicalAddress,
      postalCode : this.postalCode,
      postalAddress : this.postalAddress,
      telephone : this.telephone,
      mobile : this.mobile,
      email : this.email,
      website : this.website,
      fax : this.fax,

      notes : this.notes
    }

    if(branch.id === null){
      /**Create new branch */
      await this.http.post<IBranch>(API_URL+'/branches/create', branch, options)
      .toPromise()
      .then(
        data => {
          this.showBranchData(data!)

          console.log(data)

          this.getAllBranches()

          alert('Branch created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
    }else{
      /**Update an existing branch */
      await this.http.post<IBranch>(API_URL+'/branches/update', branch, options)
      .toPromise()
      .then(
        data => {
          this.showBranchData(data!)

          console.log(data)

          this.getAllBranches()

          alert('Branch updated successifully')
        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
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

    await this.http.post<String>(API_URL+'/branches/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBranches()

          alert('Branch activated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
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

    await this.http.post<String>(API_URL+'/branches/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getAllBranches()

          alert('Branch deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
  }

  showBranchData(data : IBranch){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.level = data!.level
    this.type = data!.type

    this.city = data!.city
    this.state = data!.state
    this.country = data!.country
    this.country = data!.country
    this.managerName = data!.managerName
    this.openingHours = data!.openingHours
    this.numberOfStaff = data!.numberOfStaff
    this.dateEstablished = data!.dateEstablished
    this.physicalAddress = data!.physicalAddress
    this.postalCode = data!.postalCode
    this.postalAddress = data!.postalAddress
    this.telephone = data!.telephone
    this.mobile = data!.mobile
    this.email = data!.email
    this.website = data!.website
    this.fax = data!.fax

    this.notes = data!.notes
  }

  clearCompanyData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.level = ''
    this.type = ''

    this.city = ''
    this.state = ''
    this.country = ''
    this.country = ''
    this.managerName = ''
    this.openingHours = ''
    this.numberOfStaff = 0
    this.dateEstablished!
    this.physicalAddress = ''
    this.postalCode = ''
    this.postalAddress = ''
    this.telephone = ''
    this.mobile = ''
    this.email = ''
    this.website = ''
    this.fax = ''

    this.notes = ''
  }
}
