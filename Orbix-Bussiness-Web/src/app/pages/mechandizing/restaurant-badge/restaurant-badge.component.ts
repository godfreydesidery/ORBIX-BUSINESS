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
import { IRestaurant } from 'src/app/domain/restaurant';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { IRestaurantDineable } from 'src/app/domain/restaurant-dineable';
import { IDineable } from 'src/app/domain/dineable';
import { IRestaurantBadge } from 'src/app/domain/restaurant-badge';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';



const API_URL = environment.apiUrl;

@Component({
  selector: 'az-restaurant-badge',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-badge.component.html',
  styleUrl: './restaurant-badge.component.scss'
})
export class RestaurantBadgeComponent {
  trackById = trackById
  restaurantId: any
  restaurantBadges: IRestaurantBadge[] = []
  totalRestaurantBadges : number = 0
  restaurantBadgesRequest : number = 0 // number of the latest list request; answers to older ones are ignored
  searchKey: string = ''
  restaurantBadgeId: any = null
  restaurantBadgeCode: string = ''
  selectedRestaurant: IRestaurant
  restaurantName: string = ''
  searchedBadges: IRestaurantBadge[] = []

  isUserTyping: boolean = true; // Flag to detect user typing
  page: number = 1; // Initialize the current page to 1
  pageSize : number = 15
  listSearchTimer : any = null
  filterRecords: string = ''
  selectedOption: string = '';
  options: string[] = ['Option 1', 'Option 2', 'Option 3'];

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private printer: PosReceiptPrinterService,
    private msg: MsgBoxService,
    private data: DataService,
    private route: ActivatedRoute
  ) { }

  async ngOnInit(): Promise<void> {
    // Retrieve the restaurant_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.restaurantId = params['restaurant_id'];
      console.log('Restaurant ID:', this.restaurantId);
    });
    await this.loadSelectedRestaurant()
    await this.getAllBadges()
  }


  async getAllBadges(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, searched on the server against the shown columns
    var page = this.page
    var request = ++this.restaurantBadgesRequest

    await this.http.get<IPage<IRestaurantBadge>>(API_URL+'/restaurants/get_all_badges_page?restaurant_id=' + this.restaurantId + '&' + pageParams(page, this.pageSize, this.filterRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.restaurantBadgesRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSize))
        if(page > lastPage){
          this.page = lastPage
          this.getAllBadges()
          return
        }
        var sn = (page - 1) * this.pageSize + 1
        data!.content.forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        this.restaurantBadges = data!.content
        this.totalRestaurantBadges = data!.totalElements
      }
    )
  }

  pageChanged(page : number){
    this.page = page
    this.getAllBadges()
  }

  searchList(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimer)
    this.listSearchTimer = setTimeout(() => {
      this.page = 1
      this.getAllBadges()
    }, 300)
  }



  clearRestaurantBadge() {
    // this.restaurantBadgeId = null

    // this.dineableName = ''

  }

  loadSelectedRestaurant = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IRestaurant>(API_URL + '/restaurants/get_selected_restaurant?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.restaurantName = data!.name
          this.selectedRestaurant = data!

        }
      )
      .catch(error => {
        console.log(error)
      }
      )
  }

  generateRestaurantBadge = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.restaurantBadgeCode = ''

    await this.http.get<IStringData>(API_URL + '/restaurants/generate_badge_code', options)
      .toPromise()
      .then(
        data => {
          console.log(data)

          this.restaurantBadgeCode = data!.restaurantBadgeCode
          
        }
      )
      .catch(error => {
        console.log(error)
      }
      )
    
  }

  saveRestaurantBadge = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var badge  = {
      id: this.restaurantBadgeId,
      code: this.restaurantBadgeCode,
      restaurantId : this.restaurantId
    }

    await this.http.post<IRestaurantBadge>(API_URL + '/restaurants/create_badge', badge, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllBadges()
          this.msg.showSuccessMessage("Successiful")
          
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage3("Error creating badge")
      }
      )
  }

  async activate(id: any) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }
      var badge = { id: id }
      await this.http.post<String>(API_URL + '/restaurant-badges/activate', badge, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.getAllBadges()
            this.msg.showSuccessMessage('Badge activated successifully')
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }
  
    async deactivate(id: any) {
      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
      }
      var badge = { id: id }
      await this.http.post<String>(API_URL + '/restaurant-badges/deactivate', badge, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.getAllBadges()
            this.msg.showSuccessMessage('Badge deactivated successifully')
          }
        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    }

}

export interface IStringData{
  restaurantBadgeCode : string
}
