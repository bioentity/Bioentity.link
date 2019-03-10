import { TestBed, inject } from '@angular/core/testing';

import { PublicLexiconService } from './public-lexicon.service';

describe('PublicLexiconService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PublicLexiconService]
    });
  });

  it('should be created', inject([PublicLexiconService], (service: PublicLexiconService) => {
    expect(service).toBeTruthy();
  }));
});
