import { TestBed } from '@angular/core/testing';

import { PosReceiptPrinterService } from './pos-receipt-printer.service';

describe('PosReceiptPrinterService', () => {
  let service: PosReceiptPrinterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PosReceiptPrinterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
