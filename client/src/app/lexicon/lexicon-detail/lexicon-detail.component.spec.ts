import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LexiconDetailComponent } from './lexicon-detail.component';

describe('LexiconDetailComponent', () => {
  let component: LexiconDetailComponent;
  let fixture: ComponentFixture<LexiconDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LexiconDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LexiconDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
