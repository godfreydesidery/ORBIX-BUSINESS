// A temporary trip of a checked-in vehicle out of the yard and back (service, testing, loading)
export interface IParkingTrip {
    id: any
    sn: number
    no: string                  // gate pass number

    parkingId: any
    parkingNo: string
    chasisNo: string
    registrationNo: string
    vehicleEquipmentTypeName: string
    vehicleEquipmentColor: string
    cardNo: string
    ownerName: string
    ownerPhoneNo: string

    reason: string
    reasonDetails: string
    driverName: string
    driverPhoneNo: string
    driverIdType: string
    driverIdNo: string
    documentsLeft: boolean
    expectedReturnAt: string

    status: 'OUT' | 'RETURNED' | 'NOT_RETURNED'
    overdue: boolean            // still out after the expected return time

    sentOutAt: string
    sentOutBy: string
    receivedAt: string
    receivedBy: string
    documentsReturned: boolean
    returnNotes: string

    minutesOut: number          // until received back, or until now while still out
}

export const TRIP_REASONS = [
    { value: 'SERVICE_REPAIR', label: 'Service / Repair' },
    { value: 'TESTING', label: 'Testing' },
    { value: 'LOADING', label: 'Loading' },
    { value: 'OTHER', label: 'Other' },
]

export const DRIVER_ID_TYPES = ['National ID', 'Driving License', 'Passport', 'Voter ID', 'Other']

export function tripReasonLabel(reason: string): string {
    return TRIP_REASONS.find(r => r.value === reason)?.label ?? reason
}

export function tripStatusLabel(status: string): string {
    switch (status) {
        case 'OUT': return 'Outside'
        case 'RETURNED': return 'Returned'
        case 'NOT_RETURNED': return 'Not returned'
        default: return status
    }
}

// "2 d 3 h", "5 h 20 min", "15 min"
export function tripDuration(minutes: number): string {
    const m = Math.max(0, Math.floor(minutes || 0))
    const days = Math.floor(m / 1440)
    const hours = Math.floor((m % 1440) / 60)
    const mins = m % 60
    if (days > 0) return days + ' d ' + hours + ' h'
    if (hours > 0) return hours + ' h ' + mins + ' min'
    return mins + ' min'
}

// "2026-11-07T14:30:00" -> "2026-11-07 14:30"
export function tripDateTime(value: string): string {
    if (!value) return ''
    return value.replace('T', ' ').substring(0, 16)
}
