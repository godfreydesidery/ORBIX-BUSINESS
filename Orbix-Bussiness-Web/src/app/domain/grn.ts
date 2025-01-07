export interface IGrn {
    id : any
    no : string
    summary : string
    status : string
    shopId : number
    shopCode : string
    shopName : string
    supplierId : number
    supplierCode : string
    supplierName : string

    grnDetails : IGrnDetail[]

    sn : number
}

export interface IGrnDetail {
    id : any
    grnId : any
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