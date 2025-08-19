
export interface IRestaurantSalesOrder {
    id : any
    no : string
    summary : string
    customerName : string
    status : string
    restaurantId : number

    createdAt : string

    restaurantBadgeCode : string
    restaurantAgentName : string

    restaurantSalesOrderDetails : IRestaurantSalesOrderDetail[]

    sn : number
}

export interface IRestaurantSalesOrderDetail {
    id : any
    salesOrderId : any
    dineableId : number
    dineableCode : string
    dineableName : string
    dineableDescription : string
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