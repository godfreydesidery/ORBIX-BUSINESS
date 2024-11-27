import { IBranch } from "./branch"
import { ICompany } from "./company"

export interface IVehicleEquipmentType {
    //Basic attributes
    id : any
    code : string
    name : string

    dailyPrice : number

    active : string

    company : ICompany

    companyName : string

    //Numbering
    sn : number
}