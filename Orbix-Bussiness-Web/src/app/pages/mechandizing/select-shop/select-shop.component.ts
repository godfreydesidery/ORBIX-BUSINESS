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
import { IShop } from 'src/app/domain/shop';
import { environment } from 'src/environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { ILpo } from 'src/app/domain/lpo';
import { IGrn } from 'src/app/domain/grn';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-select-shop',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './select-shop.component.html',
  styleUrl: './select-shop.component.scss'
})
export class SelectShopComponent {

  availableShops :IShop[] = []

  selectedShop :IShop | null = null
  selectedShopId :string | null = null

  branchId :string = ''

  shopLoaded :boolean = false

  page: number = 1; // Initialize the current page to 1
  filterRecords : string = ''
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
        headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
      }

      await this.http.get<IShop>(API_URL+'/shops/get_selected_shop?shop_id=' + this.selectedShopId , options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.selectedShop = data!
            localStorage.setItem('selected-shop-id', this.selectedShop.id.toString());
            this.shopLoaded = true
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
  async getAllPendingOrders(){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        this.lpos = []
      
        await this.http.get<ILpo[]>(API_URL+'/lpos/get_all_visible_by_shop?shop_id=' + this.selectedShopId, options)
        .toPromise()
        .then(
          data => {
            var sn = 1
            data?.forEach(element => {
              element.sn = sn
              this.lpos.push(element)
              sn = sn + 1
            })
            console.log(data)
          }
        )
      }

      grns : IGrn[] = []
      async getAllPendingGrns(){
        let options = {
          headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
        }
        this.grns = []
      
        await this.http.get<IGrn[]>(API_URL+'/grns/get_all_visible_by_shop?shop_id=' + this.selectedShopId, options)
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
}
