import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IProduct } from 'src/app/domain/product';
import { IPage } from 'src/app/domain/page';
import { pageParams } from 'src/app/common/utils/page-params';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';
import { trackById } from 'src/app/common/utils/track-by-id';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-product',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    NgxPaginationModule
  ],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent {
  trackById = trackById
  /**Data */
  id: any = null
  code: string = ''
  name: string = ''
  description: string = ''
  baseUom: string = ''
  active: string = 'Inactive'

  product: IProduct

  /**Collections */

  products: IProduct[] = []

  /**Identifiers */
  productId: string = ''


  page: number = 1; // Initialize the current page to 1
  pageSize: number = 15
  totalProducts: number = 0
  // The list is loaded a page at a time and searched on the server
  productsRequest: number = 0 // number of the latest list request; answers to older ones are ignored
  searchTimer: any = null
  filterRecords: string = ''
  selectedOption: string = '';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private msg: MsgBoxService
  ) { }

  ngOnInit() {
    this.getProductPage(1)
  }

  async getProductPage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    var request = ++this.productsRequest

    await this.http.get<IPage<IProduct>>(API_URL + '/products/get_page?' + pageParams(page, this.pageSize, this.filterRecords), options)
      .toPromise()
      .then(
        data => {
          if (request != this.productsRequest) {
            return
          }
          // The page no longer exists (rows were removed): show the one before it
          if (data!.content.length == 0 && page > 1) {
            this.getProductPage(page - 1)
            return
          }
          var sn = (page - 1) * this.pageSize + 1
          data!.content.forEach(element => {
            element.sn = sn
            sn = sn + 1
          })
          this.products = data!.content
          this.totalProducts = data!.totalElements
          this.page = page
        }
      )
  }

  pageChanged(page: number) {
    this.getProductPage(page)
  }

  searchProducts() {
    // Search on the server once the user pauses typing, starting again from the first page
    clearTimeout(this.searchTimer)
    this.searchTimer = setTimeout(() => {
      this.getProductPage(1)
    }, 300)
  }

  refreshProducts() {
    this.getProductPage(this.page)
  }



  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IProduct>(API_URL + '/products/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.showProductData(data!)
          console.log(data)
        }
      )
  }



  public async save() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var product = {
      id: this.id,
      code: this.code,
      name: this.name,
      description: this.description,
      baseUom: this.baseUom
    }

    if (product.id === null) {
      /**Create new product */
      await this.http.post<IProduct>(API_URL + '/products/create', product, options)
        .toPromise()
        .then(
          data => {
            this.showProductData(data!)

            console.log(data)

            this.refreshProducts()

            this.msg.showSuccessMessage('Product created successifully')

          }

        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an existing product */
      await this.http.post<IProduct>(API_URL + '/products/update', product, options)
        .toPromise()
        .then(
          data => {
            this.showProductData(data!)

            console.log(data)

            this.refreshProducts()

            this.msg.showSuccessMessage('Product updated successifully')
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

  async activate(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var company = {
      id: id
    }

    await this.http.post<String>(API_URL + '/products/activate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.refreshProducts()

          this.msg.showSuccessMessage('Product activated successifully')

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

    var company = {
      id: id
    }

    await this.http.post<String>(API_URL + '/products/deactivate', company, options)
      .toPromise()
      .then(
        data => {

          console.log(data)

          this.refreshProducts()
          this.msg.showSuccessMessage('Product deactivated successifully')


        }

      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  showProductData(data: IProduct) {
    this.id = data?.id
    this.code = data!.code
    this.name = data!.name
    this.description = data!.description
    this.baseUom = data!.baseUom

  }

  clearProductData() {
    this.id = null
    this.code = ''
    this.name = ''
    this.description = ''
    this.baseUom = ''

  }
}