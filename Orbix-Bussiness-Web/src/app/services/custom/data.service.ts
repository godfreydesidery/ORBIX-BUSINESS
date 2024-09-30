import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Byte } from 'src/custom-packages/util';
import { Injectable } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
//import { NgxSpinnerService } from 'ngx-spinner';
import { finalize } from 'rxjs';
import { environment } from 'src/environments/environment';
//import { ICompanyProfile } from '../domain/company';
import { AuthService } from 'src/app/auth.service';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class DataService {
  

  constructor(private http : HttpClient, 
              private auth : AuthService, 
              private sanitizer: DomSanitizer
              ) {
    
  }

  async getLogo() : Promise<string> {
    var logo : any = ''
    await this.http.get<ICompany>(API_URL+'/company_profile/get_logo')
    .toPromise()
    .then(
      res => {
        var retrieveResponse : any = res
        var base64Data = retrieveResponse.logo
        logo = 'data:image/png;base64,'+base64Data
      }
    )
    .catch(error => {
      console.log(error)
    }) 
    return logo
  }

  

  

 

  

  

  
}




export interface ICompany{
  logo : Byte[]
} 

