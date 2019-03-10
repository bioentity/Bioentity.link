import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicationLinksComponent } from './publication-links.component';

describe('PublicationLinksComponent', () => {
  let component: PublicationLinksComponent;
  let fixture: ComponentFixture<PublicationLinksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PublicationLinksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PublicationLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
