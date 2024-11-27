export interface IBillReceivable {
    id : any
    amount : number
    paid : number
    due : number
    payStatus : string
    summary : string

    qty : string

    createdDateTime : string

    sn : number
}

export interface IParkingBillReceivable {
    id : any
    startedAt : Date
    endedAt : Date
    description : string
    billingType : string
    qty : number
    price : number

    discount : number

    amount : number

    parkingId : string
    billReceivableId : string

    payStatus : string

    sn : number

}

export interface IServiceBillReceivable {
    id : any
    description : string
    billingType : string
    serviceDate : Date
    qty : number
    price : number

    discount : number

    amount : number

    parkingId : string
    billReceivableId : string

    payStatus : string

    sn : number

}