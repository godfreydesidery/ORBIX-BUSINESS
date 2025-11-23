import { IMachineService } from "./machine-service"

export interface IMachine {

    id: any
    no: string
    name: string
    machineRegNo: string
    ownerName: string
    ownerPhoneNo: string

    machineServices : IMachineService[]

    sn : number
}