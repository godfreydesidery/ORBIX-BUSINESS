import { IBranch } from "./branch"
import { ICompany } from "./company"

export interface IBondItemType {
    //Basic attributes
    id : any
    code : string
    name : string

    dailyPrice : number
    hourlyPrice : number

    active : string

    currency : string

    company : ICompany

    companyName : string

    //Numbering
    sn : number
}