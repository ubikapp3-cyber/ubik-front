import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Inputcomponent } from './input';

describe('Input', () => {
  let component: Inputcomponent;
  let fixture: ComponentFixture<Inputcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inputcomponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Inputcomponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
