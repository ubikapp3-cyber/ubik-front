import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Explore } from './explore';

describe('Explore', () => {
  let component: Explore;
  let fixture: ComponentFixture<Explore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Explore]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Explore);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
