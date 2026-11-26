import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { AuthService } from 'src/app/auth.service';
import { IParking, IParkingInspection, IParkingSignature } from 'src/app/domain/parking';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

/**
 * A parking's steps for a View dialog: 1. Inspection, 2. Registration, 3. Check-in and 4. Modifications, each with
 * its details and signatures. The page calls load(id) whenever it opens its View, so the steps are always current.
 */
@Component({
  selector: 'app-parking-steps',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parking-steps.component.html'
})
export class ParkingStepsComponent {

  parking : IParking | null = null
  signatures : IParkingSignature[] = []
  loaded : boolean = false

  constructor(private http : HttpClient, private auth : AuthService) {}

  async load(id : any){
    this.parking = null
    this.signatures = []
    this.loaded = false
    if(id == null) return
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    await Promise.all([
      this.http.get<IParking>(API_URL+'/parkings/get?id=' + id, options).toPromise()
        .then(data => this.parking = data ?? null),
      this.http.get<IParkingInspection>(API_URL+'/parkings/get_inspection?id=' + id, options).toPromise()
        .then(data => this.signatures = data?.signatures ?? [])
    ])
    .then(() => this.loaded = true)
    .catch(error => console.log(error))
  }

  get status() : string {
    return this.parking?.status ?? ''
  }

  signaturesOf(stage : string) : IParkingSignature[] {
    return this.signatures.filter(s => s.stage === stage)
  }

  roleLabel(role : string) : string {
    switch(role){
      case 'INSPECTOR': return 'Inspector'
      case 'DRIVER': return 'Driver'
      case 'REGISTRAR': return 'Registrar'
      case 'CHECK_IN_OFFICER': return 'Checked in by'
      case 'MODIFIER': return 'Modified by'
      default: return role
    }
  }

  // The API sends booleans as text; the same reading as the Parking page's View
  yesNo(value : any) : string {
    return value == true ? 'YES' : 'NO'
  }

  deviceText() : string {
    const p : any = this.parking
    if(!p || p.deviceStatus != true) return 'Not attached'
    const count = p.deviceCount ? Number(p.deviceCount) : null
    return count ? 'Attached: ' + count + (count === 1 ? ' device' : ' devices') : 'Attached'
  }
}
