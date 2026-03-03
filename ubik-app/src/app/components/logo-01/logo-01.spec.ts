import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Logo01 } from './logo-01';

describe('Logo01', () => {
  let component: Logo01;
  let fixture: ComponentFixture<Logo01>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Logo01]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Logo01);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
