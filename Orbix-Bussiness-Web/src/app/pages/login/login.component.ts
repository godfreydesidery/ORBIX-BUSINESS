import { Component, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

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
  public email: FormControl;
  public password: FormControl;

  constructor(router: Router, fb: FormBuilder) {
    this.router = router;
    this.form = fb.group({
      'email': ['', Validators.compose([Validators.required, emailValidator])],
      'password': ['', Validators.compose([Validators.required, Validators.minLength(6)])]
    });

    this.email = this.form.controls['email'] as FormControl;;
    this.password = this.form.controls['password'] as FormControl;;
  }

  public onSubmit(values: Object): void {
    if (this.form.valid) {
      console.log(values);
      this.router.navigate(['pages/dashboard']);
    }
  }
}

export function emailValidator(control: FormControl): {[key: string]: any} | null {
  const emailRegexp = /[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/;    
  if (control.value && !emailRegexp.test(control.value)) {
      return { invalidEmail: true };
  }
  return null
}
