
export interface IBondItem {
    id: any
    no: string
    ownerFirstName: string
    ownerMiddleName: string
    ownerLastName: string
    ownerCompanyName: string
    ownerIdNo: string
    ownerIdType: string
    ownerPhoneNo: string
    ownerEmail: string
    ownerAddress: string
    comments: string
    bondItemName: string
    bondItemDescription : string
    weight: number
    length: number
    width: number
    height: number
    startBillingAt: Date | null
    status: string
    billingType: string
    billingAmount: number
    billingStartAt : string
    initialQty: number
    currentQty: number
    bondZoneId: any
    bondZoneName: string
    bondItemTypeId: any
    bondItemTypeName: string

    currency : string

    serviceBillItems : IServiceBillItem[]

    // numbering
    sn : number

    //////////////////

    vehicleEquipmentTypeName: string
        vehicleEquipmentName: string
        vehicleEquipmentColor: string
        vehicleEquipmentCategory: string

    
      
    
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
    
        
    
        // Foreign keys
        parkingZoneId: any,
        vehicleEquipmentTypeId: any,
        branchId: any,
        companyId: any
    
    

    
    
        parkingZoneName : string
    
        cardNo : string
    

    //////////////////
}

export interface IServiceBillItem{
    sn : number
    item : string
    qty : number
    noOfDays : number
    payStatus : string
    amount : number
}