import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MarkupListComponent } from './markup-list.component';

describe('MarkupListComponent', () => {
  let component: MarkupListComponent;
  let fixture: ComponentFixture<MarkupListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MarkupListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MarkupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
