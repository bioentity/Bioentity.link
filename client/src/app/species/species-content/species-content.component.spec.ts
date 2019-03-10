import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SpeciesContentComponent } from './species-content.component';

describe('SpeciesContentComponent', () => {
  let component: SpeciesContentComponent;
  let fixture: ComponentFixture<SpeciesContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SpeciesContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SpeciesContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
