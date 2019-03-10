import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LexiconListComponent } from './lexicon-list.component';

describe('LexiconListComponent', () => {
  let component: LexiconListComponent;
  let fixture: ComponentFixture<LexiconListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LexiconListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LexiconListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
