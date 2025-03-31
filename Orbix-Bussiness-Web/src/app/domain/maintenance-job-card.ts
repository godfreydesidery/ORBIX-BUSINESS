import { IMaintenanceJobCardIssue } from "./maintenance-job-card-issue"

export interface IMaintenanceJobCard {
    id : any
    no : string
    status : string

    maintenanceId : any
    maintenanceNo : string

    ownerName : string
    vehicleEquipmentTypeName : string
    vehicleEquipmentName : string
    chasisNo : string
    hasKeys : string
    ownerPhoneNo : string

    sn : number

    maintenanceJobCardIssues : IMaintenanceJobCardIssue[]
}