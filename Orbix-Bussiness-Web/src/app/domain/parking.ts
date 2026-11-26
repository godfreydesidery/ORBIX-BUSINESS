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
    deviceCount: string // how many tracking devices are attached; '' when none or not recorded
    //image: Byte[]

    comments : string


    billingType : string
    billingAmount : number
    billingStartAt : string

    status: string, // PENDING (awaiting registration), REGISTERED (awaiting check-in), CHECKED-IN, CHECKED-OUT

    // INSIDE the yard, or OUTSIDE on a temporary trip
    custody: 'INSIDE' | 'OUTSIDE'

    

    // Foreign keys
    parkingZoneId: any,
    vehicleEquipmentTypeId: any,
    branchId: any,
    companyId: any


    sn: number


    parkingZoneName : string
    parkingSlotNo : string // the slot within the zone; '' when the zone has no slots
    vehicleEquipmentId : any // the registered vehicle, to open its inspection again

    cardNo : string

    serviceBillItems : IServiceBillItem[]

    // Inspection and driver; signatures come from /parkings/get_inspection
    interiorDetails : string
    bodyCondition : string
    driverName : string
    driverPhoneNo : string
    driverIdType : string
    driverIdNo : string
}

// A signature given at one step of the parking
export interface IParkingSignature {
    stage : 'INSPECTION' | 'REGISTRATION' | 'MODIFICATION'
    role : 'INSPECTOR' | 'DRIVER' | 'REGISTRAR' | 'MODIFIER'
    signerName : string
    signedAt : string
    image : string // data:image/png;base64,...
    changes : string // MODIFICATION only: one "Field: old → new" per line
}

export interface IParkingInspection {
    parkingId : string
    signatures : IParkingSignature[]
}

export interface IServiceBillItem{
    sn : number
    item : string
    qty : number
    payStatus : string
    amount : number
}

// The owner is entered by the registrar; until then the API keeps the placeholder "PENDING REGISTRATION"
export const OWNER_PENDING_FIRST_NAME = 'PENDING'
export const OWNER_PENDING_LAST_NAME = 'REGISTRATION'

export function isOwnerPending(firstName : string | null | undefined, lastName : string | null | undefined) : boolean {
    return firstName === OWNER_PENDING_FIRST_NAME && lastName === OWNER_PENDING_LAST_NAME
}
