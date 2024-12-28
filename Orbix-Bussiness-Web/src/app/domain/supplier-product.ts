export interface ISupplierProduct {
    id : number
    supplierId : number
    supplierCode : string
    supplierName : string

    productId : number
    productCode : string
    productName : string
    productDescription : string

    maxSupplyQty : number
   
    vatRate : number

    costPriceVatIncl : number

    active : string
    created : string
    createdDateTime : string

}