import { Injectable } from '@angular/core';
import { IPharmacyCustomer } from '../domain/pharmacy-customer';
import { DatePipe } from '@angular/common';
import * as pdfMake from 'pdfmake/build/pdfmake';
import { ReceiptItem } from '../domain/receipt-item';
import { DataService } from './data.service';

@Injectable({
  providedIn: 'root'
})
export class PharmacyPosReceiptService {
  constructor(private datePipe : DatePipe,
    private data : DataService) {}

  print = async (items : ReceiptItem[], receiptNo :string, cash : number, pharmacyCustomer : IPharmacyCustomer) => {

    var companyName = localStorage.getItem('company-name')!

    var header = ''
    var footer = ''
    var title  = 'Receipt'
    var total : number = 0
    var discount : number = 0
    var tax : number = 0

    var address : any = await this.data.getReceiptHeader(receiptNo)
   
    var receipt = [
      [
        {text : 'SN', fontSize : 8, bold : true}, 
        {text : 'Item', fontSize : 8, bold : true},
        {text : 'Qty', fontSize : 8, bold : true},
        {text : 'Amount', fontSize : 8, bold : true},
      ]
    ] 
    
    var sn = 0

    items.forEach((element) => {
      total = total + element.amount
      sn = sn + 1
      var item = [
        {text : sn.toString(), fontSize : 8, bold : false}, 
        {text : element.name, fontSize : 8, bold : false},
        {text : element.qty.toString(), fontSize : 8, bold : false},
        {text : (element.amount).toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 8, alignment : 'right', bold : false},
      ]
      receipt.push(item)
    })
    var detailSummary = [
      {text : ' ', fontSize : 8, bold : false},
      {text : 'Total', fontSize : 9, bold : true},
      {text : ' ', fontSize : 8, bold : false},
      {text : total.toLocaleString('en-US', { minimumFractionDigits: 2 }), fontSize : 9, alignment : 'right', bold : true},
    ]
    receipt.push(detailSummary)
    

    const docDefinition = {
      header: '',
      
      //watermark : { text : '', color: 'blue', opacity: 0.1, bold: true, italics: false },
        content : [
          {
            layout : 'noBorders',
            table : address
          }, 
          
          
          
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
              ]
            }
          }, 
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                [{text : receiptNo, fontSize : 8}],
                [{text : pharmacyCustomer?.name, fontSize : 8}],
                [{text : pharmacyCustomer?.no, fontSize : 8}],
                [{text : pharmacyCustomer?.address, fontSize : 8}],
                [{text : pharmacyCustomer?.phoneNo, fontSize : 8}],
                [{text : '________________________________',alignment : 'center',}],
              ]
            }
          },  
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [200],
              body : [
                [{text : 'Sales Receipt', alignment : 'center', fontSize : 9, bold : true}],
              ]
            }
          },      
          {
            layout : 'noBorders',
            table : {
                headerRows : 1,
                widths : [15, 100, 15, 50],
                body : receipt
            }
          },
          {
            layout : 'noBorders',
            table : {
              headerRows : 0,
              widths : [210],
              body : [
                [{text : '=============================='}],
                [{text : 'Served By : '+ localStorage.getItem('user-name'), fontSize : 9, alignment : 'left'}],
                [{text : 'Developed By @Zana Systems', fontSize : 10, bold : true, alignment : 'center'}],
                [{text : '***End of Receipt***', fontSize : 9, alignment : 'center'}]
              ]
            }
          },
        ],
        pageMargins: 10,
      }
      const win = window.open('', "tempWinForPdf")
      pdfMake.createPdf(docDefinition).print({}, win)
      //win!.onfocus = function () { setTimeout(function () { win!.close(); }, 10000); } //set to 10 seconds
  }

  
}