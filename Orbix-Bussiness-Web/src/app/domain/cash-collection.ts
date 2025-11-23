export interface ICashCollection {
  reason: string
  amount: number
  paymentType: string
  payCode: string
  cashierName: string

  sn: number
}

export interface IParkingCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  vehicleEquipmentCategory: string
  vehicleEquipmentName: string
  ownerFirstName: string
  ownerLastName: string
  cardNo: string
  ownerPhoneNo: string
  chasisNo: string
  createdDateTime: string
  days: number
  discount: number

  sn: number
}

export interface IParkingServiceCashCollection {

  amount: number
  paymentType: string
  payCode: string
  cashierName: string
  reason: string
  vehicleEquipmentCategory: string
  vehicleEquipmentName: string
  ownerFirstName: string
  ownerLastName: string
  cardNo: string
  ownerPhoneNo: string
  chasisNo: string
  createdDateTime: string
  qty: number
  serviceDescription: string
  discount: number

  sn: number
}

export interface ISalesCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  productName: string
  dateTime: string
  qty: number
  discount: number

  sn: number
}

export interface IRestaurantSalesCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  productName: string
  dateTime: string
  qty: number
  discount: number

  sn: number
}


export interface IStorageCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  goodName: string
  ownerFirstName: string
  ownerLastName: string
  ownerPhoneNo: string
  dateTime: string
  createdDateTime: string
  days: number
  discount: number

  sn: number
}

export interface IMaintenanceCashCollection {

  issueName: string
  amount: number
  paymentType: string
  cashierName: string
  reason: string
  vehicleEquipmentCategory: string
  vehicleEquipmentName: string
  ownerFirstName: string
  ownerLastName: string
  cardNo: string
  ownerPhoneNo: string
  chasisNo: string
  createdDateTime: string
  days: number
  discount: number

  sn: number
}

export interface IWeighCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  goodName: string
  regNo: string
  ownerFirstName: string
  ownerLastName: string
  ownerPhoneNo: string
  createdDateTime: string
  days: number
  discount: number

  weightOne: string
  weightTwo: string
  weightThree: string
  weightFour: string

  sn: number
}

export interface IWorkshopCashCollection {

  amount: number
  paymentType: string
  cashierName: string
  reason: string
  goodName: string
  regNo: string
  ownerName: string
  machineName: string
  ownerPhoneNo: string
  serviceName: string
  createdDateTime: string
  days: number
  discount: number
  sn: number
}