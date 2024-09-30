import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { AuthService } from 'src/app/auth.service';
import { ICompany } from 'src/app/domain/company';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-company',
  standalone: true,
  imports: [
   
  ],
  templateUrl: './company.component.html',
  styleUrl: './company.component.scss'
})
export class CompanyComponent {

  id : any = null
  name : string = ''
  brandName : string = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}

  ngOnInit(){}

  public async save(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var company : ICompany = {
      id : null,
      name : this.name
    }

    await this.http.post<ICompany>(API_URL+'/companies/save', company, options)
    .toPromise()
    .then(

    )
    .catch(
      error => {
        console.log(error)
      }
    )
    

    alert('save!')
  }
}
