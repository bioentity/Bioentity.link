import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyWordContentComponent } from './key-word-content.component';

describe('KeyWordContentComponent', () => {
  let component: KeyWordContentComponent;
  let fixture: ComponentFixture<KeyWordContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ KeyWordContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyWordContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
