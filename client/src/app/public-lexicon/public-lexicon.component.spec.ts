import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicLexiconComponent } from './public-lexicon.component';

describe('PublicLexiconComponent', () => {
  let component: PublicLexiconComponent;
  let fixture: ComponentFixture<PublicLexiconComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PublicLexiconComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PublicLexiconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
