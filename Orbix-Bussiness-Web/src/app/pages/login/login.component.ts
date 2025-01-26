import { Component, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from 'src/app/auth.service';
import { first } from 'rxjs';
import { MsgBoxService } from '@services/custom/msg-box.service';

@Component({
  selector: 'az-login',
  standalone: true,
  imports: [
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class LoginComponent {
  public router: Router;
  public form: FormGroup;
  public username: FormControl;
  public password: FormControl;

  status : string = ''

   

  constructor(
    private auth : AuthService,
    router: Router, 
    fb: FormBuilder,
    private msg : MsgBoxService
  ) {
    this.router = router;
    this.form = fb.group({
      'username': ['', Validators.compose([Validators.required])],
      'password': ['', Validators.compose([Validators.required, Validators.minLength(6)])]
    });

    this.username = this.form.controls['username'] as FormControl;;
    this.password = this.form.controls['password'] as FormControl;;
  }

  public onSubmit(values: Object): void {
    if (this.form.valid) {
      console.log(values);
      this.router.navigate(['pages/dashboard']);
    }
  }


  async loginUser(){
    localStorage.removeItem('user-name')
    localStorage.removeItem('system-date')

    if(this.username.value == '' || this.password.value == ''){ 
      this.msg.showErrorMessage3('Please fill in your username and password')
      //this.msgBox.showErrorMessage3('Please fill in your username and password')
      return
    }
    this.status = 'Loading... Please wait.'
    await this.auth.loginUser(this.username.value, this.password.value)
      .pipe(first())
      .toPromise()
      .then(
        async () => {
          this.status = 'Loading User... Please wait.'
          await this.auth.loadUserSession(this.username.value)
          this.status = 'Authenticated'
          window.location.reload()
        }
      )
      .catch(error => {
        this.status = ''
        localStorage.removeItem('current-user')
        this.msg.showErrorMessage(error, 'Invalid login')
        //this.msgBox.showErrorMessage(error, 'Invalid username and password')
        console.log(error)
        return
      })    
  }

  resetFields(){
    this.username.setValue('')
    this.password.setValue('')
  }


}

export function emailValidator(control: FormControl): {[key: string]: any} | null {
  const emailRegexp = /[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/;    
  if (control.value && !emailRegexp.test(control.value)) {
      return { invalidEmail: true };
  }
  return null
}
