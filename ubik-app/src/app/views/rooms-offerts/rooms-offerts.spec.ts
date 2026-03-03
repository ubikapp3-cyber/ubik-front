import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomsOfferts } from './rooms-offerts';

describe('RoomsOfferts', () => {
  let component: RoomsOfferts;
  let fixture: ComponentFixture<RoomsOfferts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomsOfferts]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomsOfferts);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
