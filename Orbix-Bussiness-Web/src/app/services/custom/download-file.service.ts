import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class DownloadFileService {

  constructor(private http: HttpClient) { }

  async downloadLabTestAttachment(filename: string): Promise<Observable<any>> {
    return this.http.get(API_URL + `/patients/download_lab_test_attachment?file_name=` + filename, { responseType: 'blob' })
    .pipe(map((response)=>{
        return {
            filename: filename,
            data: response
        };
    }));
  }

  async downloadRadiologyAttachment(filename: string): Promise<Observable<any>> {
    return this.http.get(API_URL + `/patients/download_radiology_attachment?file_name=` + filename, { responseType: 'blob' })
    .pipe(map((response)=>{
        return {
            filename: filename,
            data: response
        };
    }));
  }
}
