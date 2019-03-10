import { TestBed, inject } from '@angular/core/testing';

import { AuthorServiceService } from './author-service.service';

describe('AuthorServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthorServiceService]
    });
  });

  it('should be created', inject([AuthorServiceService], (service: AuthorServiceService) => {
    expect(service).toBeTruthy();
  }));
});
