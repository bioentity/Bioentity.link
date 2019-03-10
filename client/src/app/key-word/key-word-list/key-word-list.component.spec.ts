import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyWordListComponent } from './key-word-list.component';

describe('KeyWordListComponent', () => {
  let component: KeyWordListComponent;
  let fixture: ComponentFixture<KeyWordListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ KeyWordListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyWordListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
