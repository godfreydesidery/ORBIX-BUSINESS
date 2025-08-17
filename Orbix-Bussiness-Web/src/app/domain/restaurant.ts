import { IBranch } from "./branch"

export interface IRestaurant {
    id : number
    code : string
    name : string
    locationName : string

    restaurantCategory : string

    active : string

    branchId : string
    branchName : string

    otherInfo : string

    branch : IBranch

    //Numbering
    sn : number
}