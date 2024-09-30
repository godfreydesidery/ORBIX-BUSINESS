import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';

import { AuthService } from './auth.service';
import { environment } from '../environments/environment';

const API_URL = environment.apiUrl;


@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  helper = new JwtHelperService()

    constructor(
        private http :HttpClient,
        private router: Router,
        private authService: AuthService
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
            // logged in so return true
            let currentUser : {
                username : string, 
                access_token : string, 
                refresh_token : string
              } = JSON.parse(localStorage.getItem('current-user')!)
              if(currentUser == null){
                return false
              }
              if(this.tokenExpired(currentUser.refresh_token) == true){
                //clear the user session
                //reload application

                alert('Session has expired, a fresh login is required')

                localStorage.removeItem('current-user')
                window.location.reload()
                return false
              }

              if(this.tokenExpired(currentUser.access_token) == true){ 
                
                  //request for a new token using the refresh token
                  var header = {
                    headers: new HttpHeaders()
                      .set('Authorization',  'Bearer '+currentUser.refresh_token)
                  }

                  this.http.get<any>(API_URL+'/token/refresh', header)
                  .toPromise()
                  .then(
                      data => {
                        currentUser.access_token = data['refresh_token']
                        localStorage.removeItem('current-user')
                        localStorage.setItem('current-user', JSON.stringify(currentUser))
                      }
                  ).catch(
                    error => {
                      console.log('not authenticated, reloading')
                      alert('User verification failed. Please log in afresh.')
                      localStorage.removeItem('current-user')
                      window.location.reload();
                      return false
                    }
                  )      
                  //if token request successiful, continue, if unsuccesiful, redirect to login with error
              }
            return true;
        }

        // not logged in so redirect to login page with the return url
        window.location.reload();
        return false;
    }

    private tokenExpired(token: string) {
      return this.helper.isTokenExpired(token)
    }


}