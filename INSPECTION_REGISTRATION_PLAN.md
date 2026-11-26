# Vehicle Inspection and Registration: Plan

Source: the client's requirements, "Sighman Maboresho.pdf" (Mahitaji ya Mfumo wa Ukaguzi na Usajili wa Magari).
Branch: `inspection-registration`.

## Flow

```
Inspector + Driver   Reception › Vehicle Register (parking mode, "Vehicle Inspection")
                       vehicle info, condition checklist, interior details, devices attached (item + qty),
                       body condition, owner/agent details, Inspector signature, Driver signature (typed name)
                       [Submit Inspection]                                        -> status PENDING
Registrar            Reception › Parking › Awaiting Registration › [Open]
                       inspection (read-only), driver details, vehicle type, category, parking zone
                       (only zones with space), card no, keys, Registrar signature
                       [Register & Check In]                                      -> status CHECKED-IN
While parked         View (sections + signatures), Modify (+ signature, what changed), Send Out / Send In
Cashier              Accounts & Finance › Release Vehicles                        -> status CHECKED-OUT
```

The statuses do not change. PENDING means "inspected, awaiting registration", and the registrar's signed save is
the check-in.

## Decisions

- Registration and check-in are one action.
- Owner/agent details stay on the inspection form, because `owner_first_name` and `owner_last_name` are NOT NULL.
- The existing Vehicle Register page is reused as the inspection form, on its parking path only.
- Zone space is counted, not tracked by numbered slot: free = `noOfSlots` minus the CHECKED-IN parkings in the zone.
  `noOfSlots = 0` means unlimited, full zones are hidden, and space frees itself at check-out. A vehicle out on a
  trip keeps its space.
- Editing a PENDING inspection needs no new signatures and is audited only.
- Signatures are tiny: a black and white PNG of about 200×70, usually under 1 KB, and the API rejects anything over
  5 KB. They are stored in MySQL in `parking_signatures`; rows are only ever inserted.
- **No mandatory database fields.** Every new column is nullable. "Required" is enforced by the form and the API,
  for new records only. Existing records keep working, and View shows "No signature" where one is missing.
- Audit log entries are deleted after 90 days. Each modification therefore keeps its own "what changed" text on its
  signature row.

## Database (added by `ddl-auto = update`)

- `parkings`: + `interior_details`, `body_condition`, `driver_name`, `driver_phone_no`, `driver_id_type`,
  `driver_id_no`
- `parking_devices`: `parking_id`, `name`, `qty`
- `parking_signatures`: `parking_id`, `stage` (INSPECTION / REGISTRATION / MODIFICATION),
  `role` (INSPECTOR / DRIVER / REGISTRAR / MODIFIER), `user_id`, `signer_name`, `image`, `signed_at`, `changes`

## API

- `POST /parkings/create` (Submit Inspection, `PKNG-INSPECT`): requires both signatures and the driver's name;
  saves the devices; no zone or category is set.
- `POST /parkings/update` (Edit while PENDING, `PKNG-INSPECT`): no zone or category; replaces the device list.
- `POST /parkings/check_in` (Register & Check In, `PKNG-REGISTER`): zone, category, type, driver and the registrar
  signature; locks the zone and checks its space.
- `POST /parkings/modify`: requires the modifier signature and records what changed.
- `GET /parkings/get_inspection?id=`: devices and signatures of one vehicle.
- `GET /parking_zones/get_all_branch_available`: active zones with space, with their free count.
- `GET /parkings/get_all_pending_or_checked_in_page`: optional `status` filter.
- New privileges: `PKNG-INSPECT`, `PKNG-REGISTER`.

## Web

- A signature pad component.
- The inspection form (Vehicle Register, parking mode).
- The Parking list: Awaiting Registration / Checked In tabs, Open (Register & Check In), View with sections and
  signatures, Modify with a signature.

## Found while building

- "Register Parking" (`/vehicle_equipments/create` with service PARKING) used to create a PENDING parking straight
  away, with every checklist item ticked. It now creates only the vehicle record. The parking is created when the
  inspector submits the signed inspection, so no vehicle waits for registration without its inspection.
- `InvalidOperationException` never passed its message to `RuntimeException`, so the catch-all handler answered
  with an empty message ("message": null). That hid every "invalid operation" reason in the app, including
  "Parking Zone ... is full". It now passes the message on.
- A missing privilege (`@PreAuthorize`) comes back as 500 "Access is denied", the app's existing behaviour, not 403.
- Opening a vehicle for registration now sets the billing start date to today. Before, it carried the parking's full
  timestamp, which the API could not parse as a date.

## Tested (local API against the `orbtest` database)

`ddl-auto=update` created the new tables and columns. An end-to-end script passed all 38 checks:
- inspection refused without signatures, driver name, body condition or PKNG-INSPECT, and refused for a non-PNG or
  an over-5 KB signature;
- signed inspection → PENDING, with its devices and both signatures;
- the Awaiting Registration and Checked In filters;
- editing a pending inspection without signatures;
- registration refused without the registrar signature, a valid category or PKNG-REGISTER;
- Register & Check In saves the category, zone, driver and signature;
- a full zone is hidden and refused;
- Modify needs a signature, records "Owner phone: old → new", and the earlier signatures survive.

Signatures from a 200×70 black and white scribble came to about 620–640 bytes. The web pages compile, but the
screens and the signature pad have not been tried in a browser yet.

## Later: removing old signatures (manual, not built)

Signatures can be removed to save space with a **manual** action, to be added later; there will be no automatic job.
Agreed direction:
- clear only the image (`image = NULL`), keeping who signed, when, and what changed;
- clear only vehicles checked out more than a chosen period ago (e.g. 1 year after check-out), never vehicles still
  in the yard;
- record each clean-up in the audit log;
- the client should confirm the period first, because the signatures are their evidence in damage disputes.

The database already allows this, since every column is nullable. After clearing, run
`OPTIMIZE TABLE parking_signatures` if the disk space itself should be returned.

## Roll-out

1. Deploy the API, then the web app.
2. **Grant `PKNG-INSPECT` and `PKNG-REGISTER` to the reception roles before staff start work.** Without them, creating
   and checking in vehicles is refused.

## Risks

- Category stays empty until registration, so any report grouping by category shows PENDING vehicles as blank.
- Changing the vehicle type at registration changes the daily price.
