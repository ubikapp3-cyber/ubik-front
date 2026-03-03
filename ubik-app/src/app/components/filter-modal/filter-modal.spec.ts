import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterModal } from './filter-modal';

describe('FilterModal', () => {
  let component: FilterModal;
  let fixture: ComponentFixture<FilterModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilterModal],
    }).compileComponents();

    fixture = TestBed.createComponent(FilterModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
