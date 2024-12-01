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
import { IShopProduct } from 'src/app/domain/shop-product';


var pdfFonts = require('pdfmake/build/vfs_fonts.js'); 

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-shop-product-stock-status',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './shop-product-stock-status.component.html',
  styleUrl: './shop-product-stock-status.component.scss'
})
export class ShopProductStockStatusComponent {

  shopId: number;

  shopProducts : IShopProduct[] = []

  productId : any
  productName : string = ''
  productCode : string = ''

  selectedShop : IShop

  shopName : string = ''

  constructor(
    private http :HttpClient,
    private auth : AuthService,
    private router : Router,
    private printer : PosReceiptPrinterService,
    private msg : MsgBoxService,
    private data : DataService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Retrieve the shop_id query parameter from the URL
    this.route.queryParams.subscribe(params => {
      this.shopId = params['shop_id'];
      console.log('Shop ID:', this.shopId);
    });
    this.loadShopProductStockStatus()
    this.loadSelectedShop()
  }

  loadShopProductStockStatus = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    this.shopProducts = []

    await this.http.get<IShopProduct[]>(API_URL+'/shop_products/get_stock_by_shop?shop_id=' + this.shopId, options)
    .toPromise()
    .then(
      data => {
        this.shopProducts = data!
        console.log(data)
      }
    )
    .catch(
      error => {
        console.log(error)
      }
    )
  }

  saveProduct(){

  }

  loadSelectedShop = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }

    await this.http.get<IShop>(API_URL+'/shops/get_selected_shop?shop_id=' + this.shopId , options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.shopName = data!.name
          this.selectedShop = data!
          
        }
      )
      .catch(error => {
        console.log(error)
        
      }       
    ) 
  }

}
