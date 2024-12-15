
export interface IShopSalesOrder {
    id : any
    no : string
    summary : string
    status : string
    shopId : number

    shopSalesOrderDetails : IShopSalesOrderDetail[]

    sn : number
}

export interface IShopSalesOrderDetail {
    id : any
    salesOrderId : any
    productId : number
    productCode : string
    productName : string
    productDescription : string
    sellingPriceVatIncl : number
    sellingPriceVatExcl : number
    costPriceVatIncl : number
    costPriceVatExcl : number
    vatRate : number
    


    baseUom : string
    qty : number
    price : number
    discount : number
    amount : number
    sn : number
}