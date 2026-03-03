import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Button02 } from './button-02';

describe('Button02', () => {
  let component: Button02;
  let fixture: ComponentFixture<Button02>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Button02]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Button02);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
