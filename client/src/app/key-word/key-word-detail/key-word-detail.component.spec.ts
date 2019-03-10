import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyWordDetailComponent } from './key-word-detail.component';

describe('KeyWordDetailComponent', () => {
  let component: KeyWordDetailComponent;
  let fixture: ComponentFixture<KeyWordDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ KeyWordDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyWordDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
