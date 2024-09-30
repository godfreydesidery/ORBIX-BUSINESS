import { TestBed } from '@angular/core/testing';

import { PharmacyPosReceiptService } from './pharmacy-pos-receipt.service';

describe('PharmacyPosReceiptService', () => {
  let service: PharmacyPosReceiptService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PharmacyPosReceiptService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
