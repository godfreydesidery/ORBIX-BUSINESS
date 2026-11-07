import { IParkingTrip, tripDateTime, tripReasonLabel } from 'src/app/domain/parking-trip';

// pdfmake and its fonts are loaded globally (angular.json scripts)
declare var pdfMake: any;

// Width of the receipt column, as on the checkout gate pass
const RECEIPT_WIDTH = 200

// Rows without padding or borders, so the pass uses as little POS paper as possible
const COMPACT = {
  hLineWidth: () => 0,
  vLineWidth: () => 0,
  paddingLeft: () => 0,
  paddingRight: () => 0,
  paddingTop: () => 0,
  paddingBottom: () => 1,
}

/**
 * "TEMPORARY EXIT" written diagonally (bottom-left to top-right), faint red, as a PNG to lay behind the pass text.
 * pdfmake's own watermark is centred on the A4 page, away from the receipt column, so it is drawn here instead.
 */
function diagonalWatermark(text: string, widthPt: number, heightPt: number): string {
  const scale = 4 // sharper print
  const canvas = document.createElement('canvas')
  canvas.width = widthPt * scale
  canvas.height = heightPt * scale
  const ctx = canvas.getContext('2d')!
  ctx.translate(canvas.width / 2, canvas.height / 2)
  ctx.rotate(-Math.atan2(canvas.height, canvas.width))
  ctx.fillStyle = 'rgba(220, 0, 0, 0.18)'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  // As large as fits along the diagonal, leaving the corners clear
  const diagonal = Math.hypot(canvas.width, canvas.height)
  let fontSize = 100
  ctx.font = 'bold ' + fontSize + 'px Arial'
  fontSize = fontSize * (diagonal * 0.8) / ctx.measureText(text).width
  ctx.font = 'bold ' + fontSize + 'px Arial'
  ctx.fillText(text, 0, 0)
  return canvas.toDataURL('image/png')
}

/**
 * Prints the temporary gate pass of a trip out of the yard, on the receipt (POS) layout of the checkout gate pass,
 * with "TEMPORARY EXIT" written diagonally behind the text so the gate can tell it apart: the vehicle must come back.
 * header is the branch receipt header (DataService.getBranchReceiptHeaderWithNoTinAndVrn).
 */
export function printTripGatePass(trip: IParkingTrip, header: any) {
  const line = (label: string, value: string, bold = false) =>
    [{ text: label + ': ' + (value ?? ''), alignment: 'left', fontSize: 9, bold: bold }]
  const rows = (body: any[]) => ({ layout: COMPACT, table: { headerRows: 0, widths: [RECEIPT_WIDTH], body: body } })
  // A section closed by a thin line under its last row
  const section = (body: any[]) => ({
    layout: { ...COMPACT, hLineWidth: (i: number, node: any) => i === node.table.body.length ? 0.5 : 0, paddingBottom: (i: number, node: any) => i === node.table.body.length - 1 ? 3 : 1 },
    margin: [0, 0, 0, 2],
    table: { headerRows: 0, widths: [RECEIPT_WIDTH], body: body }
  })

  const passBody = [
    section([
      [{ text: 'TEMPORARY GATE PASS', alignment: 'center', fontSize: 11, bold: true }],
      [{ text: 'Vehicle must return to the yard', alignment: 'center', fontSize: 8, italics: true }],
      line('Pass No', trip.no, true),
      line('Parking No', trip.parkingNo),
      line('Vehicle', trip.vehicleEquipmentTypeName),
      line('Color', trip.vehicleEquipmentColor),
      line('Chassis No', trip.chasisNo, true),
    ]),
    section([
      [{ text: 'Driver', alignment: 'center', fontSize: 9, bold: true }],
      line('Name', trip.driverName),
      line('Phone', trip.driverPhoneNo),
      line(trip.driverIdType || 'ID', trip.driverIdNo),
      line('Documents left', trip.documentsLeft ? 'YES' : 'NO', true),
    ]),
    rows([
      line('Reason', tripReasonLabel(trip.reason) + (trip.reasonDetails ? ' - ' + trip.reasonDetails : '')),
      line('Time Out', tripDateTime(trip.sentOutAt), true),
      line('Expected Back', tripDateTime(trip.expectedReturnAt), true),
      line('Issued By', trip.sentOutBy, true),
    ]),
    { text: 'Gate Signature: ........................', fontSize: 9, margin: [0, 8, 0, 0] },
  ]

  // Laid behind the whole pass (placed first, so drawn first), across it from the header down to the footer; it takes no space
  const markWidth = RECEIPT_WIDTH
  const markHeight = 320
  const watermark = {
    image: diagonalWatermark('TEMPORARY EXIT', markWidth, markHeight),
    width: markWidth,
    height: markHeight,
    relativePosition: { x: 0, y: 15 },
  }

  const docDefinition = {
    header: '',
    content: [
      watermark,
      { layout: COMPACT, table: header },
      { text: '==============================', margin: [0, 2, 0, 2] },
      ...passBody,
      {
        layout: COMPACT,
        margin: [0, 4, 0, 0],
        table: {
          headerRows: 0,
          widths: [210],
          body: [
            [{ text: '==============================' }],
            [{ text: 'Developed By @Davaghana', fontSize: 10, bold: true, alignment: 'center' }],
            [{ text: '***End of Document***', fontSize: 9, alignment: 'center' }]
          ]
        }
      },
    ],
    pageMargins: 10,
  }
  const win = window.open('', 'tempWinForPdf')
  pdfMake.createPdf(docDefinition).print({}, win)
}
