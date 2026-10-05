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
import { IShop } from 'src/app/domain/shop';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';
import { NotificationComponent } from '../../misc/notification/notification.component';
import { trackById } from 'src/app/common/utils/track-by-id';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';



const API_URL = environment.apiUrl;
@Component({
  selector: 'az-select-shop',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-shop.component.html',
  styleUrl: './select-shop.component.scss'
})
export class SelectShopComponent {
  trackById = trackById

  underStock : number = 0
  outofStock : number = 0


  availableShops :IShop[] = []

  selectedShop :IShop | null = null
  selectedShopId :string | null = null

  branchId :string = ''

  shopLoaded :boolean = false 

  lpoPage: number = 1; // Initialize the current page to 1
  pageSizeLpos : number = 10
  listSearchTimerLpos : any = null
  grnPage: number = 1; // Initialize the current page to 1
  pageSizeGrns : number = 10
  listSearchTimerGrns : any = null
  filterLpoRecords : string = ''
  filterGrnRecords : string = ''
  selectedOption: string = '';
    
  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private route: ActivatedRoute,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    ){} //{(window as any).pdfMake.vfs = pdfFonts.pdfMake.vfs;}

    ngOnInit() {
      this.selectedShopId = localStorage.getItem('selected-shop-id')
      if(this.selectedShopId == '' || this.selectedShopId == null){
        this.shopLoaded = false
        this.loadAvailableShops()
      }else{      
        this.selectedShopId = localStorage.getItem('selected-shop-id')
        this.loadSelectedShop()
        
      }     
        this.getUnderstock()
        this.getOutofstock() 
    }

    loadAvailableShops = async () => {

      this.availableShops = []

      let options = {
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.get<IShop[]>(API_URL+'/shops/get_branch_available_shops_by_user' , options)
        .toPromise()
        .then(
          data => {
            this.availableShops = data!
            console.log(data)
          }
        )
    }

  loadSelectedShop = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IShop>(API_URL + '/shops/get_selected_shop?shop_id=' + this.selectedShopId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.selectedShop = data!
          localStorage.setItem('selected-shop-id', this.selectedShop.id.toString());
          this.shopLoaded = true

          this.getUnderstock()
          this.getOutofstock()
        }
      )
      .catch(error => {
        console.log(error)
        localStorage.setItem('selected-shop-id', '');
        this.shopLoaded = false
      }
      )
  }

    onShopChange(event: any): void {
      this.selectedShopId = event.target.value;
      console.log('Selected Shop ID:', this.selectedShopId);
      //alert('Selected Shop ID: ' + this.selectedShopId);
  }

  selectShop(){
    //localStorage.setItem('selected-shop-id', this.selectedShopId!);
    if(this.selectedShopId! === '' || this.selectedShopId === null){
      this.msg.showErrorMessage3('Please select a shop first')
      return
    }
    this.loadSelectedShop()
  }

  clearSelectedShop(){
    localStorage.setItem('selected-shop-id', '');
    this.selectedShopId = ''
    
  }

  lpos : ILpo[] = []
  totalLpos : number = 0
  lposRequest : number = 0 // number of the latest list request; answers to older ones are ignored
  async getAllPendingOrders(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, searched on the server against the shown columns
    var page = this.lpoPage
    var request = ++this.lposRequest

    await this.http.get<IPage<ILpo>>(API_URL+'/lpos/get_all_visible_by_shop_page?shop_id=' + this.selectedShopId + '&' + pageParams(page, this.pageSizeLpos, this.filterLpoRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.lposRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSizeLpos))
        if(page > lastPage){
          this.lpoPage = lastPage
          this.getAllPendingOrders()
          return
        }
        this.lpos = data!.content
        this.totalLpos = data!.totalElements
      }
    )
  }

  pageChangedLpos(page : number){
    this.lpoPage = page
    this.getAllPendingOrders()
  }

  searchListLpos(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimerLpos)
    this.listSearchTimerLpos = setTimeout(() => {
      this.lpoPage = 1
      this.getAllPendingOrders()
    }, 300)
  }

      grns : IGrn[] = []
  totalGrns : number = 0
  grnsRequest : number = 0 // number of the latest list request; answers to older ones are ignored
  async getAllPendingGrns(){
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    // One page at a time, searched on the server against the shown columns
    var page = this.grnPage
    var request = ++this.grnsRequest

    await this.http.get<IPage<IGrn>>(API_URL+'/grns/get_all_visible_by_shop_page?shop_id=' + this.selectedShopId + '&' + pageParams(page, this.pageSizeGrns, this.filterGrnRecords), options)
    .toPromise()
    .then(
      data => {
        // An answer to an older request (another page or search) is ignored
        if(request != this.grnsRequest){
          return
        }
        // Past the last page (rows were removed meanwhile): show the last page instead
        var lastPage = Math.max(1, Math.ceil(data!.totalElements / this.pageSizeGrns))
        if(page > lastPage){
          this.grnPage = lastPage
          this.getAllPendingGrns()
          return
        }
        var sn = (page - 1) * this.pageSizeGrns + 1
        data!.content.forEach(element => {
          element.sn = sn
          sn = sn + 1
        })
        this.grns = data!.content
        this.totalGrns = data!.totalElements
      }
    )
    .catch(error => {
          console.log(error)
        })
  }

  pageChangedGrns(page : number){
    this.grnPage = page
    this.getAllPendingGrns()
  }

  searchListGrns(){
    // Search on the server once the user pauses typing, from the first page
    clearTimeout(this.listSearchTimerGrns)
    this.listSearchTimerGrns = setTimeout(() => {
      this.grnPage = 1
      this.getAllPendingGrns()
    }, 300)
  }


      lpoId : any = null

      async get(id : any){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        await this.http.get<ILpo>(API_URL+'/lpos/get?id=' + id, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
            console.log(data)
            this.lpoId = data!.id

            this.router.navigate(['/app/mechandizing/shop-lpo'], {
              queryParams: {
                shop_id: this.selectedShopId,
                lpo_id: id
              }
            });
          }
        )
      }

      async getGrn(id : any){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        await this.http.get<ILpo>(API_URL+'/grns/get?id=' + id, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)
            console.log(data)
            this.lpoId = data!.id

            this.router.navigate(['/app/mechandizing/shop-grn'], {
              queryParams: {
                shop_id: this.selectedShopId,
                grn_id: id
              }
            });
          }
        )
      }

      getUnderstock = async () => {

        this.underStock = 0
  
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
  
        await this.http.get<number>(API_URL+'/shop_products/get_check_under_stock_by_shop?shop_id=' + this.selectedShopId, options)
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
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
  
        await this.http.get<number>(API_URL+'/shop_products/get_check_out_of_stock_by_shop?shop_id=' + this.selectedShopId, options)
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
    { type: 'success', message: 'Shop selected successfully!', link: 'shop-sales-order' },
    
  ];

  // Close alert method
  closeAlert(index: number) {
    this.alerts.splice(index, 1); // Remove the alert at the given index
  }

  // Add an alert for testing purposes
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }

}
