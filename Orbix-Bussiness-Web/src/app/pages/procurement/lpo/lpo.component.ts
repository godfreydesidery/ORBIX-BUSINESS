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
import { ICustomer } from 'src/app/domain/customer';


var pdfFonts = require('pdfmake/build/vfs_fonts.js');

const API_URL = environment.apiUrl;
@Component({
  selector: 'az-lpo',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule,
    NgSelectModule
  ],
  templateUrl: './lpo.component.html',
  styleUrl: './lpo.component.scss'
})
export class LpoComponent {

  show : boolean = false

  documentHeader!: any

  id: any = null
  no: any = ''
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


  lpos: ILpo[] = []

  // lpoId : any = null


  page: number = 1; // Initialize the current page to 1
  filterRecords: string = ''
  selectedOption: string = '';

  payCode: string = 'CASH'
  payRefNo: string = ''

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
    await this.getBranchShops()
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



  // searchSuppliers(): void {
  //   const options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)

  //   }
  //   this.filteredSuppliers = [];
  //   if (this.searchTerm.trim().length >= 2) {
  //     this.http
  //     //await this.http.get<IProduct[]>(API_URL+'/shop_products/get_products_by_shop_containing?product_name_like=' + searchKey + '&shop_id=' + this.shopId , options)
  //       .get<ISupplier[]>(API_URL+'/suppliers/get_suppliers_by_company_containing?supplier_name_like=' + this.searchTerm, options)
  //       .subscribe(
  //         (data) => (this.filteredSuppliers = data),
  //         (error) => console.error('Error fetching products:', error)
  //       );
  //   } else {
  //     this.filteredSuppliers = [];
  //   }
  // }

  selectSupplier(supplier: any): void {
    this.selectedSupplier = supplier;
    this.searchTerm = supplier.name;
    this.isDropdownOpen = false;

    this.getSupplier(supplier.id)
    this.filteredProducts = [];
  }














  // loadSelectedShop = async () => {
  //     let options = {
  //       headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //     }

  //     await this.http.get<IShop>(API_URL+'/shops/get_selected_shop?shop_id=' + this.shopId , options)
  //       .toPromise()
  //       .then(
  //         data => {
  //           console.log(data)
  //           this.shopName = data!.name
  //           this.selectedShop = data!

  //         }
  //       )
  //       .catch(error => {
  //         console.log(error)

  //       }       
  //     ) 
  //   }



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

  lpo!: ILpo

  totalAmount: number = 0

  async get(id: any) {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    await this.http.get<ILpo>(API_URL + '/lpos/get?id=' + id, options)
      .toPromise()
      .then(
        data => {
          this.show = true
          //this.showUomData(data!)
          console.log(data)

          this.lpo = data!

          this.status = data!.status

          this.id = data!.id
          this.no = data!.no
          this.orderDate = data!.orderDate
          this.validUntilDate = data!.validUntilDate
          this.supplierName = data!.supplierName
          this.shopName = data!.shopName

          this.totalAmount = 0

          var sn = 1
          this.lpo.lpoDetails.forEach(element => {
            element.sn = sn
            this.totalAmount = this.totalAmount + ((+element.costPriceVatIncl) * element.qty)
            sn = sn + 1

          })
        }
      )
  }


