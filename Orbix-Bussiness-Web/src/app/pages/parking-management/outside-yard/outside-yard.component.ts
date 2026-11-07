import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { DataService } from '@services/custom/data.service';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { IParkingTrip, tripDateTime, tripDuration, tripReasonLabel } from 'src/app/domain/parking-trip';
import { printTripGatePass } from 'src/app/common/utils/trip-gate-pass';

const API_URL = environment.apiUrl;

/**
 * Reception: vehicles currently outside the yard on a temporary trip.
 * When the driver comes back, reception sends the vehicle in.
 */
@Component({
  selector: 'az-outside-yard',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './outside-yard.component.html',
  styleUrl: './outside-yard.component.scss'
})
export class OutsideYardComponent {
  trackById = trackById
  tripReasonLabel = tripReasonLabel
  tripDuration = tripDuration
  tripDateTime = tripDateTime

  page: number = 1
  pageSize: number = 15
  listSearchTimer: any = null
  filterRecords: string = ''

  trips: IParkingTrip[] = []
  totalTrips: number = 0
  tripsRequest: number = 0 // number of the latest list request; answers to older ones are ignored

  sendInTrip: IParkingTrip | null = null
  documentsReturned: boolean = false
  returnNotes: string = ''
  sendingIn: boolean = false

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService,
    private data: DataService
  ) {}

  ngOnInit() {
    this.getOutsideTrips()
  }

  grant(privileges: string[]): boolean {
    return this.auth.grant(privileges)
  }

  async getOutsideTrips() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var page = this.page
    var request = ++this.tripsRequest

    await this.http.get<IPage<IParkingTrip>>(API_URL + '/parkings/get_all_outside_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
      .toPromise()
      .then(
        data => {
          // An answer to an older request (another page or search) is ignored
          if (request != this.tripsRequest) {
            return
          }
          // Past the last page (vehicles came back meanwhile): show the last page instead
          var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSize))
          if (page > lastPage) {
            this.page = lastPage
            this.getOutsideTrips()
            return
          }
          this.trips = data!.content
          this.totalTrips = data!.totalElements
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Could not load vehicles outside the yard')
        }
      )
  }

  pageChanged(page: number) {
    this.page = page
    this.getOutsideTrips()
  }

  searchList() {
    clearTimeout(this.listSearchTimer)
    this.listSearchTimer = setTimeout(() => {
      this.page = 1
      this.getOutsideTrips()
    }, 300)
  }

  openSendIn(trip: IParkingTrip) {
    this.sendInTrip = trip
    this.documentsReturned = false
    this.returnNotes = ''
  }

  async sendIn() {
    if (!this.sendInTrip || this.sendingIn) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var args = {
      parkingId: this.sendInTrip.parkingId,
      documentsReturned: this.documentsReturned,
      returnNotes: this.returnNotes
    }
    this.sendingIn = true
    await this.http.post<IParkingTrip>(API_URL + '/parkings/send_in', args, options)
      .toPromise()
      .then(
        data => {
          this.msg.showSuccessMessage('Vehicle received back into the yard (' + tripDuration(data!.minutesOut) + ' outside)')
          this.getOutsideTrips()
        }
      )
      .catch(
        error => {
          this.msg.showErrorMessage(error, 'Could not send in')
        }
      )
    this.sendingIn = false
  }

  async reprintGatePass(trip: IParkingTrip) {
    var header: any = await this.data.getBranchReceiptHeaderWithNoTinAndVrn(trip.no)
    printTripGatePass(trip, header)
  }
}
