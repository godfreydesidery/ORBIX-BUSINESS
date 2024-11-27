import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Byte } from 'src/custom-packages/util';
import { Injectable } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
//import { NgxSpinnerService } from 'ngx-spinner';
import { finalize } from 'rxjs';
import { environment } from 'src/environments/environment';
//import { ICompanyProfile } from '../domain/company';
import { AuthService } from 'src/app/auth.service';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class DataService {

  companyId       : any;
  companyName     : string;
  contactName     : string;
  tin             : string;
  vrn             : string;
  physicalAddress : string;
  postCode        : string;
  postAddress     : string;
  telephone       : string;
  mobile          : string;
  email           : string;
  website         : string
  fax             : string;
  bankAccountName : string;
  bankPhysicalAddress: string;
  bankPostCode    : string;
  bankPostAddress : string;
  bankName        : string;
  bankAccountNo   : string;
  bankAccountName2 : string;
  bankPhysicalAddress2: string;
  bankPostCode2    : string;
  bankPostAddress2 : string;
  bankName2        : string;
  bankAccountNo2   : string;
  bankAccountName3 : string;
  bankPhysicalAddress3: string;
  bankPostCode3   : string;
  bankPostAddress3 : string;
  bankName3        : string;
  bankAccountNo3   : string;

  quotationNotes   : string
  salesInvoiceNotes : string
  

  constructor(private http : HttpClient, 
              private auth : AuthService, 
              private sanitizer: DomSanitizer
              ) {

    this.companyId        = ''
    this.companyName      = ''
    this.contactName      = ''
    this.tin              = ''
    this.vrn              = ''
    this.physicalAddress  = ''
    this.postCode         = ''
    this.postAddress      = ''
    this.telephone        = ''
    this.mobile           = ''
    this.email            = ''
    this.website          = ''
    this.fax              = ''
    this.bankAccountName  = ''
    this.bankPhysicalAddress = ''
    this.bankPostCode     = ''
    this.bankPostAddress  = ''
    this.bankName         = ''
    this.bankAccountNo    = ''
    this.bankAccountName2  = ''
    this.bankPhysicalAddress2 = ''
    this.bankPostCode2     = ''
    this.bankPostAddress2  = ''
    this.bankName2         = ''
    this.bankAccountNo2    = ''
    this.bankAccountName3  = ''
    this.bankPhysicalAddress3 = ''
    this.bankPostCode3     = ''
    this.bankPostAddress3  = ''
    this.bankName3         = ''
    this.bankAccountNo3    = ''

    this.quotationNotes    = ''
    this.salesInvoiceNotes = ''
    
  }

  async getLogo() : Promise<string> {
    var logo : any = ''
    await this.http.get<ICompany>(API_URL+'/company_profile/get_logo')
    .toPromise()
    .then(
      res => {
        var retrieveResponse : any = res
        var base64Data = retrieveResponse.logo
        logo = 'data:image/png;base64,'+base64Data
      }
    )
    .catch(error => {
      console.log(error)
    }) 
    return logo
  }

  async getAddress(){
    await this.getCompanyProfile()
    var cName = this.companyName
    var cPostalAddress = 'P.O. Box '+this.postCode
    var cPhysicalAddress = this.physicalAddress
    var cTelephone = 'Tel: '+this.telephone
    var cMobile = 'Mob: '+this.mobile
    var cFax = 'Fax: '+this.fax
    var cEmail = 'Email: '+this.email
    var cWebsite = this.website
    var tin = 'TIN: '+this.tin
    var vrn = 'VRN: '+this.vrn
    
    var address = [
      {text : cName, fontSize : 16, bold : true, alignment : 'center'},
      {table : {
        headerRows : 0,
        widths: ['100%'],
        body : [
                [{text : '.', fontSize : 9, fillColor : '#546f9c', height : 30}],
                ['']
                ]
    },
    layout : 'headerLineOnly'},
      {text : cPhysicalAddress + ' ' + cPostalAddress + ' ' + cTelephone + ' ' + cEmail + ' ' + cWebsite, fontSize : 9,  alignment : 'center'},
    ]
    return address
  }

  getHorizontalLine() : any{
    try{
      return {table : {headerRows : 1,widths: ['100%'], body : [[''],['']]},layout : 'headerLineOnly'}
    }catch(error){
      return ''
    }    
  }


  async getCompanyProfile() {
    var company! : ICompany
    let options = {
      headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
    }
    //this.spinner.show()
    await this.http.get<ICompany>(API_URL+'/company/get', options)
    //.pipe(finalize(() => this.spinner.hide()))
    .toPromise()
    .then(
      data => {

        this.companyId        = ''
        this.companyName      = 'Davagan'
        this.contactName      = ''
        this.tin              = 'NA'
        this.vrn              = 'NA'
        this.physicalAddress  = 'Dar es Salaam, TZ'
        this.postCode         = ''
        this.postAddress      = 'Dar es Salaam, TZ'
        this.telephone        = ''
        this.mobile           = ''
        this.email            = ''
        this.website          = 'www.davagan.net'
        this.fax              = ''



        // this.companyId        = data?.id
        // this.companyName      = data!.companyName
        // this.contactName      = data!.contactName
        // this.tin              = data!.tin
        // this.vrn              = data!.vrn
        // this.physicalAddress  = data!.physicalAddress
        // this.postCode         = data!.postCode
        // this.postAddress      = data!.postAddress
        // this.telephone        = data!.telephone
        // this.mobile           = data!.mobile
        // this.email            = data!.email
        // this.website          = data!.website
        // this.fax              = data!.fax

        /**
         * Document notes
         */
        // this.quotationNotes = data!.quotationNotes
        // this.salesInvoiceNotes = data!.salesInvoiceNotes
        


        // this.bankAccountName  = data!.bankAccountName
        // this.bankPhysicalAddress = data!.bankPhysicalAddress
        // this.bankPostCode     = data!.bankPostCode
        // this.bankPostAddress  = data!.bankPostAddress
        // this.bankName         = data!.bankName
        // this.bankAccountNo    = data!.bankAccountNo 
        // this.bankAccountName2  = data!.bankAccountName2
        // this.bankPhysicalAddress2 = data!.bankPhysicalAddress2
        // this.bankPostCode2     = data!.bankPostCode2
        // this.bankPostAddress2  = data!.bankPostAddress2
        // this.bankName2         = data!.bankName2
        // this.bankAccountNo2    = data!.bankAccountNo2  
        // this.bankAccountName3  = data!.bankAccountName3
        // this.bankPhysicalAddress3 = data!.bankPhysicalAddress3
        // this.bankPostCode3     = data!.bankPostCode3
        // this.bankPostAddress3  = data!.bankPostAddress3
        // this.bankName3         = data!.bankName3
        // this.bankAccountNo3    = data!.bankAccountNo3  
        
        // company.bankAccountName = this.bankAccountName
        // company.bankPhysicalAddress = this.bankPhysicalAddress
        // company.bankPostCode = this.bankPostCode
        // company.bankPostAddress = this.bankPostAddress
        // company.bankName = this.bankName
        // company.bankAccountNo = this.bankAccountNo

        // company.bankAccountName2 = this.bankAccountName2
        // company.bankPhysicalAddress2 = this.bankPhysicalAddress2
        // company.bankPostCode2 = this.bankPostCode2
        // company.bankPostAddress2 = this.bankPostAddress2
        // company.bankName2 = this.bankName2
        // company.bankAccountNo2 = this.bankAccountNo2

        // company.bankAccountName3 = this.bankAccountName3
        // company.bankPhysicalAddress3 = this.bankPhysicalAddress3
        // company.bankPostCode3 = this.bankPostCode3
        // company.bankPostAddress3 = this.bankPostAddress3
        // company.bankName3 = this.bankName3
        // company.bankAccountNo3 = this.bankAccountNo3
      }
    )
    .catch(
      (error) => {
        console.log(error)
      }
    )

    this.companyId        = ''
    this.companyName      = 'Davagan'
    this.contactName      = ''
    this.tin              = 'NA'
    this.vrn              = 'NA'
    this.physicalAddress  = 'Dar es Salaam, TZ'
    this.postCode         = 'DSM'
    this.postAddress      = 'Dar es Salaam, TZ'
    this.telephone        = ''
    this.mobile           = ''
    this.email            = ''
    this.website          = 'www.davagan.net'
    this.fax              = ''


    return company
  }

  // async getBranchReceiptHeader(receiptNo : string){
  //   let options = {
  //     headers: new HttpHeaders().set('Authorization', 'Bearer '+this.auth.user.access_token)
  //   }
  //   await this.http.get<IBranchReceiptHeader>(API_URL+'/companies/get_branch_receipt_header_by_user', options)
  //   .toPromise()
  //   .then(
  //     data => {
  //       console.log(data)

  //       var companyName = data!.companyName
  //       var branchName = data!.branchName
  //       var address = data!.address
  //       var location = data!.location
  //       var email = data!.email
  //       var website = data!.website
  //       var tin = data!.tin
  //       var vrn = data!.vrn

  //       var address = {
  //         headerRows : 0,
  //         widths : [200],
  //         body : [
  //           [{text : 'Document #: '+receiptNo, fontSize : 12, bold : true, alignment : 'right'}],            
  //           [{text : ' ', fontSize : 7, bold : true, alignment : 'center'}],
  //           [{text : companyName, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : branchName, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : address, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : location, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : email, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : website, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : tin, fontSize : 12, bold : true, alignment : 'center'}],
  //           [{text : vrn, fontSize : 12, bold : true, alignment : 'center'}]
  //         ]
  //       }
  //       return address
  //     }
  //   )
  //   .catch(error => {
  //     console.log(error)
  //   })


  // }

  async getBranchReceiptHeader(receiptNo: string) {
    try {
      const options = {
        headers: new HttpHeaders().set('Authorization','Bearer ' + this.auth.user.access_token),
      }
  
      // Fetch data from the API
      const data = await this.http
        .get<IBranchReceiptHeader>(
          API_URL + '/companies/get_branch_receipt_header_by_user',
          options
        )
        .toPromise();
  
      if (data) {
        const {
          companyName,
          branchName,
          address,
          location,
          email,
          website,
          tin,
          vrn,
        } = data;
  
        // Create the address object
        const receiptHeader = {
          headerRows: 0,
          widths: [200],
          body: [
            [
              {
                text: 'Document #: ' + receiptNo,
                fontSize: 12,
                bold: true,
                alignment: 'right',
              },
            ],
            [{ text: ' ', fontSize: 7, bold: true, alignment: 'center' }],
            [{ text: companyName, fontSize: 12, bold: true, alignment: 'center' }],
            [{ text: branchName, fontSize: 10, bold: true, alignment: 'center' }],
            [{ text: address, fontSize: 8, bold: true, alignment: 'center' }],
            [{ text: location, fontSize: 8, bold: true, alignment: 'center' }],
            [{ text: email, fontSize: 8, bold: true, alignment: 'center' }],
            [{ text: website, fontSize: 8, bold: true, alignment: 'center' }],
            [{ text: tin, fontSize: 8, bold: true, alignment: 'center' }],
            [{ text: vrn, fontSize: 8, bold: true, alignment: 'center' }],
          ],
        };
  
        return receiptHeader;
      } else {
        console.log('No data received from API.');
        return null;
      }
    } catch (error) {
      console.error('Error fetching receipt header:', error);
      return null;
    }
  }
  

  

  

  async getReceiptHeader(receiptNo : string){
    await this.getCompanyProfile()
    var cName = this.companyName
    var cPostalAddress = 'P.O. Box '+this.postCode
    var cPhysicalAddress = this.physicalAddress
    var cTelephone = this.telephone
    var cMobile = this.mobile
    var cFax = 'Fax: '+this.fax
    var cEmail = this.email
    var cWebsite = this.website
    var tin = 'TIN: '+this.tin
    var vrn = 'VRN: '+this.vrn
    
    // var address = [
    //   {text : cName, fontSize : 12, bold : true, alignment : 'center', width : 200},
    //   {text : cPostalAddress, fontSize : 9, alignment : 'center', width : 200},
    //   {text : cPhysicalAddress, fontSize : 9, alignment : 'center', width : 200},
    //   {text : cTelephone, fontSize : 9, alignment : 'center', width : 200},
    //   {text : cEmail, fontSize : 9, italic : true, alignment : 'center', width : 200},
    //   {text : cWebsite, fontSize : 9, italic : true, alignment : 'center', width : 200},
    //   {text : tin, fontSize : 9, italic : true, alignment : 'center', width : 200},
    //   {text : vrn, fontSize : 9, italic : true, alignment : 'center', width : 200},
    // ]

    var address = {
      headerRows : 0,
      widths : [200],
      body : [
        [{text : 'Document #: '+receiptNo, fontSize : 12, bold : true, alignment : 'right'}],
        [{text : ' ', fontSize : 7, bold : true, alignment : 'center'}],
        [{text : cName, fontSize : 12, bold : true, alignment : 'center'}],
        [{text : cPostalAddress, fontSize : 9, alignment : 'center'}],
        [{text : cPhysicalAddress, fontSize : 9, alignment : 'center'}],
        [{text : cTelephone, fontSize : 9, alignment : 'center'}],
        [{text : cEmail, fontSize : 9, italic : true, alignment : 'center'}],
        [{text : cWebsite, fontSize : 9, italic : true, alignment : 'center'}],
        // [{text : tin, fontSize : 9, italic : true, alignment : 'center'}],
        // [{text : vrn, fontSize : 9, italic : true, alignment : 'center'}]
      ]
    }
    return address
  }

  

  async getDocumentHeader(){
    await this.getCompanyProfile()
    var cName = this.companyName
    var cPostalAddress = 'P.O. Box '+this.postCode
    var cPhysicalAddress = this.physicalAddress
    var cTelephone = 'Tel: '+this.telephone
    var cMobile = 'Mob: '+this.mobile
    var cFax = 'Fax: '+this.fax
    var cEmail = 'Email: '+this.email
    var cWebsite = this.website
    var tin = 'TIN: '+this.tin
    var vrn = 'VRN: '+this.vrn

    var logo : any = await this.getLogo()

    if(logo == ''){
      logo = { text : '', width : 70, height : 70, absolutePosition : {x : 40, y : 40}}
    }else{
      logo = {image : logo, width : 70, absolutePosition : {x : 40, y : 40}}
    }

    var address = [
      {text : cName, fontSize : 18, bold : true, alignment : 'center'},
      {table : {
        headerRows : 0,
        widths: ['100%'],
        body : [[{text : '.', fontSize : 9, fillColor : '#546f9c', height : 30}]]
      },
      layout : 'headerLineOnly'},
      {text : cPhysicalAddress + ' ' + cPostalAddress + ' ' + cTelephone + ' ' + cEmail + ' ' + cWebsite, fontSize : 9,  alignment : 'center'},
    ]

    var header : any = {
      columns : 
      [
        logo,
        {width : 10, columns : [[]]},
        {
          width : 400,
          columns : [
            address
          ]
        },
      ]
    }

    return header
  }

  async getDocumentHeaderLandScape(){
    await this.getCompanyProfile()
    var cName = this.companyName
    var cPostalAddress = 'P.O. Box '+this.postCode
    var cPhysicalAddress = this.physicalAddress
    var cTelephone = 'Tel: '+this.telephone
    var cMobile = 'Mob: '+this.mobile
    var cFax = 'Fax: '+this.fax
    var cEmail = 'Email: '+this.email
    var cWebsite = this.website
    var tin = 'TIN: '+this.tin
    var vrn = 'VRN: '+this.vrn

    var logo : any = await this.getLogo()

    if(logo == ''){
      logo = { text : '', width : 70, height : 70, absolutePosition : {x : 40, y : 40}}
    }else{
      logo = {image : logo, width : 70, absolutePosition : {x : 40, y : 40}}
    }

    var address = [
      {text : cName, fontSize : 18, bold : true, alignment : 'center'},
      {table : {
        headerRows : 0,
        widths: ['160%'],
        body : [[{text : '.', fontSize : 9, fillColor : '#546f9c', height : 30}]]
      },
      layout : 'headerLineOnly'},
      {text : cPhysicalAddress + ' ' + cPostalAddress + ' ' + cTelephone + ' ' + cEmail + ' ' + cWebsite, fontSize : 9,  alignment : 'center'},
    ]

    var header : any = {
      columns : 
      [
        logo,
        {width : 10, columns : [[]]},
        {
          width : 400,
          columns : [
            address
          ]
        },
      ]
    }

    return header
  }
  

  
}




export interface ICompany{
  logo : Byte[]
} 

export interface IBranchReceiptHeader{
  companyName : string
  branchName : string
  address : string
  location : string
  email : string
  website : string
  tin : string
  vrn : string
}

