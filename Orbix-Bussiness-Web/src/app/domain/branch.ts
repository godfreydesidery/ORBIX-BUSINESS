import { ICompany } from "./company"

export interface IBranch {

    id : number
    code : string
    name : string
    level : string
    type : string
    active : string
    physicalAddress : string
    postalCode : string
    postalAddress : string
    telephone : string
    mobile : string
    email : string
    website : string
    fax : string
    city : string
    state : string
    country : string
    managerName : string
    openingHours : string
    numberOfStaff : number
    dateEstablished : Date
    notes : string


    companyId : string
    companyName : string

    company : ICompany
    parentBranch : IBranch
    childBranches : IBranch[]

    //Numbering
    sn : number
}

