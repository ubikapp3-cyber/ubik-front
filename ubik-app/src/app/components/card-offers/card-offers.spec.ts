import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardOffers } from './card-offers';

describe('CardOffers', () => {
  let component: CardOffers;
  let fixture: ComponentFixture<CardOffers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardOffers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardOffers);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
