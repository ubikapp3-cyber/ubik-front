import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavBarBottomComponent } from './nav-bar-bottom';

describe('NavBarBottomComponent', () => {
  let component: NavBarBottomComponent;
  let fixture: ComponentFixture<NavBarBottomComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarBottomComponent],
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavBarBottomComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
