import { IBranch } from "./branch"
import { ICompany } from "./company"

export interface IParkingZone {
    //Basic attributes
    id : any
    code : string
    name : string

    noOfSlots : number
    
    active : string

    company : ICompany

    branch : IBranch

    companyName : string
    branchName : string


    //Numbering
    sn : number
}