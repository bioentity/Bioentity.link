import { TestBed, inject } from '@angular/core/testing';

import { MarkupService } from './markup.service';

describe('MarkupService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MarkupService]
    });
  });

  it('should ...', inject([MarkupService], (service: MarkupService) => {
    expect(service).toBeTruthy();
  }));
});
