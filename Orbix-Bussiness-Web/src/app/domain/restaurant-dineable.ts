
export interface IRestaurantDineable {
    id : number
    dineableId : number
    dineableCode : string
    dineableName : string
    dineableDescription : string
    baseUom : string
    restaurantId : number
    restaurantName : string
    currentStock : number
    minStock : number
    maxStock : number
    defaultReorderQty : number
    defaultReorderLevel : number
    vatRate : number
    vatPercentage : number
    costPriceVatIncl : number
    costPriceVatExcl : number
    sellingPriceVatIncl : number
    sellingPriceVatExcl : number
    active : string
    created : string
    createdDateTime : string

}