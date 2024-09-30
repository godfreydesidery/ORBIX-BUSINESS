import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpHeaders, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ILabTest } from '../domain/lab-test';
import { AuthService } from '../auth.service';
import { IRadiology } from '../domain/radiology';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class UploadFileService {

  constructor(private http: HttpClient,
    private auth : AuthService) { }

  uploadLabTestAttachment(file: File, labTest : ILabTest, name : string): Observable<HttpEvent<any>> {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    const formData: FormData = new FormData();

    formData.append('file', file);

    const req = new HttpRequest('POST', API_URL + `/patients/upload_lab_test_attachment?lab_test_id=`+labTest.id+`&name=`+name, formData, {
      reportProgress: true,
      responseType: 'json'
    });

    return this.http.request(req);
  }

  uploadRadiologyAttachment(file: File, radiology : IRadiology, name : string): Observable<HttpEvent<any>> {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    const formData: FormData = new FormData();

    formData.append('file', file);

    const req = new HttpRequest('POST', API_URL + `/patients/upload_radiology_attachment?radiology_id=`+radiology.id+`&name=`+name, formData, {
      reportProgress: true,
      responseType: 'json'
    });

    return this.http.request(req);
  }

  getFiles(): Observable<any> {
    return this.http.get(API_URL + `/files`);
  }

}