  public async save() {
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

    if (lpo.id === null) {
      /**Create new lpo */
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
    } else {
      /**Update an existing uom */
      await this.http.post<ILpo>(API_URL + '/lpos/update', lpo, options)
        .toPromise()
        .then(
          data => {
            //this.showUomData(data!)

            console.log(data)

            this.get(data!.id)


            this.msg.showSuccessMessage('LPO updated successifully')
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

    if(this.validUntilDate === null){
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

  clearOrder() {
    this.lpo!
    this.id = null
    this.no = ''
    this.orderDate = ''
    this.validUntilDate = null
    this.supplierId = null
    this.shopId = null
    this.show = true
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


  // searchProductInShop = async (product_id : any) => {
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }
  //   this.clearShopProduct()

  //   await this.http.get<IShopProduct>(API_URL+'/shop_products/get_product_in_shop?product_id=' + product_id + '&shop_id=' + this.shopId , options)
  //     .toPromise()
  //     .then(
  //       data => {
  //         console.log(data)
  //         this.productId = data!.productId
  //         this.productCode = data!.productCode
  //         this.productName = data!.productName
  //         this.productDescription = data!.productDescription
  //         this.vat = data!.vatRate * 100
  //         this.costPriceVatIncl = data!.costPriceVatIncl
  //         this.baseUom = data!.baseUom

  //       }
  //     )
  //     .catch(error => {
  //       console.log(error)

  //     }       
  //   ) 

  // }

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

  shops: IShop[] = []
  async getBranchShops() {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }
    this.shops = []
    await this.http.get<IShop[]>(API_URL + '/shops/get_branch_shops', options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.shops = data!
        }
      )
      .catch(error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
      )
  }

  async onShopChange(event: any): Promise<void> {
    this.shopId = await event.target.value;
    await this.loadSelectedSupplier()
  }

  shop!: IShop
  loadSelectedShop = async () => {
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer ' + this.auth.user.access_token)
    }

    await this.http.get<IShop>(API_URL + '/shops/get?id=' + this.shopId, options)
      .toPromise()
      .then(
        data => {
          console.log(data)
          this.shop = data!

        }
      )
      .catch(error => {
        console.log(error)
        this.shopId = null
      }
      )
  }



  receiptData: IShopSalesOrderDetail[] = []

  printReceipt() {

    // if(this.toPrintReceipt == false){
    //   return
    // }

    if (this.receiptData.length == 0) {
      this.msg.showErrorMessage3('No data to print')
      return
    }

    var items: ReceiptItem[] = []
    var item: ReceiptItem

    this.receiptData.forEach(element => {
      item = new ReceiptItem()
      item.code = element.id
      item.name = element.productName
      item.amount = element.sellingPriceVatIncl * (+element.qty) - (+element.discount)
      item.qty = element.qty
      items.push(item)
    })

    var customer: ICustomer = {
          name: '',
          address: '',
          phone: ''
        }

    this.printer.print(items, 'NA', 0, customer)
    //this.toPrintReceipt = false
  }



  ///////////////

  dropdownOptions = [
    { id: 1, name: 'Option 1' },
    { id: 2, name: 'Option 2' },
    { id: 3, name: 'Option 3' }
  ];

  //selectedOption: any = null;

  onSelectionChange(event: any) {
    console.log('Selected option:', event);
  }



  print = async () => {
    this.documentHeader = await this.data.getDocumentHeaderLandScape();
    const title = 'Local Purchase Order(LPO)';

    let total: number = 0;
    let discount: number = 0;

    const list: any[] = [];

    // Add header row
    list.push([
      { text: 'SN', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Code', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Name', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'UOM', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Qty', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Price', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
      { text: 'Amount', fontSize: 8, alignment: 'left', fillColor: '#ffffff', bold: true },
    ]);

    // Add rows dynamically
    this.lpo.lpoDetails.forEach((element) => {
      total += (element.costPriceVatIncl * element.qty) || 0;
      // discount += parseFloat(element.discount) || 0;

      // if(Number(element.amount) > 0) total += Number(element.amount) || 0;

      // if(Number(element.discount) > 0) discount += Number(element.discount) || 0;

      list.push([
        { text: element.sn || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.productCode || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.productName || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.baseUom || '', fontSize: 9, alignment: 'left', fillColor: '#ffffff', bold: false },
        { text: element.qty || '', fontSize: 9, alignment: 'center', fillColor: '#ffffff', bold: false },
        { text: (Number(element.costPriceVatIncl) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
        { text: (Number(element.costPriceVatIncl * element.qty) || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', fillColor: '#ffffff', bold: false },
      ]);
    });

    // Add summary row
    list.push([
      {},
      {},
      {},
      {},
      {},
      { text: 'Total', fontSize: 9, alignment: 'right', bold: true },
      { text: total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize: 9, alignment: 'right', bold: true },
    ]);

    // Define document structure
    const docDefinition: any = {
      header: '',
      pageOrientation: 'potrait',
      footer: (currentPage: any, pageCount: any) => ({
        text: `${currentPage} of ${pageCount}`,
        alignment: 'center',
        fontSize: 8,
      }),
      content: [
        {
          columns: [
            this.documentHeader,
          ],
        },
        { text: ' ' },
        { text: title, fontSize: 10, bold: true, alignment: 'left', margin: [0, 10, 0, 10] },
        { text: 'LPO No : ' + this.no, fontSize: 10, bold: true, alignment: 'left' },
        { text: 'Order Date : ' + this.orderDate, fontSize: 10, bold: true, alignment: 'left' },
        { text: 'Valid Until Date : ' + this.validUntilDate, fontSize: 10, bold: true, alignment: 'left' },
        { text: 'Status : ' + this.status, fontSize: 10, bold: true, alignment: 'left' },
        { text: 'Supplier : ' + this.supplierName, fontSize: 10, bold: true, alignment: 'left' },
        { text: 'Shop : ' + this.shopName, fontSize: 10, bold: true, alignment: 'left' },
        { text: ' ', fontSize: 10, bold: true, alignment: 'left' },
        {
          table: {
            widths: [25, 50, 130, 40, 40, 70, 70],
            body: list,
          },
        },
      ],
    };

    pdfMake.createPdf(docDefinition).print();
  };
}
