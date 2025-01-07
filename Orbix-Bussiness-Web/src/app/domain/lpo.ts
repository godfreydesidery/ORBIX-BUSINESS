export interface ILpo {
    id : any
    no : string
    summary : string
    status : string
    shopId : number
    shopName : string
    supplierId : number
    supplierCode : string
    supplierName : string

    lpoDetails : ILpoDetail[]

    sn : number
}

export interface ILpoDetail {
    id : any
    lpoId : any
    productId : number
    productCode : string
    productName : string
    productDescription : string
    costPriceVatIncl : number
    vatRate : number

    baseUom : string
    qty : number
    price : number
    discount : number
    amount : number
    sn : number

    createdBy : string
    createdDateTime : string
}