import { TestBed } from '@angular/core/testing';

import { MsgBoxService } from './msg-box.service';

describe('MsgBoxService', () => {
  let service: MsgBoxService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MsgBoxService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
