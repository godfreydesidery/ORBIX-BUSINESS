export interface IAuditLog {
    id : number
    occurredAt : string // UTC, ISO-8601
    category : string
    action : string
    outcome : string
    userId : number
    username : string
    companyId : number
    branchId : number
    entityType : string
    entityId : string
    entityRef : string
    summary : string
    details : string // JSON
    ipAddress : string
    userAgent : string
}
