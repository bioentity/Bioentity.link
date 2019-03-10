import { TestBed, inject } from '@angular/core/testing';

import { KeyWordService } from './key-word.service';

describe('KeyWordService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KeyWordService]
    });
  });

  it('should be created', inject([KeyWordService], (service: KeyWordService) => {
    expect(service).toBeTruthy();
  }));
});
