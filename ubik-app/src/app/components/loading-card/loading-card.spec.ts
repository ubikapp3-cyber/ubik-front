import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadingCard } from './loading-card';

describe('LoadingCard', () => {
  let component: LoadingCard;
  let fixture: ComponentFixture<LoadingCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoadingCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
