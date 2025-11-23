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
import { IProduct } from 'src/app/domain/product';
import { IShopSalesOrder, IShopSalesOrderDetail } from 'src/app/domain/shop-sales-order';

import { ReceiptItem } from 'src/app/domain/receipt-item';
import { ILpo, ILpoDetail } from 'src/app/domain/lpo';
import { ISupplier } from 'src/app/domain/supplier';
import { ISupplierProduct } from 'src/app/domain/supplier-product';
import { NgSelectModule } from '@ng-select/ng-select';

import * as pdfMake from 'pdfmake/build/pdfmake';


var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-weighbridge',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule,
    NgSelectModule
  ],
  templateUrl: './weighbridge.component.html',
  styleUrl: './weighbridge.component.scss'
})
export class WeighbridgeComponent {

  pageStatus : string = ''

  changeStatus(status : string){
    if(status == 'single'){
      this.pageStatus = 'single'
      return;
    }
    if(status == 'list'){
      this.pageStatus = 'list'
      return;
    }
    this.pageStatus = ''
    return
  }

  id: any = null
  no: string = ''
  ownerName: string = ''
  ownerPhoneNo: string = ''
  regNo: string = ''
  weighStatus: string = 'NORMAL'
  //details : IWeighbridgeDetail[] = []

  show: boolean = false

  documentHeader!: any


  orderDate: string = ''
  validUntilDate: Date | null = null
  shopId: any = null

  status: string = ''

  products: IProduct[] = []

  searchKey: string = ''

  productId: any
  productName: string = ''
  productDescription: string = ''
  productCode: string = ''

  supplierId: any = null
  supplierName: string = ''
  supplierCode: string = ''

  shopName: string = ''

  searchedProducts: IProduct[] = []

  searchedSuppliers: ISupplier[] = []

  isUserTyping: boolean = true; // Flag to detect user typing


  weigh: IWeighbridge
  weighs: IWeighbridge[] = []
  lpos: ILpo[] = []

  weightOne : number | string = ''
  weightTwo : number | string = ''
  weightThree : number | string = ''
  weightFour : number | string = ''

  clearWeights(){
    this.weightOne = ''
    this.weightTwo = ''
    this.weightThree = ''
    this.weightFour = ''
    this.weighStatus = 'NORMAL'
  }

  // lpoId : any = null


  page: number = 1; // Initialize the current page to 1
  filterRecords: string = ''
  selectedOption: string = '';

  payCode: string = 'CASH'
  payRefNo: string = ''

  selectedTareValue: number | null = null;
  selectedTareLabel: string = '';

  tareOptions = [
    { label: 'Axle One-Two', value: 10000 },
    { label: 'Axle Three-Four', value: 20000 }
  ];

