import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MotelProfile } from './motel-profile';

describe('MotelProfile', () => {
  let component: MotelProfile;
  let fixture: ComponentFixture<MotelProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MotelProfile]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MotelProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
