import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { AuthService } from 'src/app/auth.service';
import { SearchFilterPipe } from 'src/app/custom-pipes/search-filter';
import { IBillReceivable, IParkingBillReceivable } from 'src/app/domain/bill-receivable';

@Component({
  selector: 'az-vehicle-equipment-billing',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    SearchFilterPipe,
    NgxPaginationModule,
    RouterModule
  ],
  templateUrl: './vehicle-equipment-billing.component.html',
  styleUrl: './vehicle-equipment-billing.component.scss'
})
export class VehicleEquipmentBillingComponent {
  // Parking attributes
  parkingId : any = null
  parkingBillReceivableId : any = null
  parkingBillReceivableDescription : string = ''
  parkingBillReceivableStartingDate : Date | null
  parkingBillReceivableEndingDate : Date | null
  parkingBillReceivablePrice : number = 0
  parkingBillReceivableQty : number = 0
  parkingBillReceivableDiscount : number = 0
  parkingBillReceivableStatus : string = ''

  // Service attributes
  serviceBillReceivableId : any = null
  serviceBillReceivableDescription : string = ''
  serviceBillReceivablePrice : number = 0
  serviceBillReceivableQty : number = 0
  serviceBillReceivableDiscount : number = 0
  serviceBillReceivableStatus : string = ''

  //Parking and bills Collections attributes
  parkingBillReceivables : IParkingBillReceivable[] = []
  //serviceBillReceivables : IServiceBillReceivable[] = []
  billReceivables : IBillReceivable[] = []


  constructor(
    private http :HttpClient,
    private auth : AuthService
  ) {}



  saveParkingBill() { 
    // If parking bill receivable id is null, create new parking bill receivable
    // If parking bill receivable id is not null, update parking bill receivable
    if(this.parkingId != null) {
      // Here continue
    }else{
      alert('No parking available')
    }
  }

  saveServiceBill() {
    // If service bill receivable id is null, create new service bill receivable
    // If service bill receivable id is not null, update service bill receivable
    if(this.parkingId != null) {
      
    }else{
      alert('No parking available')
    }
  }

  refreshBills() {
    // Refresh both parking and service bills

  }

  getUnpaidBills() { }  

  confirmBillPayment() { }

  refreshBillsAmount() { }

  generateReceipt() { }

}
