import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DataService } from '@services/custom/data.service';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { PosReceiptPrinterService } from '@services/custom/pos-receipt-printer.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IRestaurant } from 'src/app/domain/restaurant';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';
import { JwtHelperService } from '@auth0/angular-jwt';


var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-select-restaurant',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-restaurant.component.html',
  styleUrl: './select-restaurant.component.scss'
})
export class SelectRestaurantComponent {
  underStock: number = 0
  outofStock: number = 0


  availableRestaurants: IRestaurant[] = []

  selectedRestaurant: IRestaurant | null = null
  selectedRestaurantId: string | null = null

  branchId: string = ''

  restaurantLoaded: boolean = false

  lpoPage: number = 1; // Initialize the current page to 1
  grnPage: number = 1; // Initialize the current page to 1
  filterLpoRecords: string = ''
  filterGrnRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
  ) { } //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

  ngOnInit() {
    this.selectedRestaurantId = localStorage.getItem('selected-restaurant-id')
    if (this.selectedRestaurantId == '' || this.selectedRestaurantId == null) {
      this.restaurantLoaded = false
      this.loadAvailableRestaurants()
    } else {
      this.selectedRestaurantId = localStorage.getItem('selected-restaurant-id')
      this.loadSelectedRestaurant()

    }
    this.getUnderstock()
    this.getOutofstock()
  }

  loadAvailableRestaurants = async () => {

    this.availableRestaurants = []

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurant[]>(API_URL + '/restaurants/get_branch_available_restaurants_by_user', options)
      .toPromise()
      .then(
        data => {
          this.availableRestaurants = data!
          console.log(data)
        }
      )
  }

  loadSelectedRestaurant = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurant>(API_URL + '/restaurants/get_selected_restaurant?restaurant_id=' + this.selectedRestaurantId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedRestaurant = data!
          localStorage.setItem('selected-restaurant-id', this.selectedRestaurant.id.toString());
          this.restaurantLoaded = true

          this.getUnderstock()
          this.getOutofstock()
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-restaurant-id', '');
        this.restaurantLoaded = false
      }
      )
  }

  onRestaurantChange(event: any): void {
    this.selectedRestaurantId = event.target.value;
    console.log('Selected Restaurant ID:', this.selectedRestaurantId);
    //alert('Selected Restaurant ID: ' + this.selectedRestaurantId);
  }

  selectRestaurant() {
    //localStorage.setItem('selected-restaurant-id', this.selectedRestaurantId!);
    if (this.selectedRestaurantId! === '' || this.selectedRestaurantId === null) {
      this.msg.showErrorMessage3('Please select a restaurant first')
      return
    }
    this.loadSelectedRestaurant()
  }

  clearSelectedRestaurant() {
    localStorage.setItem('selected-restaurant-id', '');
    this.selectedRestaurantId = ''

  }

  lpos: ILpo[] = []
  async getAllPendingOrders() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.lpos = []

    await this.http.get<ILpo[]>(API_URL + '/lpos/get_all_visible_by_restaurant?restaurant_id=' + this.selectedRestaurantId, options)
      .toPromise()
      .then(
        data => {
          this.lpos = [...data!]

          console.log(data)
        }
      )
  }

  grns: IGrn[] = []
  async getAllPendingGrns() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.grns = []

    await this.http.get<IGrn[]>(API_URL + '/grns/get_all_visible_by_restaurant?restaurant_id=' + this.selectedRestaurantId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.grns.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
      .catch(error => {
        console.log(error)
      })
  }


  lpoId: any = null

  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<ILpo>(API_URL + '/lpos/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)
          console.log(data)
          this.lpoId = data!.id

          this.router.navigate(['/app/mechandizing/restaurant-lpo'], {
            queryParams: {
              restaurant_id: this.selectedRestaurantId,
              lpo_id: id
            }
          });
        }
      )
  }

  async getGrn(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<ILpo>(API_URL + '/grns/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)
          console.log(data)
          this.lpoId = data!.id

          this.router.navigate(['/app/mechandizing/restaurant-grn'], {
            queryParams: {
              restaurant_id: this.selectedRestaurantId,
              grn_id: id
            }
          });
        }
      )
  }

  getUnderstock = async () => {

    this.underStock = 0

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<number>(API_URL + '/restaurant_products/get_check_under_stock_by_restaurant?restaurant_id=' + this.selectedRestaurantId, options)
      .toPromise()
      .then(
        data => {
          this.underStock = data!
          console.log(data)
        }
      )
  }

  getOutofstock = async () => {

    this.outofStock = 0

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<number>(API_URL + '/restaurant_products/get_check_out_of_stock_by_restaurant?restaurant_id=' + this.selectedRestaurantId, options)
      .toPromise()
      .then(
        data => {
          this.outofStock = data!
          console.log(data)
        }
      )
  }






  // Alerts array to manage the notifications
  alerts: {
    type: string;
    message: string;
    link?: string
  }[] = [
      { type: 'success', message: 'Restaurant selected successfully!', link: 'restaurant-sales-order' },

    ];

  // Close alert method
  closeAlert(index: number) {
    this.alerts.splice(index, 1); // Remove the alert at the given index
  }

  // Add an alert for testing purposes
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }

  grant(privileges: string[]): boolean {
        /** Allow user to perform an action if the user has that privilege */
        
        const currentUser = JSON.parse(localStorage.getItem('current-user')!);
        if (!currentUser || !currentUser.access_token) {
            console.error('No valid user or access token found.');
            return false;
        }
    
        const decodedToken = new JwtHelperService().decodeToken(currentUser.access_token);
        if (!decodedToken || !decodedToken.privileges) {
            console.error('No privileges found in the token.');
            return false;
        }
        const userPrivileges = decodedToken.privileges as string[];
      
        // Check if any of the required privileges exist in the user's privileges
        return privileges.some(privilege => userPrivileges.includes(privilege));
    }

}
