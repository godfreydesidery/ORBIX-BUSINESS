import { IBranch } from "./branch"

export interface IWorkshop {
    id : number
    code : string
    name : string
    locationName : string

    workshopCategory : string

    active : string

    branchId : string
    branchName : string

    otherInfo : string

    branch : IBranch

    //Numbering
    sn : number
}