  onTareChange() {
    const selected = this.tareOptions.find(option => option.value === +this.selectedTareValue!);
    this.selectedTareLabel = selected?.label || '';
    this.clearWeights()
  }

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
    await this.getCompanySuppliers()
  }

  searchTerm: string = '';
  filteredProducts: any[] = [];
  selectedProduct: any | null = null;
  selectedSupplier: any | null = null;
  isDropdownOpen: boolean = false;
  searchProducts(): void {
    const options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)

    }
    this.filteredProducts = [];
    if (this.searchTerm.trim().length >= 2) {
      this.http
        //await this.http.get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId , options)
        .get<IProduct[]>(API_URL + '/products/get_products_by_company_containing?product_name_like=' + this.searchTerm, options)
        .subscribe(
          (data) => (this.filteredProducts = data),
          (error) => console.error('Error fetching products:', error)
        );
    } else {
      this.filteredProducts = [];
    }
  }

  selectProduct(product: any): void {
    this.selectedProduct = product;
    this.searchTerm = product.name;
    this.isDropdownOpen = false;

    this.searchSupplierProduct(product.id)
    this.filteredProducts = [];
  }





  selectSupplier(supplier: any): void {
    this.selectedSupplier = supplier;
    this.searchTerm = supplier.name;
    this.isDropdownOpen = false;

    this.getSupplier(supplier.id)
    this.filteredProducts = [];
  }



  async getAllPendingOrders() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.lpos = []

    await this.http.get<ILpo[]>(API_URL + '/lpos/get_all_visible_by_branch', options)
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

  async getAllRecent() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.weighs = []

    await this.http.get<IWeighbridge[]>(API_URL + '/weighs/recent', options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          data?.forEach(element => {
            this.weighs.push(element)
          })
          this.weighs.reverse()
          this.weighs.forEach(ele => {
            ele.sn = sn
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }

  lpo!: ILpo

  totalAmount: number = 0

  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<IWeighbridge>(API_URL + '/weighs/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.weigh = data!
          this.id = data!.id
          this.no = data!.no
          this.ownerName = data!.ownerName
          this.ownerPhoneNo = data!.ownerPhoneNo
          this.regNo = data!.regNo

          this.getBills(data!.id)

          this.changeStatus('single')



          // var sn = 1
          // this.weigh.details.forEach(element => {
          //   // this.totalAmount = this.totalAmount + ((+element.costPriceVatIncl) * element.qty)
          //   sn = sn + 1

          // })
        }
      )
  }

  async recheck(id: any) {
    if (await this.msg.showConfirmMessageDialog('Approve', 'Are you sure you want to recheck?', 'question', 'Yes', 'No') == false) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.post<null>(API_URL + '/weighs/recheck?id=' + id, null, options)
      .toPromise()
      .then(
        data => {
          this.getAllRecent()
          this.msg.showSuccessMessage('Rechecked')
        }
      )
  }

  async addBill() {

    if (this.selectedTareLabel == '') {
      this.msg.showErrorMessage3('Please select tare type')
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var bill = {
      weighId: this.id,
      amount: this.selectedTareValue,
      description: this.selectedTareLabel,
      weightOne : this.weightOne,
      weightTwo : this.weightTwo,
      weightThree: this.weightThree,
      weightFour : this.weightFour,
      weighStatus : this.weighStatus
    }
    await this.http.post<IWeighbridge>(API_URL + '/weigh_bills/add_bill', bill, options)
      .toPromise()
      .then(
        () => {
          this.msg.showSuccessMessage('Bill added successfully')
          this.getBills(this.id)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
    this.selectedTareLabel = ''
    this.selectedTareValue = 0
  }


  bills: IWeighbridgeDetail[] = []
  async getBills(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    this.bills = []

    await this.http.get<IWeighbridgeDetail[]>(API_URL + '/weigh_bills/get_by_weigh_id?weigh_id=' + id, options)
      .toPromise()
      .then(
        data => {
          var sn = 1
          this.totalAmount = 0
          data?.forEach(element => {
            element.sn = sn
            this.bills.push(element)
            this.totalAmount = this.totalAmount + (+element.amount)
            sn = sn + 1
          })
          console.log(data)
        }
      )
  }


  public async save() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var weigh = {
      id: this.id,
      no: this.no,
      ownerName: this.ownerName,
      ownerPhoneNo: this.ownerPhoneNo,
      regNo: this.regNo
    }

    if(weigh.regNo == '') {
      alert('Please enter reg no')
      return
    }

    if (weigh.id === null) {
      /**Create new lpo */
      await this.http.post<IWeighbridge>(API_URL + '/weighs/create', weigh, options)
        .toPromise()
        .then(
          data => {

            this.id = data!.id
            this.no = data!.no
            this.ownerName = data!.ownerName
            this.ownerPhoneNo = data!.ownerPhoneNo
            this.regNo = data!.regNo

            //this.get(data!.id)

            this.msg.showSuccessMessage('Weigh created successifully')

          }

        )
        .catch(
          error => {
            console.log(error)
            this.msg.showErrorMessage(error, 'Error')
          }
        )
    } else {
      /**Update an existing uom */
      await this.http.post<IWeighbridge>(API_URL + '/weighs/update', weigh, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)

            this.id = data!.id
            this.no = data!.no
            this.ownerName = data!.ownerName
            this.ownerPhoneNo = data!.ownerPhoneNo
            this.regNo = data!.regNo

            this.get(data!.id)


            this.msg.showSuccessMessage('Weigh updated successifully')
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

  addTare() {
    var tare = {
      weighId: this.id,
      tareType: this.selectedTareLabel,
      tareValue: this.selectedTareValue
    }
  }

  public async create() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var lpo = {
      id: null,
      no: null,
      validUntilDate: this.validUntilDate,
      summary: null,
      shopId: this.shopId,
      supplierId: this.supplierId
    }

    if (this.validUntilDate === null) {
      this.msg.showErrorMessage3('Please select a valid until date')
      return
    }

    await this.http.post<ILpo>(API_URL + '/lpos/create', lpo, options)
      .toPromise()
      .then(
        data => {
          //this.showUomData(data!)

          console.log(data)

          this.get(data!.id)

          this.msg.showSuccessMessage('LPO created successifully')
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  public async removeBill(id: any) {
    if (await this.msg.showConfirmMessageDialog('Approve', 'Are you sure you want to remove this bill? Click Yes to remove, No to keep', 'question', 'Yes', 'No') == false) {
      return
    }

    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var removeBill = {
      weighId: this.id,
      weighNo: this.no,
      billId: id
    }

    await this.http.post<ILpo>(API_URL + '/weigh_bills/remove', removeBill, options)
      .toPromise()
      .then(
        data => {
          this.msg.showSuccessMessage('Removed successifully')
          this.get(this.id)
        }
      )
      .catch(
        error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
      )
  }

  clearWeigh() {
    this.weigh!
    this.id = null
    this.no = ''
    this.regNo = ''
    this.ownerName = ''
    this.ownerPhoneNo = ''
    this.bills = []
  }

  // Triggered on every keystroke
  onInputChange(searchText: string): void {
    if (this.isUserTyping) {
      this.getShopProductLike(searchText);
    }
  }

  onProductSelected(): void {
    const selectedProduct = this.searchedProducts.find(
      (product) => product.name === this.searchKey
    );

    if (selectedProduct) {
      this.searchSupplierProduct(selectedProduct.id)
      this.searchedProducts = []; // Clear suggestions after selection
    } else {
      console.warn('Selected product is not in the suggestions list');
    }

    // Allow typing detection after a short delay
    setTimeout(() => (this.isUserTyping = true), 0);

  }

  searchSupplierProduct = async (productId: any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.clearLpoDetail()

    await this.http.get<ISupplierProduct>(API_URL + '/supplier_products/get_product?product_id=' + productId + '&supplier_id=' + this.lpo.supplierId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.productId = data!.productId
          this.productCode = data!.productCode
          this.productName = data!.productName
          this.productDescription = data!.productDescription
          this.costPriceVatIncl = data!.costPriceVatIncl

        }
      )
      .catch(error => {
        console.log(error)
      }
      )

  }

  lpoDetailId: any = null
  vat: number = 0
  costPriceVatIncl: number = 0

  baseUom: string = ''

  qty: number = 0

  clearLpoDetail() {
    this.productId = null

    this.searchTerm = ''

    this.lpoDetailId = null
    this.searchKey = ''
    this.productName = ''
    this.productCode = ''
    this.productDescription = ''
    this.vat = 0
    this.costPriceVatIncl = 0

    this.baseUom = ''
    this.qty = 0
  }




  getShopProductLike = async (searchKey: string) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IProduct[]>(API_URL + '/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.searchedProducts = data!

        }
      )
      .catch(error => {
        console.log(error)

      }
      )
  }

  saveDetail() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    var detail = {
      id: this.lpoDetailId,
      lpoId: this.id,
      productId: this.productId,
      qty: this.qty
    }

    if (detail.id === null) {
      /**Create new detail */
      this.http.post<ILpoDetail>(API_URL + '/lpos/create_detail', detail, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.id)
          }
        )
        .catch(error => {
          console.log(error)
          this.msg.showErrorMessage(error, 'Error')
        }
        )
    } else if (detail.id !== null) {
      /**Update detail */
      this.http.post<ILpoDetail>(API_URL + '/lpos/update_detail', detail, options)
        .toPromise()
        .then(
          data => {
            console.log(data)
            this.get(this.id)
          }
        )
        .catch(error => {
          console.log(error)
        }
        )
    }


  }

  removeDetail(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.http.get<ILpoDetail>(API_URL + '/lpos/remove_detail?lpo_detail_id=' + id + '&lpo_id=' + this.id, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.get(this.id)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }


  async approveOrder() {
    if (await this.msg.showConfirmMessageDialog('Approve', 'Are you sure you want to approve this order?', 'question', 'Yes', 'No') == false) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.http.post<ILpo>(API_URL + '/lpos/approve?lpo_id=' + this.id, null, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.get(this.id)
          this.msg.showSuccessMessage('LPO approved successifully')
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }


  async cancelOrder() {
    if (await this.msg.showConfirmMessageDialog('Cancel', 'Are you sure you want to cancel this order?', 'question', 'Yes', 'No') == false) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.http.post<ILpo>(API_URL + '/lpos/cancel?lpo_id=' + this.id, null, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.get(this.id)
          this.msg.showSuccessMessage('LPO canceled successifully')
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  async archiveOrder() {
    if (await this.msg.showConfirmMessageDialog('Cancel', 'Are you sure you want to archive this order?', 'question', 'Yes', 'No') == false) {
      return
    }
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.http.post<ILpo>(API_URL + '/lpos/archive?lpo_id=' + this.id, null, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.get(this.id)
          this.msg.showSuccessMessage('LPO archived successifully')
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  suppliers: ISupplier[] = []
  async getCompanySuppliers() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.suppliers = []
    await this.http.get<ISupplier[]>(API_URL + '/suppliers/get_all_by_company', options)
      .toPromise()
      .then(
        data => {
          //console.log(data)
          //this.suppliers = data!
          data?.forEach(element => {
            this.suppliers.push(element)
          })
          console.log(this.suppliers)
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  async onSupplierChange(event: any): Promise<void> {
    this.supplierId = await event.target.value;
    await this.loadSelectedSupplier()
  }

  supplier!: ISupplier
  loadSelectedSupplier = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<ISupplier>(API_URL + '/suppliers/get?id=' + this.supplierId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.supplier = data!

        }
      )
      .catch(error => {
        console.log(error)
        this.supplierId = null
      }
      )
  }

  getSupplier = async (supplierId: any) => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<ISupplier>(API_URL + '/suppliers/get?id=' + supplierId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.supplier = data!

        }
      )
      .catch(error => {
        console.log(error)
        this.supplierId = null
      }
      )
  }








}

export interface IWeighbridge {
  id: any
  sn: number
  no: string
  ownerName: string
  ownerPhoneNo: string
  regNo: string
  recheck : number;
  details: IWeighbridgeDetail[]
}

export interface IWeighbridgeDetail {
  sn: number
  id: any
  weighId: any
  tareType: string
  amount: number
  price: number
  description: string
  createdBy: string
  payStatus: string

  weightOne : number | string
  weightTwo : number | string
  weightThree : number | string
  weightFour : number | string

  weighStatus : string

  

}


