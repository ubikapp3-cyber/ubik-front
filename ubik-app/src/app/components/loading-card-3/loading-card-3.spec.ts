import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadingCard3 } from './loading-card-3';

describe('LoadingCard3', () => {
  let component: LoadingCard3;
  let fixture: ComponentFixture<LoadingCard3>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingCard3]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoadingCard3);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
