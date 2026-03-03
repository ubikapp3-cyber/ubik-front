import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Card2 } from './card-2';

describe('Card2', () => {
  let component: Card2;
  let fixture: ComponentFixture<Card2>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Card2]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Card2);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
