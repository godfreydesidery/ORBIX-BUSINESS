export interface IParking {
    vehicleEquipmentTypeName: string
    vehicleEquipmentName: string
    vehicleEquipmentColor: string
    vehicleEquipmentCategory: string
    id : any
    no : string

    // Owner information
    ownerFirstName: string
    ownerMiddleName: string
    ownerLastName: string
    ownerCompanyName: string
    ownerIdNo: string
    ownerIdType: string
    ownerPhoneNo: string
    ownerEmail: string
    ownerAddress: string

    // Agent Information
    agentName: string
    agentAddress: string
    agentPhoneNo: string
    agentEmail: string
    tformNumber: string

    // Vehicle or Equipment Information
    registrationNo: string
    chasisNo: string
    leftFrontLamp: boolean
    rightFrontLamp: boolean
    leftRearLamp: boolean
    rightRearLamp: boolean
    leftSideMirror: boolean
    rightSideMirror: boolean
    leftWiper: boolean
    rightWiper: boolean 
    backWiper: boolean
    fuelCap: boolean
    spareTire: boolean
    battery: boolean
    starter: boolean
    aerial: boolean
    wheelCap: boolean
    roundMirror: boolean    
    tireIndicator: boolean
    hasKeys: boolean
    deviceStatus: boolean
    //image: Byte[]

    comments : string


    billingType : string
    billingAmount : number
    billingStartAt : string

    status: "PENDING",

    

    // Foreign keys
    parkingZoneId: any,
    vehicleEquipmentTypeId: any,
    branchId: any,
    companyId: any


    sn: number


    parkingZoneName : string

    cardNo : string

    serviceBillItems : IServiceBillItem[]
}

export interface IServiceBillItem{
    sn : number
    item : string
    qty : number
    payStatus : string
    amount : number
}
