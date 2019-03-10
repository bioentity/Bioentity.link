import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkupViewComponent } from './markup-view.component';

describe('MarkupViewComponent', () => {
  let component: MarkupViewComponent;
  let fixture: ComponentFixture<MarkupViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MarkupViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MarkupViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
