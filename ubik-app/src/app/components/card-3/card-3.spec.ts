import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Card3 } from './card-3';

describe('Card3', () => {
  let component: Card3;
  let fixture: ComponentFixture<Card3>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Card3]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Card3);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
