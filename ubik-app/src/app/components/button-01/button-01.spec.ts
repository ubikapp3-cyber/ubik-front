import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Button01 } from './button-01';

describe('Button01', () => {
  let component: Button01;
  let fixture: ComponentFixture<Button01>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Button01]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Button01);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
