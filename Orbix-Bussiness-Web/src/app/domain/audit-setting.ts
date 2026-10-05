export interface IAuditSetting {
    recordingEnabled : boolean
    updatedAt : string // UTC, ISO-8601; empty until recording is first turned on or off
    updatedBy : string
}
