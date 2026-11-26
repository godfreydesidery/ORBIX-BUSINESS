# Vehicle Inspection and Registration: Plan

Source: the client's requirements, "Sighman Maboresho.pdf" (Mahitaji ya Mfumo wa Ukaguzi na Usajili wa Magari).
Branch: `inspection-registration`.

## Flow

```
Inspector + Driver   Reception › Vehicle Register (parking mode, "Vehicle Inspection")
                       vehicle info, condition checklist, tracking device (attached + number), interior details,
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
- Owner/agent details: first kept on the inspection form because the owner name columns are NOT NULL; later moved
  to registration with a placeholder owner (see below).
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

- `parkings`: + `device_count` (beside the existing `device_status`), `parking_slot_no`, `interior_details`, `body_condition`,
  `driver_name`, `driver_phone_no`, `driver_id_type`, `driver_id_no`
- `parking_signatures`: `parking_id`, `stage` (INSPECTION / REGISTRATION / MODIFICATION),
  `role` (INSPECTOR / DRIVER / REGISTRAR / MODIFIER), `user_id`, `signer_name`, `image`, `signed_at`, `changes`

## API

- `POST /parkings/create` (Submit Inspection, `PKNG-INSPECT`): requires both signatures and the driver's name;
  an attached tracking device needs its number; no zone or category is set.
- `POST /parkings/update` (Edit while PENDING, `PKNG-INSPECT`): no zone or category.
- `POST /parkings/check_in` (Register & Check In, `PKNG-REGISTER`): zone, category, type, driver and the registrar
  signature; locks the zone and checks its space.
- `POST /parkings/modify`: requires the modifier signature and records what changed.
- `GET /parkings/get_inspection?id=`: the signatures of one vehicle.
- `GET /parking_zones/get_all_branch_available`: active zones with space, with their free count.
- `GET /parkings/get_all_pending_or_checked_in_page`: optional `status` filter.
- New privileges: `PKNG-INSPECT`, `PKNG-REGISTER`.

## Web

- A signature pad component.
- The inspection form (Vehicle Register, parking mode).
- The Parking list: Awaiting Registration / Checked In tabs, Open (Register & Check In), View with sections and
  signatures, Modify with a signature.

## Devices attached

The document's "Device Attached" with "idadi ya vifaa" means the tracking device (ECTS) fitted to transit vehicles.
The existing Device Status (Attached / Not attached) stays, with a Number of Devices beside it, required when
Attached for a new inspection. A first version used a free list of items with quantities; it was replaced, since
loose items are already covered by the yes/no condition checklist.

## Client's second write-up (same branch)

- The driver signs at inspection without a name. The name under the driver's signature stays blank until the
  registrar enters the driver's details, then is filled in automatically (and follows a correction made with Modify).
- The inspector's name, date and time appear under their signature as soon as they sign.
- The inspection page has a **Vehicle List** button: inspected vehicles awaiting registration, searchable by chassis,
  which the inspector can open and edit until check-in.
- The registrar's list is the Parking page's **List of Vehicles (Awaiting Registration)** tab, searched by chassis.
- **Parking slots:** each zone's slots are numbered 1 to its no of slots. The registrar picks a free slot after the
  zone (Open), and Modify can move a vehicle to another free zone or slot. A taken slot is refused, a zone with no free
  slot is hidden, and check-out frees the slot. One nullable column, `parkings.parking_slot_no`, with no slot table.
  Zones with no of slots 0 still have no limit and no slots. Vehicles checked in before slots existed have no slot.
- The registrar's save still checks the vehicle in; there is no separate check-in step.
- Styling: signature boxes with a header, a "Signed" badge, a signing line and a Clear button; larger buttons with
  icons for New Vehicle / Existing Vehicle / Vehicle List and for the Parking tabs.

## Owner and agent at registration (placeholder)

The client moved the owner's details to registration. The owner name columns of `parkings` and
`vehicle_equipments` are NOT NULL and the records are created at inspection, so until registration the owner is
saved as the placeholder **PENDING REGISTRATION** (first name `PENDING`, last name `REGISTRATION`). No schema change.
- Inspection (New Vehicle / the inspection form) has no owner or agent fields; Sub T1 stays with the inspector,
  under "Transit Document".
- Before registration, owner and agent information is hidden everywhere: the Vehicle List, the Parking list's owner
  cell, the Edit form, the read-only part of Open, and View.
- Open has an **Owner Details** section (names required, ID, phone, agent). Register & Check In refuses a missing
  owner or the placeholder, and copies the real owner and agent onto the vehicle record too, so "Existing Vehicle"
  fills them in next time.
- Modify refuses a blank or placeholder owner.

## One-step inspection

For parking, **New Vehicle** opens the whole inspection on one screen: Sub T1, the condition checklist, Device Status
and Number of Devices, vehicle type / chassis / colour, interior details, body condition, comments and both
signatures, then **Submit Inspection** once. There is no separate "Register Vehicle" step. The API registers the
vehicle with the inspection in the same transaction (`/parkings/create` without a vehicle id), reusing a vehicle
already registered with that chassis and still waiting for its inspection rather than duplicating it. Existing
Vehicle still opens a known vehicle and inspects it.

## Who enters what (latest)

- **Inspection:** vehicle type, **category** (In-transit / Local), chassis, colour, the condition checklist, device
  status and number of devices, interior details, body condition, comments, inspector and driver signatures.
- **Registration (Open):** owner and agent details, **Sub T1 number**, driver details, parking zone and slot, keys,
  card no, billing start date, registrar signature. Vehicle type and category are shown read-only there.
- A vehicle inspected before category moved to the inspection must have its inspection edited (category chosen)
  before it can be registered.
- The Parking page no longer has the All / List of Vehicles / Checked In buttons: one list, searched by chassis.

## Check-in is a separate step (latest flow)

The client separated check-in from registration, which adds one parking status, **REGISTERED** (awaiting
check-in), between PENDING (awaiting registration) and CHECKED-IN.

```
Inspector   Vehicle Register › New Vehicle: one-screen inspection, signed     -> PENDING
Inspector   Vehicle Register › Vehicle List (every inspected vehicle, with its status) and
            Awaiting Registration (PENDING only); Open / Edit until check-in
