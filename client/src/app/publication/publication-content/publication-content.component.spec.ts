import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicationContentComponent } from './publication-content.component';

describe('PublicationContentComponent', () => {
  let component: PublicationContentComponent;
  let fixture: ComponentFixture<PublicationContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PublicationContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PublicationContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
