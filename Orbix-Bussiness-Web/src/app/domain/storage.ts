
export interface IStorage {
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
    goodName: string
    goodDescription : string
    weight: number
    length: number
    width: number
    height: number
    startBillingAt: Date | null
    status: string
    billingType: string
    billingAmount: number
    initialQty: number
    currentQty: number
    warehouseId: any
    goodTypeId: any
    goodTypeName: string

    // numbering
    sn : number
}