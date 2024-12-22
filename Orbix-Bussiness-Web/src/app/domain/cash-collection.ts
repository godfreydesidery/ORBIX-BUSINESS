export interface ICashCollection {
    reason : string  
    amount : number
    paymentType : string
    payCode : string
    cashierName : string

    sn : number
  }

export interface IParkingCashCollection {

  amount : number
  paymentType : string
  cashierName : string
  reason : string
  vehicleEquipmentCategory : string
  vehicleEquipmentName : string
  ownerFirstName : string
  ownerLastName : string
  cardNo : string
  ownerPhoneNo : string
  chasisNo : string
  createdDateTime : string
  days : number
  discount : number

  sn : number
}

export interface IParkingServiceCashCollection {

  amount : number
  paymentType : string
  payCode : string
  cashierName : string
  reason : string
  vehicleEquipmentCategory : string
  vehicleEquipmentName : string
  ownerFirstName : string
  ownerLastName : string
  cardNo : string
  ownerPhoneNo : string
  chasisNo : string
  createdDateTime : string
  qty : number
  serviceDescription : string
  discount : number

  sn : number
}

export interface ISalesCashCollection {

  amount : number
  paymentType : string
  cashierName : string
  reason : string
  productName : string
  dateTime : string
  qty : number
  discount : number

  sn : number
}