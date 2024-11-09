import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, ViewEncapsulation, ViewChild, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
//import { ICompany } from '@services/custom/data.service';
import { DatatableComponent, NgxDatatableModule, SelectionType } from '@swimlane/ngx-datatable';
import { AuthService } from 'src/app/auth.service';
import { IBranch } from 'src/app/domain/branch';
import { ICompany } from 'src/app/domain/company';
import { IRole } from 'src/app/domain/role';
import { IUser } from 'src/app/domain/user';
import { DirectivesModule } from 'src/app/theme/directives/directives.module';

import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-user',
  standalone: true,
  imports: [
    DirectivesModule,
    NgxDatatableModule,
    CommonModule,
    FormsModule
  ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class UserComponent {
  public usernameLocked     : boolean = true
  public passwordLocked     : boolean = true
  public passwordConfLocked : boolean = true
  public codeLocked       : boolean = true
  public firstNameLocked    : boolean = true
  public middleNameLocked   : boolean = true
  public lastNameLocked     : boolean = true
  public aliasLocked        : boolean = true
  
  public enableSearch : boolean = false
  public enableDelete : boolean = false
  public enableSave   : boolean = false

  public searchKey       : any
  public id              : any
  public username        : string
  public password        : string
  public confirmPassword : string
  public code          : string
  public firstName       : string
  public middleName      : string
  public lastName        : string
  public nickname           : string
  public active          : boolean

  public roles           : IRole[]

  public users           : IUser[]

  filterRecords : string = ''

  type : string = ''
  companyId : any = null
  companyCode : any = ''
  companyName : string = ''
  branchId : any = null
  branchCode : any = ''
  branchName : string = ''

 

  constructor(private auth : AuthService,
    private http :HttpClient,
    private msg : MsgBoxService
    //private spinner : NgxSpinnerService,
    ) {

  this.searchKey       = ''
  this.id              = null
  this.username        = ''
  this.password        = ''
  this.confirmPassword = ''
  this.code          = ''
  this.firstName       = ''
  this.middleName      = ''
  this.lastName        = ''
  this.nickname           = ''
  this.active          = true
  this.roles           = []
  this.users           = []


  }  
  getUserData(): any {
    var userRoles : IRole[] = []
    this.roles.forEach(role => { /**Get the roles */
      if(role.granted == true){
        userRoles.push(role)
      }
    })
    return {
      id          : this.id,
      username    : this.username,
      password    : this.password,
      code      : this.code,
      firstName   : this.firstName,
      middleName  : this.middleName,
      lastName    : this.lastName,
      nickname    : this.nickname,
      active      : this.active,
      company : {
        id : this.companyId,
        name : this.companyName
      } ,
      branch : {
        id : this.branchId,
        name : this.branchName
      },
      roles       : userRoles,
      type : this.type,
      
    }
  }

  async ngOnInit(): Promise<void> {
    await this.getUsers()
    await this.getRoles()
    await this.getAllCompanies()
    await this.getAllBranches()
  }

  

  async saveUser(){
    /**
      * Create a single user:
      * First, validate inputs, then create user
      */
    if(this.validateInputs() == false){
      return
    }
    
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    
    if (this.id == null){
      /**Create a new user */
      //this.spinner.show()  
      await this.http.post(API_URL+'/users/create', this.getUserData(), options)
      //.pipe(finalize(() => this.spinner.hide()))
      .toPromise()
      .then(
        data => {
          this.showUser(data)
          this.msg.showSuccessMessage('User created successifuly')
          this.getUsers()
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Could not create user')
        }
      )   
    }else{
      /**Update an existing user */
      //this.spinner.show()
      await this.http.put(API_URL+'/users/update', this.getUserData(), options)
      //.pipe(finalize(() => this.spinner.hide()))
      .toPromise()
      .then(
        data => {
          console.log(data)
          //this.msgBox.showSuccessMessage('User updated successifuly')
          this.getUsers()
          this.msg.showSuccessMessage('User updated successifuly')
        }
      )
      .catch(
        error => {
          console.log(error);
          //this.msgBox.showErrorMessage(error, 'Could not update user')
          this.msg.showErrorMessage(error, 'Could not update user')
        }
      )  
    }
  }

  async getRoles(){  
  /**Get all roles */
    let options = {
      headers : new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.get<IRole[]>(API_URL+'/roles', options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {
        data?.forEach(
          element => {
            this.roles.push(element)
          }
        )
      }
    )
    .catch(error => {
      console.log(error)
    })
  }

  async getUsers(){
    this.users = []
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.get<IUser[]>(API_URL+'/users', options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {
        data?.forEach(
          element => {
            this.users.push(element)
          }
        )
        console.log(data)
      }
    )
    .catch(error => {
      this.msg.showErrorMessage(error, 'Could not load users')
    })
    return 
  }

  async getUser(key: string) {
    this.searchKey = key
    this.clearFields()
    this.username = this.searchKey

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.get(API_URL+'/users/get_user?username='+this.searchKey, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data=>{
        this.showUser(data)
        this.lockInputs()
      }
    )
    .catch(
      error=>{
        console.log(error)   
        this.msg.showErrorMessage(error, 'User not found')   
      }
    )
  }

  async get(id : any) {
    this.clearFields()
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.get(API_URL+'/users/get?id='+id, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data=>{
        this.showUser(data)
        this.lockInputs()
      }
    )
    .catch(
      error=>{
        console.log(error)   
        this.msg.showErrorMessage(error, 'User not found')
      }
    )
  }

  async deleteUser(){
    if(this.id == null){
      this.msg.showErrorMessage3('No user selected, please select a user to delete')
      return
    }
    if(await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to delete this user?', 'question', 'Yes', 'No') == false){
      return
    }
    
    let options = {
      headers : new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.delete(API_URL+'/users/delete?id='+this.id, options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      () => {
        this.clearFields()
        this.msg.showSuccessMessage('User deleted successfully')
        return true
      }
    )
    .catch(
      error => {
        console.log(error)
        //this.msgBox.showErrorMessage(error, 'Could not delete user')
        this.msg.showErrorMessage(error, 'Could not delete user')
        return false
      }
    )
  }

  showUser(user : any){
    /**
     * Display user details, takes a json user object
     * Args: user object
     */
    this.id         = user['id']
    this.username   = user['username']
    this.code     = user['code']
    this.firstName  = user['firstName']
    this.middleName = user['middleName']
    this.lastName   = user['lastName']
    this.nickname      = user['nickname']
    this.active     = user['active']
    this.showUserRoles(this.roles, user['roles'])
  }

  showUserRoles(roles : IRole[], userRoles : IRole[]){
    /**
     * Display user roles, the roles for that particular user are checked
     * args: roles-global user roles, userRoles-roles for a specific user
     */
    /** First uncheck all roles */
    this.clearRoles()
    /** Now, check the respective  roles */
    userRoles.forEach(userRole => {
      roles.forEach(role => {        
        if(role.name === userRole.name){
          role.granted = true
        }
      })
    })
    this.roles = roles
  }

  clearRoles(){
    /**Uncheck all roles */
    this.roles.forEach(role => {
      role.granted = false
    })
  }

  validateInputs() : boolean{
    let valid : boolean = true
    /**Validate username */
    if(this.username == ''){
      this.msg.showErrorMessage3('Empty username not allowed, please fill in the username field')
      return false
    }

    /**Validate Password */
    if(this.id == null){
      if(this.password == ''){
        this.msg.showErrorMessage3('Empty password not allowed for new user')
        return false
      }
      if(this.password != this.confirmPassword){
        this.msg.showErrorMessage3('Password and Password confirmation do not match')
        return false
      }
    }else{
      if(this.password != this.confirmPassword && (this.password != '' || this.confirmPassword != '')){
        this.msg.showErrorMessage3('Password and Password confirmation do not match')
        return false
      }
    }
    if(this.firstName == '' || this.lastName == ''){
      this.msg.showErrorMessage3('First name, last name and nickname are required fields')
      return false
    }
    return valid
  }


  async activate(id : any){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    var user = {
      id : id
    }

    await this.http.post<String>(API_URL+'/users/activate', user, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getUsers()

          this.msg.showSuccessMessage('User activated successifully')

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

    var user = {
      id : id
    }

    await this.http.post<String>(API_URL+'/users/deactivate', user, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.getUsers()

          this.msg.showSuccessMessage('User deactivated successifully')

        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  clearFields(){
    /**Clear the specified fields */
    this.id               = null
    this.username         = ''
    this.password         = ''
    this.confirmPassword  = ''
    this.code           = ''
    this.firstName        = ''
    this.middleName       = ''
    this.lastName         = ''
    this.nickname         = ''
    this.active           = false
    this.clearRoles()
    this.enableSave = true
  }

  unlockInputs(){
    /**Unlock the specified fields */
    this.usernameLocked      = false
    this.passwordLocked      = false
    this.passwordConfLocked  = false
    this.codeLocked        = false
    this.firstNameLocked     = false
    this.middleNameLocked    = false
    this.lastNameLocked      = false
    this.aliasLocked         = false
  }

  lockInputs(){
    /**Lock the specified fields */
    this.usernameLocked      = true
    this.passwordLocked      = true
    this.passwordConfLocked  = true
    this.codeLocked        = true
    this.firstNameLocked     = true
    this.middleNameLocked    = true
    this.lastNameLocked      = true
    this.aliasLocked         = true
  }

  public grant(privilege : string[]) : boolean{
    /**Allow user to perform an action if the user has that priviledge */
    var granted : boolean = false
    privilege.forEach(
      element => {
        if(this.auth.checkPrivilege(element)){
          granted = true
        }
      }
    )
    return granted
  }

  checkUserType(type : string){
    if(type != 'COMPANY-USER'){
      this.companyName = ''
    }
  }



  companies : ICompany[] = []
  async getAllCompanies(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    this.companies = []

    await this.http.get<ICompany[]>(API_URL+'/companies', options)
    .toPromise()
    .then(
      data => {
        data?.forEach(element => {
          this.companies.push(element)
        })
        console.log(data)
      }
    )
  }

  branches : IBranch[] = []
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

  test(id : any){
    alert(id)
  }


}