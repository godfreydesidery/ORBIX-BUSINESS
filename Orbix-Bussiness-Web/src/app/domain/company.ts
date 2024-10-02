import { Byte } from "src/custom-packages/util"

export interface ICompany {
    //Basic attributes
    id : any
    code : string
    name : string
    brandName : string
    contactName : string
    symbol : string
    domain : string
    active : boolean
    legalType : string
    industry : string
    country : string
    timeZone : string
    foundingDate : Date
    logo : Byte[]
    tin : string
    vrn : string
    physicalAddress : string
    postalCode : string
    postalAddress : string
    telephone : string
    mobile : string
    email : string
    website : string
    fax : string

    //Numbering
    sn : number
}