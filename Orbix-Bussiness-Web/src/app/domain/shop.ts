import { IBranch } from "./branch"

export interface IShop {
    id : number
    code : string
    name : string
    locationName : string

    active : string

    branchId : string
    branchName : string

    otherInfo : string

    branch : IBranch

    //Numbering
    sn : number
}