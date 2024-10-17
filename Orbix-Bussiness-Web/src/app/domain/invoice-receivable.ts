export interface IInvoiceReceivable {
    id : string
    no : string
    status : string
    summary : string
    createdBy : string
    createdDateTime : string

    branchId : string
    branchName : string
    companyId : string
    companyName : string

    ownerName : string
    ownerPhoneNo : string
    chasisNo : string
    cardNo : string
    model : string

    invoiceReceivableDetails : IInvoiceReceivableDetail[]


    sn : number

}

export interface IInvoiceReceivableDetail {
    id : any
    amount : number
    paid : number
    due : number
    status : string
    summary : string

}