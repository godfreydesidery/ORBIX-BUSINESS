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
import { IRestaurantDineable } from 'src/app/domain/restaurant-dineable';
import { IDineable } from 'src/app/domain/dineable';
import { IRestaurantAgent } from 'src/app/domain/restaurant-agent';
import { IRestaurantBadge } from 'src/app/domain/restaurant-badge';


var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-restaurant-agent',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './restaurant-agent.component.html',
  styleUrl: './restaurant-agent.component.scss'
})
export class RestaurantAgentComponent {
  restaurantId: any
  restaurantAgents: IRestaurantAgent[] = []
  searchKey: string = ''
  restaurantAgentId: any = null
  restaurantAgentName: string = ''
  restaurantAgentPhone: string = ''
  selectedRestaurant: IRestaurant
  restaurantName: string = ''
  searchedAgents: IRestaurantAgent[] = []

  isUserTyping: boolean = true; // Flag to detect user typing
  page: number = 1; // Initialize the current page to 1
  filterRecords: string = ''
  selectedOption: string = '';
  options: string[] = ['Option 1', 'Option 2', 'Option 3'];

  selectedBadgeId: any = null
  selectedAgentId: any = null

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
    await this.getAllAgents()
  }


  async getAllAgents() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.restaurantAgents = []

    await this.http.get<IRestaurantAgent[]>(API_URL + '/restaurants/get_all_agents?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.restaurantAgents.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  availableBadges: IRestaurantBadge[] = []
  async loadAvailableBadgesByRestaurant(agentId: any) {

    this.selectedBadgeId = null
    this.selectedAgentId = agentId

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.availableBadges = []

    await this.http.get<IRestaurantBadge[]>(API_URL + '/restaurants/get_available_badges_by_restaurant_id?restaurant_id=' + this.restaurantId, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            this.availableBadges.push(element)
            sn = sn + 1
          })
          console.log(data)
        }
      )

  }



  clearRestaurantAgent() {
    // this.restaurantAgentId = null

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

  async assignBadge() {

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var assignData = {
      restaurantAgentId: this.selectedAgentId,
      restaurantBadgeId: this.selectedBadgeId
    }

    await this.http.post<null>(API_URL + '/restaurant-agents/assign-badge-to-agent', assignData, options)
      .toPromise()
      .then(
        data => {
          this.msg.showSuccessMessage("Successiful")
          this.getAllAgents()
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage3("Error Assigning")
      }
      )

  }

  // generateRestaurantAgent = async () => {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
  //   }

  //   this.restaurantAgentCode = ''

  //   await this.http.get<IStringData>(API_URL + '/restaurants/generate_agent_code', options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         console.log(data)

  //         this.restaurantAgentCode = data!.restaurantAgentCode

  //       }
  //     )
  //     .catch(error => {
  //       console.log(error)
  //     }
  //     )

  // }

  saveRestaurantAgent = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var agent = {
      id: this.restaurantAgentId,
      name: this.restaurantAgentName,
      phoneNo: this.restaurantAgentPhone,
      restaurantId: this.restaurantId
    }

    await this.http.post<IRestaurantAgent>(API_URL + '/restaurants/create_agent', agent, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllAgents()
          this.msg.showSuccessMessage("Successiful")

        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage3("Error creating agent")
      }
      )
  }

  async activate(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var agent = { id: id }
    await this.http.post<String>(API_URL + '/restaurant-agents/activate', agent, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllAgents()
          this.msg.showSuccessMessage('Agent activated successifully')
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
    var agent = { id: id }
    await this.http.post<String>(API_URL + '/restaurant-agents/deactivate', agent, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllAgents()
          this.msg.showSuccessMessage('Agent deactivated successifully')
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  async unassignRestaurantBadge(id: any) {

    if (await this.msg.showConfirmMessageDialog('Confirm', 'Are you sure you want to unassign?', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var agent = { id: id }
    await this.http.post<String>(API_URL + '/restaurant-agents/unassign-badge', agent, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.getAllAgents()
          this.msg.showSuccessMessage('badge unassigned')
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  clearAgent() {
    this.restaurantAgentId = null
    this.restaurantAgentName = ''
    this.restaurantAgentPhone = ''
  }

}

export interface IStringData {
  restaurantAgentCode: string
}
