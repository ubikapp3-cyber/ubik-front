import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductRoom } from './product-room';

describe('ProductRoom', () => {
  let component: ProductRoom;
  let fixture: ComponentFixture<ProductRoom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductRoom]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductRoom);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
