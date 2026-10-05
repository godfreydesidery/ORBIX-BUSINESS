import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';

import { ICompany } from 'src/app/domain/company';
import { IProduct } from 'src/app/domain/product';
import { IPage } from 'src/app/domain/page';
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
    SearchFilterPipe,
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
  // The list is loaded a page at a time; the whole list is loaded only when searching, so the search still covers every product
  allProductsLoaded: boolean = false
  requestedPage: number = 1
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

  async getAllProducts() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.products = []

    await this.http.get<IProduct[]>(API_URL + '/products', options)
      .toPromise()
      .then(
        data => {
          // Built apart and assigned at the end, so two overlapping loads cannot mix their rows
          var products : IProduct[] = []
          var sn = 1
          data?.forEach(element => {
            element.sn = sn
            products.push(element)
            sn = sn + 1
          })
          this.products = products
          this.allProductsLoaded = true
          console.log(data)
        }
      )
  }

  async getProductPage(page: number) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.requestedPage = page

    await this.http.get<IPage<IProduct>>(API_URL + '/products/get_page?page=' + (page - 1) + '&size=' + this.pageSize, options)
      .toPromise()
      .then(
        data => {
          // Ignore a page that arrives after another page or the whole list was requested
          if (this.allProductsLoaded || page != this.requestedPage) {
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
    if (this.allProductsLoaded) {
      this.page = page
    } else {
      this.getProductPage(page)
    }
  }

  searchProducts(filter: string) {
    if (filter != '' && !this.allProductsLoaded) {
      // Set before loading, so a page that arrives meanwhile is ignored
      this.allProductsLoaded = true
      this.getAllProducts()
    }
  }

  refreshProducts() {
    if (this.allProductsLoaded) {
      this.getAllProducts()
    } else {
      this.getProductPage(this.page)
    }
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