import { IParkingTrip, tripDateTime, tripReasonLabel } from 'src/app/domain/parking-trip';

// pdfmake and its fonts are loaded globally (angular.json scripts)
declare var pdfMake: any;

/**
 * Prints the temporary gate pass of a trip out of the yard. Same receipt layout as the checkout gate pass,
 * with a watermark so the gate can tell it apart: the vehicle must come back.
 * header is the branch receipt header (DataService.getBranchReceiptHeaderWithNoTinAndVrn).
 */
export function printTripGatePass(trip: IParkingTrip, header: any) {
  const line = (label: string, value: string, bold = false) =>
    [{ text: label + ': ' + (value ?? ''), alignment: 'left', fontSize: 9, bold: bold }]

  const body: any[] = [
    [{ text: 'TEMPORARY GATE PASS', alignment: 'center', fontSize: 11, bold: true }],
    [{ text: 'Vehicle must return to the yard', alignment: 'center', fontSize: 8, italics: true }],
    [{ text: ' ' }],
    line('Pass No', trip.no, true),
    line('Parking No', trip.parkingNo),
    line('Vehicle', trip.vehicleEquipmentTypeName),
    line('Color', trip.vehicleEquipmentColor),
    line('Chassis No', trip.chasisNo, true),
    line('Card No', trip.cardNo),
    [{ text: '________________________________' }],
    [{ text: 'Driver', alignment: 'center', fontSize: 9, bold: true }],
    line('Name', trip.driverName),
    line('Phone', trip.driverPhoneNo),
    line(trip.driverIdType || 'ID', trip.driverIdNo),
    line('Documents left', trip.documentsLeft ? 'YES' : 'NO', true),
    [{ text: '________________________________' }],
    line('Reason', tripReasonLabel(trip.reason) + (trip.reasonDetails ? ' - ' + trip.reasonDetails : '')),
    line('Time Out', tripDateTime(trip.sentOutAt), true),
    line('Expected Back', tripDateTime(trip.expectedReturnAt), true),
    [{ text: ' ' }],
    line('Issued By', trip.sentOutBy, true),
    [{ text: ' ' }],
    [{ text: 'Driver Signature: ......................', fontSize: 9 }],
    [{ text: ' ' }],
    [{ text: 'Gate Signature: ........................', fontSize: 9 }],
  ]

  const docDefinition = {
    header: '',
    watermark: { text: 'TEMPORARY EXIT', color: 'red', opacity: 0.15, bold: true, italics: false, angle: -45 },
    content: [
      { layout: 'noBorders', table: header },
      { layout: 'noBorders', table: { headerRows: 0, widths: [210], body: [[{ text: '==============================' }]] } },
      { layout: 'noBorders', table: { headerRows: 0, widths: [200], body: body } },
      {
        layout: 'noBorders',
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
