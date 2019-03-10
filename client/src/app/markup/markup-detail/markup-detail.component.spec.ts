import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkupDetailComponent } from './markup-detail.component';

describe('MarkupDetailComponent', () => {
  let component: MarkupDetailComponent;
  let fixture: ComponentFixture<MarkupDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MarkupDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MarkupDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
