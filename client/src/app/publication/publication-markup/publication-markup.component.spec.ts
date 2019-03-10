import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicationMarkupComponent } from './publication-markup.component';

describe('PublicationMarkupComponent', () => {
  let component: PublicationMarkupComponent;
  let fixture: ComponentFixture<PublicationMarkupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PublicationMarkupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PublicationMarkupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
