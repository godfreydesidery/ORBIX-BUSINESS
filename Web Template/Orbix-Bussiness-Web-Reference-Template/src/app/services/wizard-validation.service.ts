import { Injectable } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";

@Injectable()
export class WizardValidationService {

  static emailValidator(control: FormControl): {[key: string]: any} | null {
    const emailRegexp = /[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/;    
    if (control.value && !emailRegexp.test(control.value)) {
        return { invalidEmail: true };
    }
    return null
}

  static matchingPasswords(passwordKey: string, passwordConfirmationKey: string) {
    return (group: FormGroup) => {
      let password = group.controls[passwordKey];
      let passwordConfirmation = group.controls[passwordConfirmationKey];
      if (password.value !== passwordConfirmation.value) {
        return passwordConfirmation.setErrors({ mismatchedPasswords: true })
      }
    }
  }

  static numberValidator(control: FormControl): { [key: string]: any } {
    var onlyNumberRegexp = /.*[^0-9].*/;
    if (control.value && onlyNumberRegexp.test(control.value)) {
      return { invalidNumber: true };
    }
    return { invalidNumber: false }
  }

}
