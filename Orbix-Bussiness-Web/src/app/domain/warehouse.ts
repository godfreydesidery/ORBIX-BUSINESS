import { IBranch } from "./branch"
import { ICompany } from "./company"

export interface IWarehouse {
    //Basic attributes
    id : any
    code : string
    name : string

    location : string

    noOfSections : number
    
    active : string

    company : ICompany

    branch : IBranch

    companyName : string
    branchName : string


    //Numbering
    sn : number
}