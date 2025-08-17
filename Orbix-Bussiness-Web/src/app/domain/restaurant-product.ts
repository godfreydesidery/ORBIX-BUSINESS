
export interface IRestaurantProduct {
    id : number
    productId : number
    productCode : string
    productName : string
    productDescription : string
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