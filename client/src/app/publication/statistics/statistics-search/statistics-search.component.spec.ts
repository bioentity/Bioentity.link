import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticsSearchComponent } from './statistics-search.component';

describe('StatisticsSearchComponent', () => {
  let component: StatisticsSearchComponent;
  let fixture: ComponentFixture<StatisticsSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StatisticsSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatisticsSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
