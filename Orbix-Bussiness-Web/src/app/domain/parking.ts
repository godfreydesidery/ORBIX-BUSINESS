export interface IParking {
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
    leftFrontLamp: string
    rightFrontLamp: string
    leftRearLamp: string
    rightRearLamp: string
    leftSideMirror: string
    rightSideMirror: string
    leftWiper: string
    rightWiper: string
    backWiper: string
    fuelCap: string
    spareTire: string
    battery: string
    starter: string
    aerial: string
    wheelCap: string
    roundMirror: string
    tireIndicator: string
    //image: Byte[]
    vehicleAndEquipmentCategory : string

    billingType : string
    billingAmount : number

    status: "PENDING",

    // Foreign keys
    parkingZoneId: any,
    vehicleAndEquipmentTypeId: any,
    branchId: any,
    companyId: any


    sn: number

    vehicleAndEquipmentTypeName : string

    parkingZoneName : string

    cardNo : string

}