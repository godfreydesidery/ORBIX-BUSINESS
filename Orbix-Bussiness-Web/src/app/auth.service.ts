import { HttpClient, HttpHeaders } from '@angular/common/http'
import { Injectable } from '@angular/core'
import * as moment from "moment"
import { BehaviorSubject, Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { JwtHelperService } from '@auth0/angular-jwt'
import { DatePipe } from '@angular/common'
import { environment } from '../environments/environment';
import { IUser } from './domain/user'

const API_URL = environment.apiUrl;

interface IUserData{
  alias : string
}

interface IDayData{
  bussinessDate : String
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  

  helper = new JwtHelperService()
  

  private currentUserSubject: BehaviorSubject<IUser>
  public currentUser: Observable<IUser>

  public user : {
    username : string, 
    access_token : string, 
    refresh_token : string
  } = JSON.parse(localStorage.getItem('current-user')!) 


  constructor(
    private http : HttpClient,
    private datePipe : DatePipe
    ) {
    this.currentUserSubject = new BehaviorSubject<IUser>(JSON.parse(localStorage.getItem('current-user') || '{}'));
    this.currentUser = this.currentUserSubject.asObservable()
  }

  public get currentUserValue(): IUser {
    return this.currentUserSubject.value
  }

  loginUser(username : string, password : string){
    let user = new URLSearchParams()
    user.set('username', username)
    user.set('password', password)

    //remove pharmacy data
    localStorage.removeItem('selected-pharmacy-id')
    localStorage.removeItem('selected-pharmacy-code')
    localStorage.removeItem('selected-pharmacy-name')

    //remove store data
    localStorage.removeItem('selected-store-id')
    localStorage.removeItem('selected-store-code')
    localStorage.removeItem('selected-store-name')

    let options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }

    return this.http.post<any>(API_URL+'/login', user, options)
      .pipe(map(user => {
        // store user details and jwt token in local storage to keep user logged in between page refreshes
        localStorage.setItem('current-user', JSON.stringify(user))
       // this.currentUserSubject.next(user)
        let currentUser : {
          username : string, 
          access_token : string, 
          refresh_token : string
        } = JSON.parse(localStorage.getItem('current-user')!)

        if(this.tokenExpired(currentUser.access_token)){
          //should clear user information
          return
        }
                
        return user
      })); 

  }

  autoLogin(){
    let currentUser : {
      username : string, 
      access_token : string, 
      refresh_token : string
    } = JSON.parse(localStorage.getItem('current-user')!)
    if(!currentUser){
      return
    }

  }

  logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('current-user')
    //this.currentUserSubject.next(new User('', '', new Date))
    
  }

  private tokenExpired(token: string) {
    return this.helper.isTokenExpired(token)
  }



  public async loadUserSession(username : string){
      
    let currentUser : {
      username : string, 
      access_token : string, 
      refresh_token : string
    } = JSON.parse(localStorage.getItem('current-user')!)    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+currentUser.access_token)
    }

    await this.http.get<IUserData>(API_URL+'/users/load_user?username='+username, options)
    .toPromise()
    .then(
      data => {
        localStorage.setItem('user-name', data?.alias!+'')  
        localStorage.setItem('username', username)  
      }
    )

    await this.http.get<IDayData>(API_URL+'/days/get_bussiness_date', options)
    .toPromise()
    .then(
      data => {
        localStorage.setItem('system-date', data?.bussinessDate!+'')        
      },
      error => {
        console.log(error)
      }
    )
   //localStorage.setItem('system-date', '2021-12-02')
  }

  public unloadUserSession(){
    localStorage.removeItem('username')
    localStorage.removeItem('user-name')
    localStorage.removeItem('system-date')
  }

  public checkPrivilege(priv : string) : boolean{
    /**
     * Return true if a user has a certain privilege
     */
    var granted : boolean = false
    let currentUser : {
      username : string, 
      access_token : string, 
      refresh_token : string
    } = JSON.parse(localStorage.getItem('current-user')!)
    var privs : {
      privileges : string[]
    } = this.helper.decodeToken(currentUser.access_token)! 

    for(let i = 0; i < privs.privileges.length; i++){
      if(privs.privileges[i] === priv){
        return true
      }
    }
    return granted
  }
}


