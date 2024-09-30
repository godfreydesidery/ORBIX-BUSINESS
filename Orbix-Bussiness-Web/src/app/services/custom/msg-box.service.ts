import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import swal, { SweetAlertIcon } from 'sweetalert2';


@Injectable({
  providedIn: 'root'
})
export class MsgBoxService {

  constructor() { }

  public showSuccessMessage(message : string){
    swal.fire({
      icon: 'info',
      title: 'Success',
      text: message
    })
  }

  public showErrorMessage3(message : string){
    swal.fire({
      icon: 'error',
      title: 'Error',
      text: message
    })
  }

  public showSimpleErrorMessage(message : string){
    swal.fire({
      icon: 'error',
      title: 'Error',
      text: message
    })
  }

  public async showConfirmMessageDialog(title : string, text : string, icon : SweetAlertIcon | undefined, confirmButtonText : string, cancelButtonText : string) : Promise<boolean>{
    var confirmed = false
    await swal.fire({
      title : title,
      text : text,
      icon : icon,
      width : '500',
      showCancelButton : true,
      confirmButtonText : confirmButtonText,
      confirmButtonColor : '#009688',
      cancelButtonText : cancelButtonText,
      cancelButtonColor : '#e5343d'
    }).then(async (result) => {
      if(result.value) {
        confirmed = true
      } else { 
        confirmed = false
      }
    })
    return confirmed
  }

  public showErrorMessage(error : HttpErrorResponse, msg : string){
    var message : string = msg
    let code : number = error['status']
    switch(code){
      /**
       * Client error http status codes
       */
      case 400:
        message = 'Bad request!';
      break;
      case 401:
        message = 'Unauthorized. Wrong username and password';
      break;
      case 402:
        message = 'Payment required';
      break;
      case 403:
        message = 'Forbidden. Access restricted or service unavailable. Please contact administrator';
      break;
      case 404:
        message = 'Not Found. Resource not found';
      break;
      case 405:
        message = 'Method Not Allowed';
      break;
      case 406:
        message = 'Not Acceptable';
      break;
      case 407:
        message = 'Proxy Authentication Required';
      break;
      case 408:
        message = 'Request Timeout';
      break;
      case 409:
        message = 'Conflict';
      break;
      case 410:
        message = 'Gone';
      break;
      case 411:
        message = 'Length Required';
      break;
      case 412:
        message = 'Precondition Failed';
      break;
      case 413:
        message = 'Payload too Large';
      break;
      case 414:
        message = 'Request-URI Too Long';
      break;
      case 415:
        message = 'Unsupported Media Type';
      break;
      case 416:
        message = 'Requested Range not Satisfiable';
      break;
      case 417:
        message = 'Expectetion Failed';
      break;
      case 418:
        message = 'I am a Teapot';
      break;
      case 421:
        message = 'Misdirected Request';
      break;
      case 422:
        message = 'Unprocessable Entity';
      break;
      case 423:
        message = 'Locked';
      break;
      case 424:
        message = 'Failed Dependency';
      break;
      case 426:
        message = 'Upgrade Required';
      break;
      case 428:
        message = 'Precondition Required';
      break;
      case 429:
        message = 'Too Many Requests';
      break;
      case 431:
        message = 'Request Header Fields too Large';
      break;
      case 444:
        message = 'Connection closed without Response';
      break;
      case 451:
        message = 'Unavailable for Legal Reasons';
      break;
      case 499:
        message = 'Client Closed Request';
      break;
      /**
       * Server error http status codes
       */
      case 500:
        message = 'Internal Server Error. Please cross-check request or contact system administrator';
      break;
      case 501:
        message = 'Not Implemented';
      break;
      case 502:
        message = 'Bad Gateway';
      break;
      case 503:
        message = 'Service Unavailable';
      break;
      case 504:
        message = 'Gateway Timeout';
      break;
      case 505:
        message = 'HTTP Version not Supported';
      break;
      case 506:
        message = 'Variant also negotiates';
      break;
      case 507:
        message = 'Insufficient storage';
      break;
      case 508:
        message = 'Loop Detected';
      break;
      case 510:
        message = 'Not Extended';
      break;
      case 511:
        message = 'Network Authentication Required';
      break;
      case 512:
        message = 'Network Connect Timeout Error';
      break;
      default:
        message = 'Unknown error has occured. Please contact System Administrator!';
      break;
    }
    if(typeof(error['error']) === 'string'){
      message = error['error']
    }
    swal.fire({
      icon: 'error',
      title: 'Error',
      text: message
    })
  }
}
