import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomsMotel } from './rooms-motel';

describe('RoomsMotel', () => {
  let component: RoomsMotel;
  let fixture: ComponentFixture<RoomsMotel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomsMotel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomsMotel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
