import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from 'src/app/auth.service';
import { ICompany } from 'src/app/domain/company';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-company',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './company.component.html',
  styleUrl: './company.component.scss'
})
export class CompanyComponent {

  /**Data */
  id : any = null
  code : string = ''
  name : string = ''
  brandName : string = ''
  contactName : string = ''
  domain : string = ''
  symbol : string = ''
  active : boolean = false

  legalType : string = ''
  industry : string = ''
  country : string = ''
  timeZone : string = ''
  foundingDate : Date
  logo : Byte[]
  tin : string = ''
  vrn : string = ''
  physicalAddress : string = ''
  postalCode : string = ''
  postalAddress : string = ''
  telephone : string = ''
  mobile : string = ''
  email : string = ''
  website : string = ''
  fax : string = ''

  /**Collections */
  companies : ICompany[] = []

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){
    this.getAllCompanies()
  }

  async getAllCompanies(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.companies = []

    await this.http.get<ICompany[]>(API_URL+'/companies', options)
    .toPromise()
    .then(
      data => {
        var sn = 1
        data?.forEach(element => {
          element.sn = sn
          this.companies.push(element)
          sn = sn + 1
        })
      }
    )
  }

  async get(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await this.http.get<ICompany>(API_URL+'/companies/get?id=' + id, options)
    .toPromise()
    .then(
      data => {
        this.showCompanyData(data!)
        console.log(data)
      }
    )
  }



  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var company : ICompany = {
      id: this.id,
      code: this.code,
      name: this.name,
      brandName: this.brandName,
      contactName: this.contactName,
      domain: this.domain,
      active: this.active,

      symbol: '',
      legalType: '',
      industry: '',
      country: '',
      timeZone: '',
      foundingDate: undefined!,
      logo: [],
      tin: '',
      vrn: '',
      physicalAddress: '',
      postalCode: '',
      postalAddress: '',
      telephone: '',
      mobile: '',
      email: '',
      website: '',
      fax: '',

      sn : 0
    }

    if(company.id === null){
      /**Create new company */
      await this.http.post<ICompany>(API_URL+'/companies/create', company, options)
      .toPromise()
      .then(
        data => {
          this.id = data?.id
          this.code = data!.code
          this.name = data!.name

          console.log(data)

          this.getAllCompanies()

          alert('Company created successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          alert('An error has occured')
        }
      )
    }else{
      /**Update an exiisting company */
      await this.http.post<ICompany>(API_URL+'/companies/update', company, options)
      .toPromise()
      .then(
        data => {
          this.id = data?.id
          this.code = data!.code
          this.name = data!.name

          console.log(data)

          this.getAllCompanies()

          alert('Company updated successifully')
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

  showCompanyData(data : ICompany){
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.brandName = data!.brandName
    this.contactName = data!.contactName
    this.domain = data!.domain
  }

  clearCompanyData(){
    this.id = null
    this.code = ''
    this.name = ''
    this.brandName = ''
    this.contactName = ''
    this.domain = ''
  }
}
