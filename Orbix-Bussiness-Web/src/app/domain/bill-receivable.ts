export interface IBillReceivable {
    id : any
    amount : number
    paid : number
    due : number
    status : string
    summary : string

    qty : string

    createdDateTime : string

    sn : number
}

export interface IParkingBillReceivable {
    id : any
    startedAt : Date
    endedAt : Date
    billingType : string
    qty : number
    price : number

    discount : number

    amount : number

    parkingId : string
    billReceivableId : string

    status : string

    sn : number

}