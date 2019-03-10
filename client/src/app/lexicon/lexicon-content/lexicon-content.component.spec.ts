import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LexiconContentComponent } from './lexicon-content.component';

describe('LexiconDetailsComponent', () => {
  let component: LexiconContentComponent;
  let fixture: ComponentFixture<LexiconContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LexiconContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LexiconContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
