import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MsgBoxService } from '@services/custom/msg-box.service';
import { AuthService } from 'src/app/auth.service';

import { ICompany } from 'src/app/domain/company';
import { IProduct } from 'src/app/domain/product';
import { Byte } from 'src/custom-packages/util';
import { environment } from 'src/environments/environment';

const API_URL = environment.apiUrl;

@Component({
  selector: 'az-product',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent {
/**Data */
id : any = null
code : string = ''
name : string = ''
description : string = ''
baseUom : string = ''
active : string = 'Inactive'

product : IProduct

/**Collections */

products : IProduct[] = []

/**Identifiers */
productId : string = ''

constructor(
  private http :HttpClient,
  private auth : AuthService,
  private msg : MsgBoxService
) {}

ngOnInit(){
  this.getAllProducts()
}

async getAllProducts(){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }
  this.products = []

  await this.http.get<IProduct[]>(API_URL+'/products', options)
  .toPromise()
  .then(
    data => {
      var sn = 1
      data?.forEach(element => {
        element.sn = sn
        this.products.push(element)
        sn = sn + 1
      })
      console.log(data)
    }
  )
}

async get(id : any){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }
  await this.http.get<IProduct>(API_URL+'/products/get?id=' + id, options)
  .toPromise()
  .then(
    data => {
      this.showProductData(data!)
      console.log(data)
    }
  )
}



public async save(){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }

  var product = {
    id : this.id,
    code : this.code,
    name : this.name,
    description : this.description,
    baseUom : this.baseUom
  }

  if(product.id === null){
    /**Create new product */
    await this.http.post<IProduct>(API_URL+'/products/create', product, options)
    .toPromise()
    .then(
      data => {
        this.showProductData(data!)

        console.log(data)

        this.getAllProducts()

        this.msg.showSuccessMessage('Product created successifully')

      }

    )
    .catch(
      error => {
        console.log(error)
        this.msg.showErrorMessage(error, 'Error')
      }
    )
  }else{
    /**Update an existing product */
    await this.http.post<IProduct>(API_URL+'/products/update', product, options)
    .toPromise()
    .then(
      data => {
        this.showProductData(data!)

        console.log(data)

        this.getAllProducts()

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

async activate(id : any){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }

  var company = {
    id : id
  }

  await this.http.post<String>(API_URL+'/products/activate', company, options)
    .toPromise()
    .then(
      data => {

        console.log(data)

        this.getAllProducts()

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

async deactivate(id : any){
  let options = {
    headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  }

  var company = {
    id : id
  }

  await this.http.post<String>(API_URL+'/products/deactivate', company, options)
    .toPromise()
    .then(
      data => {

        console.log(data)

        this.getAllProducts()
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

showProductData(data : IProduct){
  this.id = data?.id
  this.code = data!.code
  this.name = data!.name
  this.description = data!.description
  this.baseUom = data!.baseUom
  
}

clearProductData(){
  this.id = null
  this.code = ''
  this.name = ''
  this.description = ''
  this.baseUom = ''

}
}