Registrar   Awaiting Registration › Register (opens on the Parking page): owner, agent, Sub T1, driver,
            registrar signature                                                -> REGISTERED
Reception   Parking (awaiting check-in + checked in; never awaiting registration) › Check In:
            parking zone and slot, keys, card no, billing start date (no signature)  -> CHECKED-IN
```

- API: `POST /parkings/register` (PKNG-REGISTER) and `POST /parkings/check_in` (PKNG-REGISTER, only for REGISTERED).
  The page list accepts `status` as a comma list of PENDING, REGISTERED, CHECKED-IN.
- REGISTERED counts as active in the duplicate-chassis checks and Existing Vehicle. The inspection can still be edited
  while REGISTERED. Reports that count CHECKED-IN vehicles do not count REGISTERED ones.
- View shows 1. Inspection, 2. Registration, 3. Check-in, 4. Modifications.

## Zone and slot at registration, optional check-in signature (latest)

- The registrar picks the **parking zone and slot** in Register, below the owner and driver details and above the
  registrar's signature, which sits in the middle of the dialog. The slot is held from registration until check-out:
  zone free counts and taken slots count REGISTERED and CHECKED-IN vehicles.
- **Check In** takes keys, card no and billing start date, shows the zone and slot read-only, and offers an
  **optional** signature (stage CHECK_IN, role CHECK_IN_OFFICER), shown in View under 3. Check-in.
- Once a zone is chosen the slot field always shows; a zone with no of slots 0 says it has no numbered slots.
- The Parking list is bordered.

## Found while building

- "Register Parking" (`/vehicle_equipments/create` with service PARKING) used to create a PENDING parking straight
  away, with every checklist item ticked. It now creates only the vehicle record. The parking is created when the
  inspector submits the signed inspection, so no vehicle waits for registration without its inspection.
- `InvalidOperationException` never passed its message to `RuntimeException`, so the catch-all handler answered
  with an empty message ("message": null). That hid every "invalid operation" reason in the app, including
  "Parking Zone ... is full". It now passes the message on.
- A missing privilege (`@PreAuthorize`) comes back as 500 "Access is denied", the app's existing behaviour, not 403.
- Fixed after review:
  - the same chassis could be registered twice while waiting for its inspection;
  - Modify could move a vehicle into a full zone;
  - the screens now say which privilege is missing instead of silently hiding the buttons;
  - branch and company ids in the parking module are compared by value (`Long` `!=` failed for ids above 127).
  The same `Long` comparison remains in 18 places outside the parking module.
- Opening a vehicle for registration now sets the billing start date to today. Before, it carried the parking's full
  timestamp, which the API could not parse as a date.

## Tested (local API against the `orbtest` database)

`ddl-auto=update` created the new tables and columns. An end-to-end script passed all 38 checks:
- inspection refused without signatures, driver name, body condition or PKNG-INSPECT, and refused for a non-PNG or
  an over-5 KB signature;
- signed inspection → PENDING, with its tracking-device count and both signatures;
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
